package hu.bme.aut.android.demo.teamMatch

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import hu.bme.aut.android.demo.MainActivity
import hu.bme.aut.android.demo.data.TestData
import hu.bme.aut.android.demo.functions.Authentication.ensureLoggedOut
import hu.bme.aut.android.demo.functions.Authentication.login
import hu.bme.aut.android.demo.functions.Authentication.logoutWithTryCatch
import hu.bme.aut.android.demo.functions.Match.goBack
import hu.bme.aut.android.demo.functions.Match.openMatch
import hu.bme.aut.android.demo.functions.Match.scrollAndClickText
import hu.bme.aut.android.demo.functions.Match.waitForMatchList
import hu.bme.aut.android.demo.functions.Navigation
import hu.bme.aut.android.demo.functions.Navigation.navigate
import org.junit.Rule
import org.junit.Test

class FillMatchResultsTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun fillMatchResultsAndSignWorkflow() {
        val match = TestData.teamMatches[0]
        val homeTeam = match.homeTeam
        val guestTeam = match.guestTeam

        ensureLoggedOut(composeTestRule)

        // ========================================================
        // 1. VENDÉG KAPITÁNY: Meccs indítása és Felállás (Lineup) elküldése
        // ========================================================
        login(composeTestRule, guestTeam.captain.email, guestTeam.captain.password)
        navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
        waitForMatchList(composeTestRule)
        openMatch(composeTestRule, match.id)

        // Pontosított keresőszó: "elindítása"
        scrollAndClickText(composeTestRule, "elindítása", "Nem találtam meg a Meccs Indítása gombot!")

        composeTestRule.waitUntilAtLeastOneExists(hasText("elküldése", substring = true, ignoreCase = true), 10000)
        scrollAndClickText(composeTestRule, "elküldése", "Nem találtam meg az Elküldés gombot a vendég felállásnál!")

        goBack(composeTestRule)
        logoutWithTryCatch(composeTestRule)

        // ========================================================
        // 2. HAZAI KAPITÁNY: Felállás elküldése és EREDMÉNYEK BEVITEL
        // ========================================================
        login(composeTestRule, homeTeam.captain.email, homeTeam.captain.password)
        navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
        waitForMatchList(composeTestRule)
        openMatch(composeTestRule, match.id)

        // Pontosított keresőszó: "élő mérkőzés"
        scrollAndClickText(composeTestRule, "élő mérkőzés", "Nem találtam meg a Tovább az Élő Mérkőzésre gombot!")

        composeTestRule.waitUntilAtLeastOneExists(hasText("elküldése", substring = true, ignoreCase = true), 10000)
        scrollAndClickText(composeTestRule, "elküldése", "Nem találtam meg az Elküldés gombot a hazai felállásnál!")

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("individual_match_card"), 15000)
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // 16 EGYÉNI MECCS KITÖLTÉSE
        for (i in 0..15) {
            composeTestRule.onAllNodesWithTag("individual_match_card")[i].performScrollTo().performClick()

            composeTestRule.waitUntilAtLeastOneExists(hasText("Véglegesítés", substring = true, ignoreCase = true), 10000)
            composeTestRule.waitForIdle()

            // 3 szett kitöltése (11-0)
            val textFields = composeTestRule.onAllNodes(hasSetTextAction())
            textFields[0].performTextInput("11")
            textFields[1].performTextInput("0")
            textFields[2].performTextInput("11")
            textFields[3].performTextInput("0")
            textFields[4].performTextInput("11")
            textFields[5].performTextInput("0")

            composeTestRule.waitForIdle()
            scrollAndClickText(composeTestRule, "Véglegesítés", "Nem találtam a Véglegesítés gombot az egyéni meccsnél!")

            composeTestRule.waitUntilAtLeastOneExists(hasTestTag("individual_match_card"), 10000)
            composeTestRule.waitForIdle()
            Thread.sleep(500)
        }

        // Aláírás
        scrollAndClickText(composeTestRule, "Aláírás", "Nem találtam meg az Aláírás gombot a hazai kapitánynál!")

        Thread.sleep(1500)
        goBack(composeTestRule)
        logoutWithTryCatch(composeTestRule)

        // ========================================================
        // 3. VENDÉG KAPITÁNY: Aláírás és Lezárás
        // ========================================================
        login(composeTestRule, guestTeam.captain.email, guestTeam.captain.password)
        navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
        waitForMatchList(composeTestRule)
        openMatch(composeTestRule, match.id)

        scrollAndClickText(composeTestRule, "élő mérkőzés", "Nem találtam meg a Tovább az Élő Mérkőzésre gombot a vendég kapitánynál!")

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("individual_match_card"), 15000)

        // Aláírás Vendégként
        scrollAndClickText(composeTestRule, "Aláírás", "Nem találtam meg az Aláírás gombot a vendég kapitánynál!")

        composeTestRule.waitUntilAtLeastOneExists(hasText("hivatalosan is véget ért", substring = true, ignoreCase = true), 15000)
    }
}