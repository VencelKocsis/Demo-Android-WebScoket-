package hu.bme.aut.android.demo.registration

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import hu.bme.aut.android.demo.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationAutomatedTest {

    data class TestUser(
        val firstName: String,
        val lastName: String,
        val email: String,
        val password: String
    )

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    // Ez a szabály elindítja a MainActivity-t a teszteléshez
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun createMultipleTestUsers() {
        // Hozzunk létre 3 teszt felhasználót a MAFC II csapatba
        val testUsers = listOf(
            // BEAC I.


            // BEAC II.


            // BEAC III. in DB
            TestUser("Ferenc", "Katus", "KF@test.com", "bestpassword"),
            TestUser("Fanni", "Takáts", "TF@test.com", "bestpassword"),
            TestUser("Miklós", "Szabó", "SZM@test.com", "bestpassword"),
            TestUser("Péter", "Szekulesz", "SZP@test.com", "bestpassword"),


            // BEAC IV. in DB
            TestUser("Zsombor", "Nagy", "NZS@test.com", "bestpassword"),
            TestUser("Gergő", "Széles", "SZG@test.com", "bestpassword"),
            TestUser("Áron", "Gulyás", "GA@test.com", "bestpassword"),
            TestUser("Norbert", "Gábor", "GN@test.com", "bestpassword"),
            TestUser("Győző", "Szabó", "SZGY@test.com", "bestpassword"),

            // BEAC V. in DB
            TestUser("Dániel", "Molnár", "MD@test.com", "bestpassword"),
            TestUser("Dániel", "Illés", "ID@test.com", "bestpassword"),
            TestUser("Vilmos", "Módos", "MV@test.com", "bestpassword"),
            TestUser("Zsolt", "Tamás", "TZS@test.com", "bestpassword"),
            TestUser("Máté", "Kurucz", "KM@test.com", "bestpassword"),
            TestUser("Péter", "Juhász", "JP@test.com", "bestpassword"),
            TestUser("Gábor", "Wiener", "WG@test.com", "bestpassword"),
            TestUser("Vencel", "Kocsis", "KV@test.com", "bestpassword"),

            // BEAC VI. in DB
            TestUser("Pál", "Kolumbál", "KP@test.com", "bestpassword"),
            TestUser("Tamás", "Sipos", "ST@test.com", "bestpassword"),
            TestUser("Tibor", "Tengerdi", "TT@test.com", "bestpassword"),
            TestUser("Botond", "Böröcz", "BB@test.com", "bestpassword"),
            TestUser("Balázs", "Reszler", "RB@test.com", "bestpassword")


            //TestUser("firstName", "lastName", "email@test.com", "bestpassword")
        )

        testUsers.forEach { user ->
            // 1. Várakozás a lassú emulátorra! (Maximum 10 másodpercet vár, amíg megjelenik az E-mail mező)
            composeTestRule.waitUntilAtLeastOneExists(hasText("E-mail cím"), timeoutMillis = 10000)

            // --- 2. REGISZTRÁCIÓ ---
            // FIGYELEM: "E-mail cím", pontosan úgy, ahogy a LoginScreen.kt-ban van!
            composeTestRule.onNodeWithText("E-mail cím").performTextClearance()
            composeTestRule.onNodeWithText("E-mail cím").performTextInput(user.email)

            composeTestRule.onNodeWithText("Jelszó").performTextClearance()
            composeTestRule.onNodeWithText("Jelszó").performTextInput(user.password)

            // Kattintás a Regisztráció gombra
            composeTestRule.onNodeWithText("Regisztráció").performClick()

            // Itt kell a legtöbbet várni, mert a Firebase-hez kimegy a kérés, majd a Ktorhoz is.
            Thread.sleep(5000) // Felemeltem 5 másodpercre a lassú emulátor miatt

            // --- 3. NAVIGÁCIÓ A PROFILRA ---
            // Várunk, amíg a Profil gomb megjelenik az alsó menüben
            composeTestRule.waitUntilAtLeastOneExists(hasText("Profil"), timeoutMillis = 10000)
            composeTestRule.onNodeWithText("Profil").performClick()

            Thread.sleep(1000)

            // --- 4. PROFIL SZERKESZTÉSE ---
            // A 3 pont (IconButton) a TopAppBar-ban
            composeTestRule.onNodeWithContentDescription("Menü").performClick()

            // Lenyíló menü elem
            composeTestRule.waitUntilAtLeastOneExists(hasText("Profil szerkesztése"), timeoutMillis = 5000)
            composeTestRule.onNodeWithText("Profil szerkesztése").performClick()

            Thread.sleep(1000) // Várjuk meg, amíg az AlertDialog animációja befejeződik

            // Név kitöltése
            composeTestRule.onNodeWithText("Vezetéknév").performTextClearance()
            composeTestRule.onNodeWithText("Vezetéknév").performTextInput(user.lastName)

            composeTestRule.onNodeWithText("Keresztnév").performTextClearance()
            composeTestRule.onNodeWithText("Keresztnév").performTextInput(user.firstName)

            composeTestRule.onNodeWithText("Mentés").performClick()

            // Várakozás a Ktor PUT /auth/me hívásra
            Thread.sleep(3000)

            // --- 5. KIJELENTKEZÉS ---
            composeTestRule.onNodeWithContentDescription("Menü").performClick()

            composeTestRule.waitUntilAtLeastOneExists(hasText("Kijelentkezés"), timeoutMillis = 5000)
            composeTestRule.onNodeWithText("Kijelentkezés").performClick()

            // Hagyunk időt, hogy visszatöltsön a Login képernyő
            Thread.sleep(2000)
        }
    }
}