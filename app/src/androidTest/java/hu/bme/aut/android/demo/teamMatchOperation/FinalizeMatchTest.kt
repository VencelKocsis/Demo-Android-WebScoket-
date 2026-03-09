package hu.bme.aut.android.demo.teamMatchOperation

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import hu.bme.aut.android.demo.MainActivity
import hu.bme.aut.android.demo.data.DataStructure.CaptainTestUser
import hu.bme.aut.android.demo.data.DataStructure.FullTestUser
import hu.bme.aut.android.demo.data.DataStructure.TeamData
import hu.bme.aut.android.demo.data.DataStructure.TeamMatchData
import hu.bme.aut.android.demo.functions.Authentication.login
import hu.bme.aut.android.demo.functions.Authentication.logout
import hu.bme.aut.android.demo.functions.Navigation
import hu.bme.aut.android.demo.functions.Navigation.navigate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinalizeMatchTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    val teams = listOf(
        // BEAC III.
        TeamData(
            captain = CaptainTestUser("tf@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Katus Ferenc", "kf@test.com", "bestpassword"),
                FullTestUser("Szabó Miklós", "szm@test.com", "bestpassword"),
                FullTestUser("Szekulesz Péter", "szp@test.com", "bestpassword"),
                FullTestUser("Bakos Bertalan", "babe@test.com", "bestpassword")
            ),
            teamName = "BEAC III."
        ),
        // BEAC IV.
        TeamData(
            captain = CaptainTestUser("nzs@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Széles Gergő", "szg@test.com", "bestpassword"),
                FullTestUser("Gulyás Áron", "ga@test.com", "bestpassword"),
                FullTestUser("Gábor Norbert", "gn@test.com", "bestpassword"),
                FullTestUser("Szabó Győző", "szgy@test.com", "bestpassword")
            ),
            teamName = "BEAC IV."
        ),
        // BEAC V.
        TeamData(
            captain = CaptainTestUser("id@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Kurucz Máté", "km@test.com", "bestpassword"),
                FullTestUser("Juhász Péter", "jp@test.com", "bestpassword"),
                FullTestUser("Wiener Gábor", "wg@test.com", "bestpassword"),
                FullTestUser("Kocsis Vencel", "kv@test.com", "bestpassword")
            ),
            teamName = "BEAC V."
        ),
        // BEAC VI.
        TeamData(
            captain = CaptainTestUser("kp@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Sipos Tamás", "st@test.com", "bestpassword"),
                FullTestUser("Tengerdi Tibor", "tt@test.com", "bestpassword"),
                FullTestUser("Böröcz Botond", "bb@test.com", "bestpassword"),
                FullTestUser("Reszler Balázs", "rb@test.com", "bestpassword")
            ),
            teamName = "BEAC VI."
        )
    )

    val teamMatches = listOf(
        TeamMatchData(homeTeam = teams[0], guestTeam = teams[1]),
        TeamMatchData(homeTeam = teams[2], guestTeam = teams[3]),
        TeamMatchData(homeTeam = teams[0], guestTeam = teams[2]),
        TeamMatchData(homeTeam = teams[1], guestTeam = teams[2])
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captainFinalizeMatches() {
        teams.forEach { team ->

            val matchesToFinalize = teamMatches.filter { it.homeTeam == team || it.guestTeam == team }

            if (matchesToFinalize.isNotEmpty()) {
                login(composeTestRule, team.captain.email, team.captain.password)
                navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)

                composeTestRule.waitForIdle()
                Thread.sleep(2500) // Kicsit megnöveltem az induló várakozást

                matchesToFinalize.forEach { match ->
                    val matchTitle = "${match.homeTeam.teamName} vs ${match.guestTeam.teamName}"

                    // 1. MECCS KÁRTYA KERESÉSE (Várakozással)
                    composeTestRule.waitUntilAtLeastOneExists(hasScrollToNodeAction(), 10000)

                    composeTestRule.onAllNodes(hasScrollToNodeAction())[0]
                        .performScrollToNode(hasText(matchTitle, substring = true))

                    composeTestRule.onNodeWithText(matchTitle, substring = true).performTouchInput {
                        click(position = topCenter)
                    }

                    composeTestRule.waitForIdle()
                    Thread.sleep(2000) // Biztos, ami biztos, több idő a részleteknek

                    // 2. KERET KIJELÖLÉSE (4 játékos)
                    for (i in 0 until 4) {
                        composeTestRule.waitUntilAtLeastOneExists(hasText("Betesz"), timeoutMillis = 5000)

                        composeTestRule.onAllNodesWithText("Betesz")[0]
                            .performScrollTo()
                            .performClick()

                        // --- ITT VAN AZ ÚJ SZÜNET ---
                        // A kattintás után várunk, hogy az app biztosan feldolgozza a "Kivesz" állapotot
                        composeTestRule.waitForIdle()
                        Thread.sleep(1500)
                    }

                    // 3. VÉGLEGESÍTÉS
                    val finalizeButtonText = "Keret véglegesítése és Indítás (4 fő)"
                    composeTestRule.waitUntilAtLeastOneExists(hasText(finalizeButtonText), timeoutMillis = 5000)

                    composeTestRule.onNodeWithText(finalizeButtonText)
                        .performScrollTo()
                        .performClick()

                    composeTestRule.waitForIdle()
                    Thread.sleep(2000)

                    // 4. VISSZALÉPÉS
                    navigate(composeTestRule, "Vissza", Navigation.OnNodeWith.DESCRIPTION)
                    composeTestRule.waitForIdle()
                    Thread.sleep(1500)
                }
                logout(composeTestRule)
            }
        }
    }
}