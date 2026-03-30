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

class FillAllMatchResultsTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun fillAllMatchResultsAndSignWorkflow() {
        ensureLoggedOut(composeTestRule)

        // =========================================================================
        // FÁZIS 1: VENDÉG KAPITÁNYOK MECCS INDÍTÁSA ÉS FELÁLLÁS BEKÜLDÉSE
        // =========================================================================
        println("=== FÁZIS 1: VENDÉG SORRENDEK BEKÜLDÉSE ===")

        for (team in TestData.teams) {
            val guestMatches = TestData.teamMatches.filter { it.guestTeam.id == team.id }
            if (guestMatches.isEmpty()) continue

            login(composeTestRule, team.captain.email, team.captain.password)
            navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
            waitForMatchList(composeTestRule)

            for (match in guestMatches) {
                openMatch(composeTestRule, match.id)
                composeTestRule.waitForIdle()
                Thread.sleep(1500)

                scrollAndClickText(composeTestRule, "elindítása", "Nem találtam meg a Meccs Indítása gombot!")

                composeTestRule.waitUntilAtLeastOneExists(hasText("beküldése", substring = true, ignoreCase = true), 10000)
                scrollAndClickText(composeTestRule, "beküldése", "Nem találtam meg az Elküldés gombot a vendég felállásnál!")

                composeTestRule.waitForIdle()
                Thread.sleep(1000)

                // Stabil visszalépés a meccslistáig
                composeTestRule.onNodeWithContentDescription("Visszalépés").performClick()
                Thread.sleep(500)
                composeTestRule.onNodeWithContentDescription("Visszalépés").performClick()
                waitForMatchList(composeTestRule)
            }
            logoutWithTryCatch(composeTestRule)
            Thread.sleep(1000)
        }

        // =========================================================================
        // FÁZIS 2: HAZAI KAPITÁNYOK FELÁLLÁS BEKÜLDÉSE, EREDMÉNYEK BEVITEL ÉS ALÁÍRÁS
        // =========================================================================
        println("=== FÁZIS 2: HAZAI SORRENDEK, EREDMÉNYEK KITÖLTÉSE ÉS HAZAI ALÁÍRÁS ===")

        for (team in TestData.teams) {
            val homeMatches = TestData.teamMatches.filter { it.homeTeam.id == team.id }
            if (homeMatches.isEmpty()) continue

            login(composeTestRule, team.captain.email, team.captain.password)
            navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
            waitForMatchList(composeTestRule)

            for (match in homeMatches) {
                println("   -> Eredmények kitöltése: ${match.homeTeam.teamName} vs ${match.guestTeam.teamName}")
                openMatch(composeTestRule, match.id)
                composeTestRule.waitForIdle()
                Thread.sleep(1500)

                scrollAndClickText(composeTestRule, "élő mérkőzés", "Nem találtam meg a Tovább az Élő Mérkőzésre gombot!")

                composeTestRule.waitUntilAtLeastOneExists(hasText("beküldése", substring = true, ignoreCase = true), 10000)
                scrollAndClickText(composeTestRule, "beküldése", "Nem találtam meg az Elküldés gombot a hazai felállásnál!")

                composeTestRule.waitUntilAtLeastOneExists(hasTestTag("match_grid_list"), 15000)
                composeTestRule.waitForIdle()
                Thread.sleep(2000)

                // 16 EGYÉNI MECCS KITÖLTÉSE A STABILIZÁLT LOGIKÁVAL
                for (i in 1..16) {

                    // Gördítés és kattintás a megfelelő egyéni kártyára
                    composeTestRule.onNodeWithTag("match_grid_list")
                        .performScrollToNode(hasTestTag("individual_match_card_$i"))

                    composeTestRule.onNode(hasTestTag("individual_match_card_$i")).performClick()

                    composeTestRule.waitUntilAtLeastOneExists(hasText("Véglegesítés", substring = true, ignoreCase = true), 10000)
                    composeTestRule.waitForIdle()

                    val generatedSets = generateRandomSets()

                    generatedSets.forEachIndexed { setIndex, (homePoints, guestPoints) ->

                        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("input_home_$setIndex"), 10000)
                        Thread.sleep(500)

                        // Hazai pont
                        val homeNode = composeTestRule.onNode(hasTestTag("input_home_$setIndex"), useUnmergedTree = true)
                        homeNode.performClick()
                        composeTestRule.waitForIdle()
                        Thread.sleep(300)
                        homeNode.performTextClearance()
                        homeNode.performTextInput(homePoints.toString())

                        composeTestRule.waitForIdle()
                        Thread.sleep(400)

                        // Vendég pont
                        val guestNode = composeTestRule.onNode(hasTestTag("input_guest_$setIndex"), useUnmergedTree = true)
                        guestNode.performClick()
                        composeTestRule.waitForIdle()
                        Thread.sleep(300)
                        guestNode.performTextClearance()
                        guestNode.performTextInput(guestPoints.toString())

                        composeTestRule.waitForIdle()
                        Thread.sleep(1000)

                        if (setIndex < generatedSets.size - 1) {
                            composeTestRule.onNode(hasText("Mentés", substring = true, ignoreCase = true)).performClick()
                            composeTestRule.waitForIdle()
                            Thread.sleep(2500)
                        }
                    }

                    // Meccs véglegesítése
                    composeTestRule.onNode(hasText("Véglegesítés", substring = true, ignoreCase = true))
                        .assertIsEnabled()
                        .performClick()

                    composeTestRule.waitUntilAtLeastOneExists(hasText("lezárult", substring = true, ignoreCase = true), 15000)

                    // Visszalépés a gridhez
                    composeTestRule.onNodeWithContentDescription("Vissza").performClick()

                    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("match_grid_list"), 10000)
                    composeTestRule.waitForIdle()
                    Thread.sleep(800)
                }

