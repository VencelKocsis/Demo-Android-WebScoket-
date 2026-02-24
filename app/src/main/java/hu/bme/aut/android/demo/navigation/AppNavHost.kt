package hu.bme.aut.android.demo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import hu.bme.aut.android.demo.feature.auth.AuthState
import hu.bme.aut.android.demo.feature.list_players.DemoScreen
import hu.bme.aut.android.demo.feature.list_players.PlayersViewModel
import hu.bme.aut.android.demo.feature.auth.LoginScreen
import hu.bme.aut.android.demo.feature.auth.AuthViewModel
import hu.bme.aut.android.demo.feature.main.MainScreen

// --- 1. TÍPUSBIZTOS ÚTVONALAK DEFINIÁLÁSA ---
// Ez váltja le a régi String alapú "Screen.Login.route" megoldást.
sealed interface AppRoute {
    data object Login : AppRoute
    data object Players : AppRoute
    data object Main : AppRoute
}

// FIGYELEM: Már NEM kell átadni NavHostController paramétert!
@Composable
fun AppNavHost() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // --- 2. A NAVIGÁCIÓS BACK STACK (LISTA) ---
    // Az egész navigáció mindössze egy memóriában lévő lista!
    val backStack = remember { mutableStateListOf<AppRoute>() }

    // --- 3. KEZDŐÁLLAPOT BEÁLLÍTÁSA ---
    // Ha az AuthState betöltött, beállítjuk a lista első elemét.
    LaunchedEffect(authState) {
        if (backStack.isEmpty()) {
            when (authState) {
                AuthState.AUTHENTICATED -> backStack.add(AppRoute.Main)
                AuthState.UNAUTHENTICATED -> backStack.add(AppRoute.Login)
                AuthState.UNKNOWN -> { /* Splash screen jöhet ide később */ }
            }
        }
    }

    // Csak akkor indul a UI, ha a lista nem üres
    if (backStack.isNotEmpty()) {

        // --- 4. NAVDISPLAY RENDEREZÉSE ---
        NavDisplay(backStack = backStack,
            entryProvider = { route ->
                when (route) {
                    is AppRoute.Login -> NavEntry(route) {
                        LoginScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = {
                                // Navigáció: Csak lecseréljük a listát!
                                backStack.clear()
                                backStack.add(AppRoute.Main) // TODO temp Players
                            }
                        )
                    }

                    is AppRoute.Players -> NavEntry(route) {
                        // A hiltViewModel() tökéletesen működik a NavEntry-n belül is!
                        val playersViewModel: PlayersViewModel = hiltViewModel()
                        DemoScreen(
                            viewModel = playersViewModel,
                            onLogout = {
                                authViewModel.signOut()
                                backStack.clear()
                                backStack.add(AppRoute.Login)
                            }
                        )
                    }

                    is AppRoute.Main -> NavEntry(route) {
                        MainScreen (
                            onLogout = {
                                authViewModel.signOut()
                                backStack.clear()
                                backStack.add(AppRoute.Login)
                            }
                        )
                    }
                }
            }
        )
    }
}