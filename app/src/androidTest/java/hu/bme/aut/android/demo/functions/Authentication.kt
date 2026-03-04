package hu.bme.aut.android.demo.functions

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import hu.bme.aut.android.demo.functions.Navigation.navigate

@OptIn(ExperimentalTestApi::class)
object Authentication {

    fun login(composeTestRule: ComposeTestRule, email: String, password: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasText("E-mail cím"), 10000)
        composeTestRule.onNodeWithText("E-mail cím").performTextClearance()
        composeTestRule.onNodeWithText("E-mail cím").performTextInput(email)

        composeTestRule.onNodeWithText("Jelszó").performTextClearance()
        composeTestRule.onNodeWithText("Jelszó").performTextInput(password)

        // 1. KIKAPCSOLJUK AZ AUTOMATIKUS VÁRAKOZÁST
        composeTestRule.mainClock.autoAdvance = false

        // 2. KATTINTUNK A GOMBRA
        composeTestRule.onNodeWithText("Bejelentkezés").performClick()

        // 3. MANUÁLISAN PÖRGETJÜK AZ IDŐT (képkockánként), amíg be nem tölt a főképernyő
        // (Ahol már látszik a "Csapat" felirat)
        var timeElapsed = 0L
        while (composeTestRule.onAllNodesWithText("Csapat").fetchSemanticsNodes().isEmpty() && timeElapsed < 10000) {
            composeTestRule.mainClock.advanceTimeByFrame()
            timeElapsed += 16 // Egy képkocka ~16 milliszekundum
        }

        // 4. VISSZAKAPCSOLJUK AZ AUTOMATIKÁT
        composeTestRule.mainClock.autoAdvance = true
    }

    fun logout(composeTestRule: ComposeTestRule) {
        navigate(composeTestRule, "Profil", Navigation.OnNodeWith.TEXT)

        composeTestRule.waitUntilAtLeastOneExists(hasContentDescription("Menü"), 10000)
        composeTestRule.onNodeWithContentDescription("Menü").performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasText("Kijelentkezés"), 5000)
        composeTestRule.onNodeWithText("Kijelentkezés").performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasText("E-mail cím"), 10000)
        composeTestRule.waitForIdle()
    }
}