                // Legörgetés az aláírás gombhoz a 16 meccs után
                composeTestRule.onNodeWithTag("match_grid_list")
                    .performScrollToNode(hasText("Aláírom", substring = true, ignoreCase = true))

                scrollAndClickText(composeTestRule, "Aláírom", "Nem találtam meg az Aláírás gombot a hazai kapitánynál!")
                Thread.sleep(1500)

                // Visszalépés a meccslistáig
                composeTestRule.onNodeWithContentDescription("Visszalépés").performClick()
                Thread.sleep(500)
                composeTestRule.onNodeWithContentDescription("Visszalépés").performClick()
                waitForMatchList(composeTestRule)
            }
            logoutWithTryCatch(composeTestRule)
            Thread.sleep(1000)
        }

        // =========================================================================
        // FÁZIS 3: VENDÉG KAPITÁNYOK ALÁÍRÁSA ÉS LEZÁRÁS
        // =========================================================================
        println("=== FÁZIS 3: VENDÉG ALÁÍRÁSOK ÉS VÉGLEGESÍTÉS ===")

        for (team in TestData.teams) {
            val guestMatches = TestData.teamMatches.filter { it.guestTeam.id == team.id }
            if (guestMatches.isEmpty()) continue

            login(composeTestRule, team.captain.email, team.captain.password)
            navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
            waitForMatchList(composeTestRule)

            for (match in guestMatches) {
                openMatch(composeTestRule, match.id)
                composeTestRule.waitForIdle()
                Thread.sleep(1500)

                scrollAndClickText(composeTestRule, "élő mérkőzés", "Nem találtam meg a Tovább az Élő Mérkőzésre gombot a vendég kapitánynál!")

                composeTestRule.waitUntilAtLeastOneExists(hasTestTag("match_grid_list"), 15000)

                // Legörgetünk az Aláírom gombhoz
                composeTestRule.onNodeWithTag("match_grid_list")
                    .performScrollToNode(hasText("Aláírom", substring = true, ignoreCase = true))

                scrollAndClickText(composeTestRule, "Aláírom", "Nem találtam meg az Aláírás gombot a vendég kapitánynál!")

                // Pontosított keresés a meccs végére
                composeTestRule.waitUntilAtLeastOneExists(hasText("hivatalosan", substring = true, ignoreCase = true), 15000)

                // Visszalépés a meccslistáig
                composeTestRule.onNodeWithContentDescription("Visszalépés").performClick()
                Thread.sleep(500)
                composeTestRule.onNodeWithContentDescription("Visszalépés").performClick()
                waitForMatchList(composeTestRule)
            }
            logoutWithTryCatch(composeTestRule)
            Thread.sleep(1000)
        }

        println("🏆 AZ ÖSSZES MECCS SIKERESEN KITÖLTVE ÉS LEZÁRVA RANDOM PONTSZÁMOKKAL! 🏆")
    }
}