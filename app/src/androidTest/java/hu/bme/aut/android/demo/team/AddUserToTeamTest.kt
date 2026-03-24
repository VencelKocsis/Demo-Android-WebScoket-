package hu.bme.aut.android.demo.team

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import hu.bme.aut.android.demo.MainActivity
import hu.bme.aut.android.demo.data.TestData.teams
import hu.bme.aut.android.demo.functions.Authentication.login
import hu.bme.aut.android.demo.functions.Authentication.logout
import hu.bme.aut.android.demo.functions.Navigation
import hu.bme.aut.android.demo.functions.Navigation.navigate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddUserToTeamTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captainBuildsTeam() {

        // 1. Végigmegyünk a csapatokon
        teams.forEach { team ->

            // BEJELENTKEZÉS az adott csapat kapitányával
            login(composeTestRule, team.captain.email, team.captain.password)

            // NAVIGÁCIÓ CSAPAT KÉPERNYŐRE
            navigate(composeTestRule, "Csapat", Navigation.OnNodeWith.TEXT)

            // --- BIZTONSÁGI VÁRAKOZÁS ---
            composeTestRule.waitForIdle()
            Thread.sleep(2000)

            // BELÉPÉS A CSAPATSZERKESZTŐBE
            navigate(composeTestRule, "Csapat szerkesztése", Navigation.OnNodeWith.DESCRIPTION)

            // --- BIZTONSÁGI VÁRAKOZÁS 2 ---
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
            Thread.sleep(1000)

            // KIJELENTKEZÉS
            logout(composeTestRule)
        }
    }
}