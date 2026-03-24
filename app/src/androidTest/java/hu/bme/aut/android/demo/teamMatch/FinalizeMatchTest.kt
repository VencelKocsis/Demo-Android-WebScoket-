package hu.bme.aut.android.demo.teamMatch

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
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

        // BEAC I.
        TeamData(
            id = 13, // <- Adatbázis ID
            captain = CaptainTestUser("NZ@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Nagy Zoltán", "nz@test.com", "bestpassword"),
                FullTestUser("Szarvas Tamás", "szt@test.com", "bestpassword"),
                FullTestUser("Bíró Csaba", "bcs@test.com", "bestpassword"),
                FullTestUser("Halász Gábor", "hg@test.com", "bestpassword"),
            ),
            teamName = "BEAC I."
        ),

        // BEAC II.
        TeamData(
            id = 14,
            captain = CaptainTestUser("BG@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Botlik Gergő", "bg@test.com", "bestpassword"),
                FullTestUser("Nagy Tamás", "nt@test.com", "bestpassword"),
                FullTestUser("Váczi Attila", "vam@test.com", "bestpassword"),
                FullTestUser("Simon Dániel", "sd@test.com", "bestpassword"),
            ),
            teamName = "BEAC II."
        ),

        // BEAC III.
        TeamData(
            id = 5,
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
            id = 6,
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
            id = 7,
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
            id = 8,
            captain = CaptainTestUser("kp@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Sipos Tamás", "st@test.com", "bestpassword"),
                FullTestUser("Tengerdi Tibor", "tt@test.com", "bestpassword"),
                FullTestUser("Böröcz Botond", "bb@test.com", "bestpassword"),
                FullTestUser("Reszler Balázs", "rb@test.com", "bestpassword")
            ),
            teamName = "BEAC VI."
        ),

        // MAFC I.
        TeamData(
            id = 15,
            captain = CaptainTestUser("NA@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Nagy Anna", "na@test.com", "bestpassword"),
                FullTestUser("Kovács Bence", "kb@test.com", "bestpassword"),
                FullTestUser("Tóth Dávid", "td@test.com", "bestpassword"),
                FullTestUser("Szabó Eszter", "sze@test.com", "bestpassword"),
            ),
            teamName = "MAFC I."
        ),

        // MAFC II.
        TeamData(
            id = 16,
            captain = CaptainTestUser("HGA@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Horváth Gábor", "hga@test.com", "bestpassword"),
                FullTestUser("Varga Zita", "vz@test.com", "bestpassword"),
                FullTestUser("Kocsis Károly", "kk@test.com", "bestpassword"),
                FullTestUser("Molnár Orsolya", "mo@test.com", "bestpassword"),
            ),
            teamName = "MAFC II."
        ),

        // MAFC III.
        TeamData(
            id = 17,
            captain = CaptainTestUser("NB@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Németh Balázs", "nb@test.com", "bestpassword"),
                FullTestUser("Farkas Kinga", "fk@test.com", "bestpassword"),
                FullTestUser("Balogh Tamás", "bt@test.com", "bestpassword"),
                FullTestUser("Papp Judit", "pj@test.com", "bestpassword"),
            ),
            teamName = "MAFC III."
        ),
        // MAFC IV.
        TeamData(
            id = 18,
            captain = CaptainTestUser("TL@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Takács László", "tl@test.com", "bestpassword"),
                FullTestUser("Juhász Dóra", "jd@test.com", "bestpassword"),
                FullTestUser("Mészáros Zoltán", "mz@test.com", "bestpassword"),
                FullTestUser("Simon Réka", "sr@test.com", "bestpassword"),
            ),
            teamName = "MAFC IV."
        ),
        // MAFC V.
        TeamData(
            id = 19,
            captain = CaptainTestUser("FM@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Fekete Miklós", "fm@test.com", "bestpassword"),
                FullTestUser("Szilágyi Tímea", "szti@test.com", "bestpassword"),
                FullTestUser("Török Gergely", "tg@test.com", "bestpassword"),
                FullTestUser("Fehér Andrea", "fa@test.com", "bestpassword"),
            ),
            teamName = "MAFC V."
        ),
        // MAFC VI.
        TeamData(
            id = 20,
            captain = CaptainTestUser("GD@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Gál Dániel", "gd@test.com", "bestpassword"),
                FullTestUser("Hegedűs Katalin", "hk@test.com", "bestpassword"),
                FullTestUser("Sipos Márton", "sm@test.com", "bestpassword"),
                FullTestUser("Lukács Boglárka", "lb@test.com", "bestpassword")
            ),
            teamName = "MAFC VI."
        )
    )

    val teamMatches = listOf(
        // --- BEAC I. (teams[0], ID: 13) hazai meccsei ---
        TeamMatchData(id = 35, homeTeam = teams[0], guestTeam = teams[6]),  // vs MAFC I.
        TeamMatchData(id = 36, homeTeam = teams[0], guestTeam = teams[7]),  // vs MAFC II.
        TeamMatchData(id = 37, homeTeam = teams[0], guestTeam = teams[8]),  // vs MAFC III.
        TeamMatchData(id = 38, homeTeam = teams[0], guestTeam = teams[9]),  // vs MAFC IV.
        TeamMatchData(id = 39, homeTeam = teams[0], guestTeam = teams[10]), // vs MAFC V.
        TeamMatchData(id = 40, homeTeam = teams[0], guestTeam = teams[11]), // vs MAFC VI.

        // --- BEAC II. (teams[1], ID: 14) hazai meccsei ---
        TeamMatchData(id = 41, homeTeam = teams[1], guestTeam = teams[6]),
        TeamMatchData(id = 42, homeTeam = teams[1], guestTeam = teams[7]),
        TeamMatchData(id = 43, homeTeam = teams[1], guestTeam = teams[8]),
        TeamMatchData(id = 44, homeTeam = teams[1], guestTeam = teams[9]),
        TeamMatchData(id = 45, homeTeam = teams[1], guestTeam = teams[10]),
        TeamMatchData(id = 46, homeTeam = teams[1], guestTeam = teams[11]),

        // --- BEAC III. (teams[2], ID: 5) hazai meccsei ---
        TeamMatchData(id = 23, homeTeam = teams[2], guestTeam = teams[6]),
        TeamMatchData(id = 24, homeTeam = teams[2], guestTeam = teams[7]),
        TeamMatchData(id = 25, homeTeam = teams[2], guestTeam = teams[8]),
        TeamMatchData(id = 26, homeTeam = teams[2], guestTeam = teams[9]),
        TeamMatchData(id = 27, homeTeam = teams[2], guestTeam = teams[10]),
        TeamMatchData(id = 28, homeTeam = teams[2], guestTeam = teams[11]),

        // --- BEAC IV. (teams[3], ID: 6) hazai meccsei ---
        TeamMatchData(id = 29, homeTeam = teams[3], guestTeam = teams[6]),
        TeamMatchData(id = 30, homeTeam = teams[3], guestTeam = teams[7]),
        TeamMatchData(id = 31, homeTeam = teams[3], guestTeam = teams[8]),
        TeamMatchData(id = 32, homeTeam = teams[3], guestTeam = teams[9]),
        TeamMatchData(id = 33, homeTeam = teams[3], guestTeam = teams[10]),
        TeamMatchData(id = 34, homeTeam = teams[3], guestTeam = teams[11]),

        // --- BEAC V. (teams[4], ID: 7) hazai meccsei ---
        TeamMatchData(id = 11, homeTeam = teams[4], guestTeam = teams[6]),
        TeamMatchData(id = 12, homeTeam = teams[4], guestTeam = teams[7]),
        TeamMatchData(id = 13, homeTeam = teams[4], guestTeam = teams[8]),
        TeamMatchData(id = 14, homeTeam = teams[4], guestTeam = teams[9]),
        TeamMatchData(id = 15, homeTeam = teams[4], guestTeam = teams[10]),
        TeamMatchData(id = 16, homeTeam = teams[4], guestTeam = teams[11]),

        // --- BEAC VI. (teams[5], ID: 8) hazai meccsei ---
        TeamMatchData(id = 17, homeTeam = teams[5], guestTeam = teams[6]),
        TeamMatchData(id = 18, homeTeam = teams[5], guestTeam = teams[7]),
        TeamMatchData(id = 19, homeTeam = teams[5], guestTeam = teams[8]),
        TeamMatchData(id = 20, homeTeam = teams[5], guestTeam = teams[9]),
        TeamMatchData(id = 21, homeTeam = teams[5], guestTeam = teams[10]),
        TeamMatchData(id = 22, homeTeam = teams[5], guestTeam = teams[11])
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
                Thread.sleep(2500)

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
                    Thread.sleep(2000)

                    // 2. KERET KIJELÖLÉSE (4 játékos)
                    for (i in 0 until 4) {
                        composeTestRule.waitUntilAtLeastOneExists(hasText("Betesz"), timeoutMillis = 5000)

                        composeTestRule.onAllNodesWithContentDescription("Betesz")[0]
                            .performScrollTo()
                            .performClick()

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