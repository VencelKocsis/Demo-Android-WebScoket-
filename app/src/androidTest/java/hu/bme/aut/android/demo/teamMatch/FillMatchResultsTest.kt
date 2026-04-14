package hu.bme.aut.android.demo.teamMatch

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import hu.bme.aut.android.demo.MainActivity
import hu.bme.aut.android.demo.data.TestData
import hu.bme.aut.android.demo.data.TestData.generateRandomSets
import hu.bme.aut.android.demo.functions.Authentication.ensureLoggedOut
import hu.bme.aut.android.demo.functions.Authentication.login
import hu.bme.aut.android.demo.functions.Authentication.logoutWithTryCatch
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
        val match = TestData.teamMatches[35]
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

        // Várjuk meg, amíg az új emulátor betölti a meccs részleteit
        composeTestRule.waitUntilAtLeastOneExists(hasText("Mérkőzés Információk", substring = true, ignoreCase = true), 15000)
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        scrollAndClickText(composeTestRule, "elindítása", "Nem találtam meg a Meccs Indítása gombot!")

        composeTestRule.waitUntilAtLeastOneExists(hasText("beküldése", substring = true, ignoreCase = true), 10000)
        scrollAndClickText(composeTestRule, "beküldése", "Nem találtam meg az Elküldés gombot a vendég felállásnál!")

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithContentDescription("Visszalépés").performClick()
        Thread.sleep(500)
        composeTestRule.onNodeWithContentDescription("Visszalépés").performClick()
        waitForMatchList(composeTestRule)

        logoutWithTryCatch(composeTestRule)

        // ========================================================
        // 2. HAZAI KAPITÁNY: Felállás elküldése és EREDMÉNYEK BEVITEL
        // ========================================================
        login(composeTestRule, homeTeam.captain.email, homeTeam.captain.password)
        navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
        waitForMatchList(composeTestRule)
        openMatch(composeTestRule, match.id)

        // Itt is a fixen látható elemre várunk a hálózat betöltésekor
        composeTestRule.waitUntilAtLeastOneExists(hasText("Mérkőzés Információk", substring = true, ignoreCase = true), 15000)
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        scrollAndClickText(composeTestRule, "élő mérkőzés", "Nem találtam meg a Tovább az Élő Mérkőzésre gombot!")

        composeTestRule.waitUntilAtLeastOneExists(hasText("beküldése", substring = true, ignoreCase = true), 10000)
        scrollAndClickText(composeTestRule, "beküldése", "Nem találtam meg az Elküldés gombot a hazai felállásnál!")

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("match_grid_list"), 15000)
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // 16 EGYÉNI MECCS KITÖLTÉSE RANDOM SZETTEKKEL
        for (i in 1..16) {

            composeTestRule.onNodeWithTag("match_grid_list")
                .performScrollToNode(hasTestTag("individual_match_card_$i"))

            composeTestRule.onNode(hasTestTag("individual_match_card_$i")).performClick()

            composeTestRule.waitUntilAtLeastOneExists(hasText("Véglegesítés", substring = true, ignoreCase = true), 10000)
            composeTestRule.waitForIdle()

            val generatedSets = generateRandomSets()

            generatedSets.forEachIndexed { setIndex, (homePoints, guestPoints) ->

                composeTestRule.waitUntilAtLeastOneExists(hasTestTag("input_home_$setIndex"), 10000)

                Thread.sleep(800) // Nagyobb szünet mielőtt bármit is csinálunk a szettel

                // 🔥 HAZAI PONT BEÍRÁSA LASSÍTVA ÉS BIZTOSÍTVA
                val homeNode = composeTestRule.onNode(hasTestTag("input_home_$setIndex"), useUnmergedTree = true)
                homeNode.performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(800) // Bőséges idő a billentyűzet megnyílásának

                homeNode.performTextClearance()
                composeTestRule.waitForIdle()
                Thread.sleep(200) // Idő a törlés regisztrálására

                homeNode.performTextInput(homePoints.toString())
                composeTestRule.waitForIdle()
                Thread.sleep(800) // Idő a bevitel regisztrálására

                // 🔥 VENDÉG PONT BEÍRÁSA LASSÍTVA ÉS BIZTOSÍTVA
                val guestNode = composeTestRule.onNode(hasTestTag("input_guest_$setIndex"), useUnmergedTree = true)
                guestNode.performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(800) // Bőséges idő a fókuszváltásnak

                guestNode.performTextClearance()
                composeTestRule.waitForIdle()
                Thread.sleep(200) // Idő a törlés regisztrálására

                guestNode.performTextInput(guestPoints.toString())
                composeTestRule.waitForIdle()
                Thread.sleep(1500) // Extrém várakozás, hogy mindkét mező értéke biztosan beálljon

                if (setIndex < generatedSets.size - 1) {
                    val mentesGomb = composeTestRule.onNode(hasText("Mentés", substring = true, ignoreCase = true))
                    mentesGomb.assertIsEnabled() // Ha itt elszáll, tudjuk, hogy az emulátor megint megette a számot!
                    mentesGomb.performClick()

                    composeTestRule.waitForIdle()
                    Thread.sleep(3000) // Még több idő a hálózati kérésnek és a Compose újrarenderelésnek
                }
            }

            composeTestRule.onNode(hasText("Véglegesítés", substring = true, ignoreCase = true))
                .assertIsEnabled()
                .performClick()

            composeTestRule.waitUntilAtLeastOneExists(hasText("lezárult", substring = true, ignoreCase = true), 15000)

            composeTestRule.onNodeWithContentDescription("Vissza").performClick()

            composeTestRule.waitUntilAtLeastOneExists(hasTestTag("match_grid_list"), 10000)
            composeTestRule.waitForIdle()
            Thread.sleep(800)
        }

        composeTestRule.onNodeWithTag("match_grid_list")
            .performScrollToNode(hasText("Aláírom", substring = true, ignoreCase = true))

        scrollAndClickText(composeTestRule, "Aláírom", "Nem találtam meg az Aláírás gombot a hazai kapitánynál!")
        Thread.sleep(1500)

        composeTestRule.onNodeWithContentDescription("Visszalépés").performClick()
        Thread.sleep(500)
        composeTestRule.onNodeWithContentDescription("Visszalépés").performClick()
        waitForMatchList(composeTestRule)

        logoutWithTryCatch(composeTestRule)

        // ========================================================
        // 3. VENDÉG KAPITÁNY: Aláírás és Lezárás
        // ========================================================
        login(composeTestRule, guestTeam.captain.email, guestTeam.captain.password)
        navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
        waitForMatchList(composeTestRule)
        openMatch(composeTestRule, match.id)

        // És itt is a fixen látható elemre várunk!
        composeTestRule.waitUntilAtLeastOneExists(hasText("Mérkőzés Információk", substring = true, ignoreCase = true), 15000)
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        scrollAndClickText(composeTestRule, "élő mérkőzés", "Nem találtam meg a Tovább az Élő Mérkőzésre gombot a vendég kapitánynál!")

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("match_grid_list"), 15000)

        composeTestRule.onNodeWithTag("match_grid_list")
            .performScrollToNode(hasText("Aláírom", substring = true, ignoreCase = true))

        scrollAndClickText(composeTestRule, "Aláírom", "Nem találtam meg az Aláírás gombot a vendég kapitánynál!")

        composeTestRule.waitUntilAtLeastOneExists(hasText("hivatalosan", substring = true, ignoreCase = true), 15000)

        composeTestRule.onNodeWithContentDescription("Visszalépés").performClick()
        Thread.sleep(500)
        composeTestRule.onNodeWithContentDescription("Visszalépés").performClick()
    }
}
