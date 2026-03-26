package hu.bme.aut.android.demo.teamMatch

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
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

class FinalizeParticipantsTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun finalizeParticipants() {
        val match = TestData.teamMatches[0]
        val homeTeam = match.homeTeam
        val guestTeam = match.guestTeam

        ensureLoggedOut(composeTestRule)

        // ========================================================
        // 1. HAZAI KAPITÁNY: Játékosok kiválasztása
        // ========================================================
        login(composeTestRule, homeTeam.captain.email, homeTeam.captain.password)
        navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
        waitForMatchList(composeTestRule)
        openMatch(composeTestRule, match.id)

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Játékosok kiválasztása natív görgetéssel
        homeTeam.members.forEach { user ->
            val userTag = hasTestTag("toggle_${user.name}")
            try {
                composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(userTag)
            } catch (e: Throwable) {}
            composeTestRule.onNode(userTag).performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(500)
        }

        goBack(composeTestRule)
        logoutWithTryCatch(composeTestRule)

        // ========================================================
        // 2. VENDÉG KAPITÁNY: Kiválasztás, Meccs indítása, Felállás
        // ========================================================
        login(composeTestRule, guestTeam.captain.email, guestTeam.captain.password)
        navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
        waitForMatchList(composeTestRule)
        openMatch(composeTestRule, match.id)

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Vendég játékosok kiválasztása natív görgetéssel
        guestTeam.members.forEach { user ->
            val userTag = hasTestTag("toggle_${user.name}")
            try {
                composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(userTag)
            } catch (e: Throwable) {}
            composeTestRule.onNode(userTag).performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(500)
        }
    }
}