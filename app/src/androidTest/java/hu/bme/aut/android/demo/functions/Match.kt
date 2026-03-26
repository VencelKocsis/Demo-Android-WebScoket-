package hu.bme.aut.android.demo.functions

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp

object Match {

    fun goBack(composeTestRule: ComposeTestRule) {
        Navigation.navigate(
            composeTestRule,
            "Visszalépés",
            Navigation.OnNodeWith.DESCRIPTION
        )
        waitForMatchList(composeTestRule)
    }

    @OptIn(ExperimentalTestApi::class)
    fun waitForMatchList(composeTestRule: ComposeTestRule) {
        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag("match_list"),
            15000
        )
        composeTestRule.waitForIdle()

        Thread.sleep(3000)

        try {
            composeTestRule.onNodeWithTag("match_list").performTouchInput {
                swipeDown(durationMillis = 500)
            }
            composeTestRule.waitForIdle()
            Thread.sleep(2000)
        } catch (e: Throwable) {
            println("A lista nem frissíthető kézzel, vagy már be volt töltve.")
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun openMatch(composeTestRule: ComposeTestRule, matchId: Int) {
        composeTestRule
            .onNodeWithTag("match_list")
            .performScrollToNode(hasTestTag("match_$matchId"))

        composeTestRule
            .onNodeWithTag("match_$matchId")
            .performClick()

        composeTestRule.waitUntilAtLeastOneExists(
            hasText("Mérkőzés Részletei"),
            10000
        )
    }

    // --- AZ OKOS GÖRGETŐ ÉS KATTINTÓ FÜGGVÉNY ---
    @OptIn(ExperimentalTestApi::class)
    fun scrollAndClickText(composeTestRule: ComposeTestRule, text: String, errorMessage: String) {
        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // 1. Gyors kísérlet: ha a Compose natívan meg tudja találni
        try {
            val node = composeTestRule.onNodeWithText(text, substring = true, ignoreCase = true)
            node.performScrollTo()
            node.performClick()
            composeTestRule.waitForIdle()
            return // Sikerült, kilépünk!
        } catch (e: Throwable) {}

        // 2. Erőteljes görgetés lefelé
        var nodeFound = false
        for (i in 0..15) {
            val nodes = composeTestRule.onAllNodesWithText(text, substring = true, ignoreCase = true)
            if (nodes.fetchSemanticsNodes().isNotEmpty()) {
                nodeFound = true
                // Odagörgetünk a gombhoz
                try { nodes[0].performScrollTo() } catch (e: Throwable) {}

                // RÁKATTINTUNK! Nincs try-catch!
                // Ha a gomb le van tiltva, itt fog beszédes hibával elszállni!
                nodes[0].performClick()
                break
            } else {
                try {
                    composeTestRule.onNode(hasScrollToNodeAction()).performTouchInput { swipeUp(durationMillis = 300) }
                    composeTestRule.waitForIdle()
                } catch (scrollEx: Throwable) {}
            }
        }

        if (!nodeFound) {
            throw AssertionError(errorMessage)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun applyIfPossible(composeTestRule: ComposeTestRule) {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        try {
            val listNode = composeTestRule.onNode(hasScrollToNodeAction())
            for (i in 0..3) {
                listNode.performTouchInput { swipeUp(durationMillis = 300) }
                composeTestRule.waitForIdle()
            }
        } catch (e: Throwable) {}

        try {
            composeTestRule.waitUntilAtLeastOneExists(
                hasText("Jelentkezem a mérkőzésre") or hasText("Jelentkezés visszavonása"),
                timeoutMillis = 4000
            )
        } catch (e: Throwable) {}

        val exists = composeTestRule
            .onAllNodesWithText("Jelentkezem a mérkőzésre")
            .fetchSemanticsNodes()
            .isNotEmpty()

        if (exists) {
            composeTestRule
                .onNodeWithText("Jelentkezem a mérkőzésre")
                .performClick()

            composeTestRule.waitForIdle()
            Thread.sleep(1500)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun cancelIfPossible(composeTestRule: ComposeTestRule) {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        try {
            val listNode = composeTestRule.onNode(hasScrollToNodeAction())
            for (i in 0..3) {
                listNode.performTouchInput { swipeUp(durationMillis = 300) }
                composeTestRule.waitForIdle()
            }
        } catch (e: Throwable) {}

        try {
            composeTestRule.waitUntilAtLeastOneExists(
                hasText("Jelentkezem a mérkőzésre") or hasText("Jelentkezés visszavonása"),
                timeoutMillis = 4000
            )
        } catch (e: Throwable) {}

        val exists = composeTestRule
            .onAllNodesWithText("Jelentkezés visszavonása")
            .fetchSemanticsNodes()
            .isNotEmpty()

        if (exists) {
            composeTestRule
                .onNodeWithText("Jelentkezés visszavonása")
                .performClick()

            composeTestRule.waitForIdle()
            Thread.sleep(1500)
        }
    }
}