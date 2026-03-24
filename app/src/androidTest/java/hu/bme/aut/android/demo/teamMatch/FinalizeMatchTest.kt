package hu.bme.aut.android.demo.teamMatch

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import hu.bme.aut.android.demo.MainActivity
import hu.bme.aut.android.demo.data.TestData.teamMatches
import hu.bme.aut.android.demo.data.TestData.teams
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