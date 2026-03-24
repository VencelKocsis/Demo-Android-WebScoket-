package hu.bme.aut.android.demo.teamOperations

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import hu.bme.aut.android.demo.MainActivity
import hu.bme.aut.android.demo.data.DataStructure.CaptainTestUser
import hu.bme.aut.android.demo.data.DataStructure.FullTestUser
import hu.bme.aut.android.demo.data.DataStructure.RegularTestUser
import hu.bme.aut.android.demo.data.DataStructure.TeamData
import hu.bme.aut.android.demo.functions.Authentication.login
import hu.bme.aut.android.demo.functions.Authentication.logout
import hu.bme.aut.android.demo.functions.Navigation
import hu.bme.aut.android.demo.functions.Navigation.navigate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoveUserFromTeamTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    val teams = listOf(

        // --- ALL IN DB ---
        // BEAC I.
        TeamData(
            captain = CaptainTestUser("NZ@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Szarvas Tamás"),
                RegularTestUser("Bíró Csaba"),
                RegularTestUser("Halász Gábor")
            ),
            teamName = "BEAC I."
        ),

        // BEAC II.
        TeamData(
            captain = CaptainTestUser("BG@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Nagy Tamás"),
                RegularTestUser("Váczi Attila"),
                RegularTestUser("Simon Dániel")
            ),
            teamName = "BEAC II."
        ),

        // BEAC III.
        TeamData(
            captain = CaptainTestUser("tf@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Katus Ferenc"),
                RegularTestUser("Szabó Miklós"),
                RegularTestUser("Szekulesz Péter"),
                RegularTestUser("Bakos Bertalan")
            ),
            teamName = "BEAC III."
        ),

        // BEAC IV.
        TeamData(
            captain = CaptainTestUser("nzs@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Széles Gergő"),
                RegularTestUser("Gulyás Áron"),
                RegularTestUser("Gábor Norbert"),
                RegularTestUser("Szabó Győző")
            ),
            teamName = "BEAC IV."
        ),

        // BEAC V.
        TeamData(
            captain = CaptainTestUser("id@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Molnár Dániel"),
                RegularTestUser("Módos Vilmos"),
                RegularTestUser("Tamás Zsolt"),
                RegularTestUser("Kurucz Máté"),
                RegularTestUser("Juhász Péter"),
                RegularTestUser("Wiener Gábor"),
                RegularTestUser("Kocsis Vencel")
            ),
            teamName = "BEAC V."
        ),

        // BEAC VI.
        TeamData(
            captain = CaptainTestUser("kp@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Sipos Tamás"),
                RegularTestUser("Tengerdi Tibor"),
                RegularTestUser("Böröcz Botond"),
                RegularTestUser("Reszler Balázs")
            ),
            teamName = "BEAC VI."
        ),

        // MAFC I.
        TeamData(
            captain = CaptainTestUser("NA@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Kovács Bence"),
                RegularTestUser("Tóth Dávid"),
                RegularTestUser("Szabó Eszter")
            ),
            teamName = "MAFC I."
        ),

        // MAFC II.
        TeamData(
            captain = CaptainTestUser("HGA@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Varga Zita"),
                RegularTestUser("Kocsis Károly"),
                RegularTestUser("Molnár Orsolya")
            ),
            teamName = "MAFC II."
        ),

        // MAFC III.
        TeamData(
            captain = CaptainTestUser("NB@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Farkas Kinga"),
                RegularTestUser("Balogh Tamás"),
                RegularTestUser("Papp Judit")
            ),
            teamName = "MAFC III."
        ),
        // MAFC IV.
        TeamData(
            captain = CaptainTestUser("TL@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Juhász Dóra"),
                RegularTestUser("Mészáros Zoltán"),
                RegularTestUser("Simon Réka")
            ),
            teamName = "MAFC IV."
        ),
        // MAFC V.
        TeamData(
            captain = CaptainTestUser("FM@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Szilágyi Tímea"),
                RegularTestUser("Török Gergely"),
                RegularTestUser("Fehér Andrea"),
            ),
            teamName = "MAFC V."
        ),
        // MAFC VI.
        TeamData(
            captain = CaptainTestUser("GD@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Hegedűs Katalin"),
                RegularTestUser("Sipos Márton"),
                RegularTestUser("Lukács Boglárka")
            ),
            teamName = "MAFC VI."
        )
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captainDestroyTeam() {

        teams.forEach { team ->

            login(composeTestRule, team.captain.email, team.captain.password)

            navigate(composeTestRule, "Csapat", Navigation.OnNodeWith.TEXT)

            // --- VÁRAKOZÁS AZ ADATOK BETÖLTÉSÉRE ---
            composeTestRule.waitForIdle()
            Thread.sleep(2000)

            navigate(composeTestRule, "Csapat szerkesztése", Navigation.OnNodeWith.DESCRIPTION)

            // --- VÁRAKOZÁS A LISTA FELÉPÜLÉSÉRE ---
            composeTestRule.waitForIdle()
            Thread.sleep(1000)

            team.members.forEach { user ->
                // 1. Megvárjuk az egyedi tag-gel ellátott törlés ikont
                composeTestRule.waitUntilAtLeastOneExists(hasTestTag("kick_${user.name}"), 5000)
                composeTestRule.onNodeWithTag("kick_${user.name}").performClick()

                // 2. Megvárjuk a megerősítő dialógust
                composeTestRule.waitUntilAtLeastOneExists(hasText("Tag eltávolítása"), 5000)
                composeTestRule.onNodeWithText("Eltávolítás").performClick()

                // --- VÁRAKOZÁS A TÖRLÉS SIKERESSÉGÉRE ---
                composeTestRule.waitForIdle()
                Thread.sleep(500)
            }

            navigate(composeTestRule, "Visszalépés", Navigation.OnNodeWith.DESCRIPTION)

            composeTestRule.waitForIdle()
            Thread.sleep(1000)

            logout(composeTestRule)
        }
    }
}