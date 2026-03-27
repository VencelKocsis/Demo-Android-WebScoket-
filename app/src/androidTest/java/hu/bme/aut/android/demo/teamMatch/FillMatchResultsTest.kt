package hu.bme.aut.android.demo.teamMatch

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
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
    fun fillAllMatchResultsAndSignWorkflow() {
        // Végigmegyünk az ÖSSZES meccsen a TestData-ból
        for ((matchIndex, match) in TestData.teamMatches.withIndex()) {

            val homeTeam = match.homeTeam
            val guestTeam = match.guestTeam

            println("=========================================================================")
            println("▶ START: ${matchIndex + 1} / ${TestData.teamMatches.size}. MECCS -> ${homeTeam.teamName} vs ${guestTeam.teamName}")
            println("=========================================================================")

            ensureLoggedOut(composeTestRule)

            // ========================================================
            // 1. VENDÉG KAPITÁNY: Meccs indítása és Sorrend beküldése
            // ========================================================
            login(composeTestRule, guestTeam.captain.email, guestTeam.captain.password)
            navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
            waitForMatchList(composeTestRule)
            openMatch(composeTestRule, match.id)

            composeTestRule.waitForIdle()
            Thread.sleep(2000)

            scrollAndClickText(composeTestRule, "elindítása", "Nem találtam meg a Meccs Indítása gombot!")

            composeTestRule.waitForIdle()
            Thread.sleep(2000)

            scrollAndClickText(composeTestRule, "beküldése", "Nem találtam meg a Beküldés gombot a vendég felállásnál!")

            composeTestRule.waitForIdle()
            Thread.sleep(2000)
            composeTestRule.onNodeWithTag("back_button").performClick()

            goBack(composeTestRule)
            logoutWithTryCatch(composeTestRule)

            // ========================================================
            // 2. HAZAI KAPITÁNY: Sorrend beküldése ÉS Eredmények kitöltése
            // ========================================================
            login(composeTestRule, homeTeam.captain.email, homeTeam.captain.password)
            navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
            waitForMatchList(composeTestRule)
            openMatch(composeTestRule, match.id)

            composeTestRule.waitForIdle()
            Thread.sleep(2000)

            scrollAndClickText(composeTestRule, "élő mérkőzés", "Nem találtam meg a Tovább az Élő Mérkőzésre gombot!")

            composeTestRule.waitForIdle()
            Thread.sleep(2000)

            scrollAndClickText(composeTestRule, "beküldése", "Nem találtam meg a Beküldés gombot a hazai felállásnál!")

            composeTestRule.waitForIdle()
            Thread.sleep(2000)

            // --- BUG WORKAROUND: Visszalépés a testTag-gel, majd újra belépés ---
            composeTestRule.onNodeWithTag("back_button").performClick()

            composeTestRule.waitUntilAtLeastOneExists(hasText("Mérkőzés Részletei", substring = true, ignoreCase = true), 5000)
            composeTestRule.waitForIdle()
            Thread.sleep(1000)

            scrollAndClickText(composeTestRule, "élő mérkőzés", "Bug workaround: Nem találtam meg az Élő Mérkőzés gombot visszalépés után!")
            // -------------------------------------------------------------------

            composeTestRule.waitForIdle()
            Thread.sleep(2000)

            // 16 EGYÉNI MECCS KITÖLTÉSE
            for (i in 1..16) {
                val cardTag = "individual_match_card_$i"

                composeTestRule.onNodeWithTag("match_grid_list")
                    .performScrollToNode(hasTestTag(cardTag))

                composeTestRule.onNodeWithTag(cardTag).performClick()

                composeTestRule.waitUntilAtLeastOneExists(hasText("Eredmény rögzítése", substring = true, ignoreCase = true), 5000)
                composeTestRule.waitForIdle()
                Thread.sleep(1000)

                // ---- DINAMIKUS RANDOM EREDMÉNY BEÍRÁSA ----
                val generatedSets = generateRandomSets()

                try {
                    generatedSets.forEachIndexed { setIndex, (homePoints, guestPoints) ->
                        composeTestRule.waitForIdle()
                        Thread.sleep(300)

                        composeTestRule.onNodeWithTag("input_home_$setIndex", useUnmergedTree = true)
                            .performTextReplacement(homePoints.toString())

                        composeTestRule.waitForIdle()

                        composeTestRule.onNodeWithTag("input_guest_$setIndex", useUnmergedTree = true)
                            .performTextReplacement(guestPoints.toString())
                    }
                } catch (e: Throwable) {
                    println("Hiba az eredmény beírásánál a(z) $i. meccsen: ${e.message}")
                }

                composeTestRule.waitForIdle()
                Thread.sleep(1000)

                // Lekattintjuk a Véglegesítést
                scrollAndClickText(composeTestRule, "Véglegesítés", "Nem találtam a Véglegesítés gombot az egyéni meccsnél!")

                // Várunk a mentésre
                composeTestRule.waitForIdle()
                Thread.sleep(1500)

                // --- BIZTOSÍTÉK: Visszalépés, ha nem dobott vissza automatikusan ---
                try {
                    if (composeTestRule.onAllNodesWithText("Eredmény rögzítése").fetchSemanticsNodes().isNotEmpty()) {
                        composeTestRule.onNodeWithContentDescription("Vissza").performClick()
                    }
                } catch (e: Throwable) {}

                composeTestRule.waitForIdle()
                Thread.sleep(500)
            }

            // --- ALÁÍRÁS HAZAIKÉNT ---
            composeTestRule.waitForIdle()
            Thread.sleep(1500)

            try {
                val listNode = composeTestRule.onNodeWithTag("match_grid_list")
                for (i in 0..10) {
                    if (composeTestRule.onAllNodesWithText("Aláírom", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()) {
                        break
                    }
                    listNode.performTouchInput { swipeUp(durationMillis = 300) }
                    composeTestRule.waitForIdle()
                }
            } catch (e: Throwable) {}

            composeTestRule.waitForIdle()
            Thread.sleep(1000)

            scrollAndClickText(composeTestRule, "Aláírom", "Nem találtam meg az Aláírom gombot a hazai kapitánynál!")

            Thread.sleep(1500)

            composeTestRule.onNodeWithTag("back_button").performClick()

            goBack(composeTestRule)
            logoutWithTryCatch(composeTestRule)

            // ========================================================
            // 3. VENDÉG KAPITÁNY: Visszatér és Aláírja a jegyzőkönyvet
            // ========================================================
            login(composeTestRule, guestTeam.captain.email, guestTeam.captain.password)
            navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
            waitForMatchList(composeTestRule)
            openMatch(composeTestRule, match.id)

            composeTestRule.waitForIdle()
            Thread.sleep(2000)

            scrollAndClickText(composeTestRule, "élő mérkőzés", "Nem találtam meg a Tovább az Élő Mérkőzésre gombot a vendég kapitánynál!")

            composeTestRule.waitUntilAtLeastOneExists(hasTestTag("match_grid_list"), 15000)

            // --- ALÁÍRÁS VENDÉGKÉNT ---
            composeTestRule.waitForIdle()
            Thread.sleep(1500)

            try {
                val listNode = composeTestRule.onNodeWithTag("match_grid_list")
                for (i in 0..10) {
                    if (composeTestRule.onAllNodesWithText("Aláírom", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()) {
                        break
                    }
                    listNode.performTouchInput { swipeUp(durationMillis = 300) }
                    composeTestRule.waitForIdle()
                }
            } catch (e: Throwable) {}

            composeTestRule.waitForIdle()
            Thread.sleep(1000)

            scrollAndClickText(composeTestRule, "Aláírom", "Nem találtam meg az Aláírom gombot a vendég kapitánynál!")

            // Végső ellenőrzés: Megjelent-e a lezáró üzenet?
            composeTestRule.waitUntilAtLeastOneExists(hasText("LEZÁRULT", substring = true, ignoreCase = true), 15000)

            println("✔ ${matchIndex + 1}. MECCS SIKERESEN LEZÁRVA!")

            // --- VISSZALÉPÉS ÉS KIJELENTKEZÉS A KÖVETKEZŐ ITERÁCIÓHOZ ---
            Thread.sleep(1500)
            try {
                composeTestRule.onNodeWithTag("back_button").performClick()
                goBack(composeTestRule)
                logoutWithTryCatch(composeTestRule)
            } catch (e: Throwable) {
                ensureLoggedOut(composeTestRule)
            }
        }

        println("🏆 AZ ÖSSZES MECCS (${TestData.teamMatches.size} db) SIKERESEN KITÖLTVE ÉS LEZÁRVA! 🏆")
    }

    /**
     * Valósághű asztalitenisz meccseredmény generátor.
     * Visszatér egy listával, ahol a Pairek a Hazai és Vendég pontszámokat jelzik.
     */
    private fun generateRandomSets(): List<Pair<Int, Int>> {
        val sets = mutableListOf<Pair<Int, Int>>()
        var homeWins = 0
        var guestWins = 0

        while (homeWins < 3 && guestWins < 3) {
            val homeWinsSet = (0..1).random() == 1
            val isDeuce = (1..100).random() <= 8
            val homeScore: Int
            val guestScore: Int

            if (isDeuce) {
                val winnerScore = (12..20).random()
                val loserScore = winnerScore - 2

                if (homeWinsSet) {
                    homeScore = winnerScore
                    guestScore = loserScore
                } else {
                    homeScore = loserScore
                    guestScore = winnerScore
                }
            } else {
                val loserScore = (0..9).random()
                if (homeWinsSet) {
                    homeScore = 11
                    guestScore = loserScore
                } else {
                    homeScore = loserScore
                    guestScore = 11
                }
            }

            sets.add(Pair(homeScore, guestScore))
            if (homeWinsSet) homeWins++ else guestWins++
        }
        return sets
    }
}