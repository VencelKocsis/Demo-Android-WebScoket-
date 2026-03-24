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
import hu.bme.aut.android.demo.data.TestData.teams
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