package hu.bme.aut.android.demo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import hu.bme.aut.android.demo.feature.auth.AuthState
import hu.bme.aut.android.demo.feature.list_players.DemoScreen
import hu.bme.aut.android.demo.feature.list_players.PlayersViewModel
import hu.bme.aut.android.demo.feature.auth.LoginScreen
import hu.bme.aut.android.demo.feature.auth.AuthViewModel
import hu.bme.aut.android.demo.feature.main.MainScreen

/**
 * Az alkalmazás fő navigációs konténere.
 * Kezeli az útvonalak közti váltást és a kezdőképernyő beállítását
 * a felhasználó bejelentkezési állapota alapján.
 *
 * @param navController A NavHostController, ami az aktuális képernyő állapotát kezeli.
 */
@Composable
fun AppNavHost(
    navController: NavHostController
) {
    // Az AuthViewModel Hilt általi beszerzése
    val authViewModel: AuthViewModel = hiltViewModel()

    // A bejelentkezési állapot figyelése lifecycle-aware módon
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // A kezdőképernyő eldöntése a hitelesítési állapot alapján
    val startDestination = when (authState) {
        // Amíg az állapot ismeretlen (pl. token ellenőrzés fut), a Login útvonallal indulunk.
        AuthState.UNKNOWN -> Screen.Login.route
        // Ha be van jelentkezve, a Players képernyővel (DemoScreen) indul.
        AuthState.AUTHENTICATED -> Screen.Main.route // TODO TEMP Players
        // Ha nincs bejelentkezve, a Login képernyővel indul.
        AuthState.UNAUTHENTICATED -> Screen.Login.route
    }

    // Amíg az állapot UNKNOWN (ismeretlen), nem renderelünk NavHost-ot, hogy elkerüljük a gyorsan váltó képernyőket.
    // Ha az állapot már AUTHENTICATED vagy UNAUTHENTICATED, elindul a NavHost.
    if (authState != AuthState.UNKNOWN) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // --- 1. Login Képernyő ---
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onAuthSuccess = {
                        // Navigáció a PlayersScreen-re bejelentkezés után,
                        // eltávolítva a LoginScreen-t a back stack-ből.
                        navController.navigate(Screen.Players.route) { // TODO to Main.route
                            popUpTo(Screen.Login.route) {
                                inclusive = true // Az LoginScreen-t is eltávolítja
                            }
                        }
                    }
                )
            }

            // --- HIÁNYZOTT: A Demo/Players Képernyő ---
            composable(Screen.Players.route) {
                // Itt kérjük le a ViewModelt, aminek hatására lefut az init blokk!
                val playersViewModel: PlayersViewModel = hiltViewModel()
                DemoScreen(
                    viewModel = playersViewModel,
                    onLogout = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Players.route) { inclusive = true }
                        }
                    }
                )
            }

            // --- 2. FőKépernyő (MainScreen) ---
            composable(Screen.Main.route) {
                MainScreen(
                    onLogout = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Main.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}
