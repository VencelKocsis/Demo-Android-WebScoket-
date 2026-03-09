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
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.rule.GrantPermissionRule
import hu.bme.aut.android.demo.MainActivity
import hu.bme.aut.android.demo.data.DataStructure.FullTestUser
import hu.bme.aut.android.demo.data.DataStructure.CaptainTestUser
import hu.bme.aut.android.demo.data.DataStructure.TeamData
import hu.bme.aut.android.demo.data.DataStructure.TeamMatchData
import hu.bme.aut.android.demo.functions.Authentication.login
import hu.bme.aut.android.demo.functions.Authentication.logout
import hu.bme.aut.android.demo.functions.Navigation
import hu.bme.aut.android.demo.functions.Navigation.navigate
import org.junit.Rule
import org.junit.Test

class CancelApplyForMatchTest {

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
        TeamMatchData(
            homeTeam = teams[0],
            guestTeam = teams[1]
        ),

        TeamMatchData(
            homeTeam = teams[2],
            guestTeam = teams[3]
        ),

        TeamMatchData(
            homeTeam = teams[0],
            guestTeam = teams[2]
        ),

        TeamMatchData(
            homeTeam = teams[1],
            guestTeam = teams[2]
        )
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancelMatchApplication() {
        teams.forEach { team ->
            team.members.forEach { user ->

                login(composeTestRule, user.email, user.password)

                navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)

                composeTestRule.waitForIdle()
                Thread.sleep(2000)

                val myMatches = teamMatches.filter { it.homeTeam == team || it.guestTeam == team }

                myMatches.forEach { match ->
                    val matchTitle = "${match.homeTeam.teamName} vs ${match.guestTeam.teamName}"

                    composeTestRule.onAllNodes(hasScrollToNodeAction())[0]
                        .performScrollToNode(hasText(matchTitle))

                    // Sima performClick() helyett a kártya legfelső pontjára (topCenter) kattintunk,
                    // ahol a meccs neve van, így elkerüljük a középen lévő Térkép gombot!
                    composeTestRule.onNodeWithText(matchTitle).performTouchInput {
                        click(position = topCenter)
                    }

                    composeTestRule.waitForIdle()
                    Thread.sleep(1500)

                    // --- JAVÍTOTT, OKOS VÁRAKOZÁS ---
                    // Megvárjuk, amíg VAGY a visszavonás, VAGY a jelentkezés gomb megjelenik.
                    composeTestRule.waitUntilAtLeastOneExists(
                        hasText("Jelentkezés visszavonása") or hasText("Jelentkezem a meccsre"),
                        timeoutMillis = 5000
                    )

                    // Ellenőrizzük, hogy a visszavonás gomb látszik-e
                    val isCancelButtonVisible = composeTestRule
                        .onAllNodesWithText("Jelentkezés visszavonása")
                        .fetchSemanticsNodes()
                        .isNotEmpty()

                    if (isCancelButtonVisible) {
                        // Ha már jelentkezett, akkor rákattintunk a visszavonásra
                        composeTestRule.onNodeWithText("Jelentkezés visszavonása")
                            .performClick()
                    } else {
                        // Ha még nem jelentkezett, akkor nincs mit visszavonni, megyünk tovább.
                        println("Nem volt aktív jelentkezés a meccsre: $matchTitle")
                    }

                    navigate(composeTestRule, "Vissza", Navigation.OnNodeWith.DESCRIPTION)

                    composeTestRule.waitForIdle()
                    Thread.sleep(1000)
                }

                logout(composeTestRule)
            }
        }
    }
}