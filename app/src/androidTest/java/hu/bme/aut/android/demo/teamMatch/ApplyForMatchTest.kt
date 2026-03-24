package hu.bme.aut.android.demo.teamMatch

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import hu.bme.aut.android.demo.MainActivity
import hu.bme.aut.android.demo.data.TestData.teamMatches
import hu.bme.aut.android.demo.data.TestData.teams
import hu.bme.aut.android.demo.functions.Authentication.login
import hu.bme.aut.android.demo.functions.Authentication.logout
import hu.bme.aut.android.demo.functions.Match.applyIfPossible
import hu.bme.aut.android.demo.functions.Match.goBack
import hu.bme.aut.android.demo.functions.Match.openMatch
import hu.bme.aut.android.demo.functions.Match.waitForMatchList
import hu.bme.aut.android.demo.functions.Navigation
import hu.bme.aut.android.demo.functions.Navigation.navigate
import org.junit.Rule
import org.junit.Test

class ApplyForMatchTest {
    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun applyForMatches() {

        teams.forEach { team ->

            team.members.forEach { user ->

                // 1. BEJELENTKEZÉS
                login(composeTestRule, user.email, user.password)

                navigate(
                    composeTestRule,
                    "Bajnokság",
                    Navigation.OnNodeWith.TEXT
                )

                waitForMatchList(composeTestRule)

                val myMatches = teamMatches.filter {
                    it.homeTeam.teamName == team.teamName ||
                            it.guestTeam.teamName == team.teamName
                }

                // 2. MECCSEK VÉGIGKATTINTÁSA
                myMatches.forEach { match ->
                    // Csak az ID kell a megnyitáshoz!
                    openMatch(composeTestRule, match.id)

                    // Jelentkezés a kiszervezett, stabil függvénnyel
                    applyIfPossible(composeTestRule)

                    // Visszalépés
                    goBack(composeTestRule)
                }

                // --- 3. LÉPÉS: BIZTONSÁGOS KIJELENTKEZÉSI FOLYAMAT ---
                try {
                    // Átnavigálunk a Profil fülre
                    navigate(
                        composeTestRule,
                        "Profil",
                        Navigation.OnNodeWith.TEXT
                    )
                    composeTestRule.waitForIdle()
                    Thread.sleep(1000)

                    // Kijelentkezés a Profil képernyőről
                    logout(composeTestRule)
                } catch (e: Exception) {
                    println("Figyelem: A kijelentkezés megszakadt. Valószínűleg az app már automatikusan a Login képernyőre lépett.")
                }

                try {
                    composeTestRule.waitUntilAtLeastOneExists(
                        hasText("E-mail cím", substring = true, ignoreCase = true),
                        timeoutMillis = 10000
                    )
                } catch (e: Exception) {
                    throw AssertionError("A teszt teljesen elakadt: Nem találom a bejelentkező képernyőt a következő felhasználóhoz!")
                }
                composeTestRule.waitForIdle()
                Thread.sleep(1000)
            }
        }
    }
}