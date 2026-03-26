package hu.bme.aut.android.demo.functions

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

@OptIn(ExperimentalTestApi::class)
object Navigation {

    enum class OnNodeWith {
        TEXT, DESCRIPTION
    }

    fun navigate(composeTestRule: ComposeTestRule, target: String, by: OnNodeWith = OnNodeWith.TEXT) {
        if (by == OnNodeWith.TEXT) {
            val nodes = composeTestRule.onAllNodesWithText(target)
            val count = nodes.fetchSemanticsNodes().size

            if (count > 1) {
                // Ha több is van a képernyőn (pl. a fenti Cím és az alsó Tab is "Bajnokság"),
                // akkor az utolsóra kattintunk, mert az az alsó navigációs sáv!
                nodes[count - 1].performClick()
            } else if (count == 1) {
                nodes[0].performClick()
            }
        } else {
            val nodes = composeTestRule.onAllNodesWithContentDescription(target)
            val count = nodes.fetchSemanticsNodes().size

            if (count > 1) {
                nodes[count - 1].performClick()
            } else if (count == 1) {
                nodes[0].performClick()
            }
        }
        composeTestRule.waitForIdle()
    }
}