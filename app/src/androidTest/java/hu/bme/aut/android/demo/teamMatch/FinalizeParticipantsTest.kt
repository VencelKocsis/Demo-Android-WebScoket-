package hu.bme.aut.android.demo.teamMatch

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.rule.GrantPermissionRule
import hu.bme.aut.android.demo.MainActivity
import hu.bme.aut.android.demo.data.TestData
import hu.bme.aut.android.demo.functions.Authentication.ensureLoggedOut
import hu.bme.aut.android.demo.functions.Authentication.login
import hu.bme.aut.android.demo.functions.Authentication.logoutWithTryCatch
import hu.bme.aut.android.demo.functions.Match.goBack
import hu.bme.aut.android.demo.functions.Match.openMatch
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
    fun finalizeAllParticipants() {
        ensureLoggedOut(composeTestRule)

        // Végigmegyünk az ÖSSZES CSAPATON (és azok kapitányain)
        for (team in TestData.teams) {

            // Kigyűjtjük azokat a meccseket, ahol ez a csapat játszik
            val myMatches = TestData.teamMatches.filter { it.homeTeam.id == team.id || it.guestTeam.id == team.id }

            // Ha a csapatnak nincs meccse a tesztadatokban, átugorjuk
            if (myMatches.isEmpty()) continue

            println("=========================================================================")
            println("▶ KAPITÁNY BELÉP: ${team.teamName} (${team.captain.email}) - ${myMatches.size} meccs vár rá.")
            println("=========================================================================")

            // 1. Kapitány bejelentkezik
            login(composeTestRule, team.captain.email, team.captain.password)
            navigate(composeTestRule, "Bajnokság", Navigation.OnNodeWith.TEXT)
            waitForMatchList(composeTestRule)

            // 2. Végigmegy a saját meccsein
            for (match in myMatches) {
                openMatch(composeTestRule, match.id)
                composeTestRule.waitForIdle()
                Thread.sleep(1500)

                // Játékosok kiválasztása natív görgetéssel
                team.members.forEach { user ->
                    val userTag = hasTestTag("toggle_${user.name}")
                    try {
                        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(userTag)
                    } catch (e: Throwable) {}

                    try {
                        composeTestRule.onNode(userTag).performClick()
                    } catch (e: Throwable) {
                        println("Nem sikerült rákattintani a játékosra: ${user.name}")
                    }
                    composeTestRule.waitForIdle()
                    Thread.sleep(500)
                }

                // Visszalép a listába a következő meccs kiválasztásához
                goBack(composeTestRule)
            }

            // 3. Ha minden meccsével végzett, kilép
            logoutWithTryCatch(composeTestRule)
            Thread.sleep(1000)
        }

        println("🏆 AZ ÖSSZES CSAPAT JÁTÉKOSAI KIVÁLASZTVA! 🏆")
    }
}