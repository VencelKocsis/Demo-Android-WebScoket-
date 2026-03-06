package hu.bme.aut.android.demo.teamOperation

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import hu.bme.aut.android.demo.MainActivity
import hu.bme.aut.android.demo.functions.Authentication.login
import hu.bme.aut.android.demo.functions.Authentication.logout
import hu.bme.aut.android.demo.functions.Navigation
import hu.bme.aut.android.demo.functions.Navigation.navigate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddUserToTeamTest {

    data class CaptainTestUser(
        val email: String,
        val password: String
    )

    data class RegularTestUser(
        val name: String
    )

    data class TeamData(
        val captain: CaptainTestUser,
        val members: List<RegularTestUser>
    )

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
                RegularTestUser("Katus Ferenc"),
                RegularTestUser("Szabó Miklós"),
                RegularTestUser("Szekulesz Péter"),
                RegularTestUser("Bakos Bertalan")
            )
        ),
        // BEAC IV.
        TeamData(
            captain = CaptainTestUser("nzs@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Széles Gergő"),
                RegularTestUser("Gulyás Áron"),
                RegularTestUser("Gábor Norbert"),
                RegularTestUser("Szabó Győző")
            )
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
            )
        ),
        // BEAC VI.
        TeamData(
            captain = CaptainTestUser("kp@test.com", "bestpassword"),
            members = listOf(
                RegularTestUser("Sipos Tamás"),
                RegularTestUser("Tengerdi Tibor"),
                RegularTestUser("Böröcz Botond"),
                RegularTestUser("Reszler Balázs")
            )
        )
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captainBuildsTeam() {

        // 1. Végigmegyünk a csapatokon
        teams.forEach { team ->

            // BEJELENTKEZÉS az adott csapat kapitányával
            login(composeTestRule, team.captain.email, team.captain.password)

            // NAVIGÁCIÓ CSAPAT KÉPERNYŐRE
            navigate(composeTestRule, "Csapat", Navigation.OnNodeWith.TEXT)

            // --- ÚJ BIZTONSÁGI VÁRAKOZÁS ---
            // Várunk 2 másodpercet, hogy a hálózati/adatbázis lekérdezés biztosan befejeződjön,
            // és a Csapat képernyő adatai feltöltődjenek.
            composeTestRule.waitForIdle()
            Thread.sleep(2000)

            // BELÉPÉS A CSAPATSZERKESZTŐBE
            navigate(composeTestRule, "Csapat szerkesztése", Navigation.OnNodeWith.DESCRIPTION)

            // --- ÚJ BIZTONSÁGI VÁRAKOZÁS 2 ---
            // Itt is adunk 1 másodpercet, hogy a lenyíló lista (Dropdown) adatai biztosan betöltsenek,
            // mielőtt elkezdenénk a játékosokon iterálni.
            composeTestRule.waitForIdle()
            Thread.sleep(1000)

            // 2. Csak a kapitányhoz tartozó játékosokon megyünk végig!
            team.members.forEach { user ->
                composeTestRule.onNodeWithText("Válassz szabad játékost...").performClick()
                composeTestRule.onNodeWithText(user.name).performClick()
                composeTestRule.onNodeWithText("Felvétel").performClick()
            }

            // NAVIGÁCIÓ VISSZA A CSAPATKÉPERNYŐRE
            navigate(composeTestRule, "Visszalépés", Navigation.OnNodeWith.DESCRIPTION)

            composeTestRule.waitForIdle()
            Thread.sleep(1000) // Biztonsági tartalék, amíg betölt a BottomBar

            // KIJELENTKEZÉS a csapatépítés végén, jöhet a következő kapitány
            logout(composeTestRule)
        }
    }
}