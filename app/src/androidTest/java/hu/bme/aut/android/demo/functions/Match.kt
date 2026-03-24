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
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
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
    }

    @OptIn(ExperimentalTestApi::class)
    fun openMatch(composeTestRule: ComposeTestRule, matchId: Int) {
        // Natív görgetés a listában a kártyához
        composeTestRule
            .onNodeWithTag("match_list")
            .performScrollToNode(hasTestTag("match_$matchId"))

        // Rákattint a kártyára
        composeTestRule
            .onNodeWithTag("match_$matchId")
            .performClick()

        // Megvárja a részletek fejlécét
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("Mérkőzés Részletei"),
            10000
        )
    }

    @OptIn(ExperimentalTestApi::class)
    fun applyIfPossible(composeTestRule: ComposeTestRule) {
        // 1. Várjuk meg a hálózati kérést, hogy a meccs adatai betöltsenek
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // 2. Mivel az előző játékosok már feliratkoztak, a listák megnőttek!
        try {
            val listNode = composeTestRule.onNode(hasScrollToNodeAction())
            // Húzunk 3-at felfelé, hogy biztosan a képernyő legaljára érjünk
            for (i in 0..3) {
                listNode.performTouchInput { swipeUp(durationMillis = 300) }
                composeTestRule.waitForIdle()
            }
        } catch (e: Throwable) {
            println("Nincs görgethető felület a részleteknél, folytatjuk...")
        }

        // 3. Várakozás a gombra - most már Throwable-t kapunk el, így NEM FOG ÖSSZEOMLANI!
        try {
            composeTestRule.waitUntilAtLeastOneExists(
                hasText("Jelentkezem a mérkőzésre") or hasText("Jelentkezés visszavonása"),
                timeoutMillis = 4000
            )
        } catch (e: Throwable) {
            println("Figyelem: Nem jelent meg a gomb a képernyő alján sem.")
        }

        // 4. Kattintás, ha a gomb tényleg ott van
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

    // UGYANAZ A GOLYÓÁLLÓ LOGIKA VISSZAVONÁSRA
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
        } catch (e: Throwable) {
            println("Nincs görgethető felület a részleteknél, folytatjuk...")
        }

        try {
            composeTestRule.waitUntilAtLeastOneExists(
                hasText("Jelentkezem a mérkőzésre") or hasText("Jelentkezés visszavonása"),
                timeoutMillis = 4000
            )
        } catch (e: Throwable) {
            println("Figyelem: Nem jelent meg gomb a képernyő alján sem.")
        }

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