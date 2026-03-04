package hu.bme.aut.android.demo.functions

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

@OptIn(ExperimentalTestApi::class)
object Navigation {

    enum class OnNodeWith {
        TEXT, DESCRIPTION
    }

    fun navigate(
        composeTestRule: ComposeTestRule,
        destination: String,
        onNodeWith: OnNodeWith,
        expectedNextScreenText: String? = null // Opcionális: Várakozás a célképernyőre
    ) {

        // 1. Dinamikusan döntjük el, mit keressünk a várakozáshoz
        val matcher = when (onNodeWith) {
            OnNodeWith.TEXT -> hasText(destination)
            OnNodeWith.DESCRIPTION -> hasContentDescription(destination)
        }

        // Várunk, amíg a kattintandó elem (gomb vagy ikon) meg nem jelenik
        composeTestRule.waitUntilAtLeastOneExists(matcher, 5000)

        // 2. Rákattintunk
        when (onNodeWith) {
            OnNodeWith.TEXT -> {
                composeTestRule.onNodeWithText(destination).performClick()
            }
            OnNodeWith.DESCRIPTION -> {
                composeTestRule.onNodeWithContentDescription(destination).performClick()
            }
        }

        // 3. Opcionális várakozás az ÚJ képernyőre
        // (Ha megadtad, hogy mit várjon a kattintás után, akkor megvárja. Ha nem, megy tovább.)
        if (expectedNextScreenText != null) {
            composeTestRule.waitUntilAtLeastOneExists(hasText(expectedNextScreenText), 10000)
        }
    }
}