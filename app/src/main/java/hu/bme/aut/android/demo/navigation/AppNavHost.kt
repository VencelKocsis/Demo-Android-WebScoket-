package hu.bme.aut.android.demo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import hu.bme.aut.android.demo.feature.auth.AuthState
import hu.bme.aut.android.demo.feature.list_players.DemoScreen
import hu.bme.aut.android.demo.feature.list_players.PlayersViewModel
import hu.bme.aut.android.demo.feature.auth.LoginScreen
import hu.bme.aut.android.demo.feature.auth.AuthViewModel
import hu.bme.aut.android.demo.feature.main.MainScreen
import hu.bme.aut.android.demo.feature.team.editor.TeamEditorScreen
import hu.bme.aut.android.demo.feature.team.TeamScreen
import hu.bme.aut.android.demo.feature.tournament.match.MatchDetailsScreen

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
        AuthState.AUTHENTICATED -> Screen.Main.route // TODO TEMP Players will be: Screen.Main.route
        // Ha nincs bejelentkezve, a Login képernyővel indul. // TODO 3 dot options: user gets notified of starting round before a week, and can customize time before
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
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true // Az LoginScreen-t is eltávolítja
                            }
                        }
                    }
                )
            }

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
            } // TODO new navigation implementation, on navigation3 branch (build failed because of gradle versions)

            // --- 2. FőKépernyő (MainScreen) ---
            composable(Screen.Main.route) {
                MainScreen(
                    authViewModel = authViewModel,
                    onLogout = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Main.route) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToTeamEditor = { teamId ->
                        navController.navigate(Screen.TeamEditor.createRoute(teamId))
                    },
                    onNavigateToMatchDetails = { matchId ->
                        navController.navigate(Screen.MatchDetails.createRoute(matchId))
                    }
                )
            }

            composable(Screen.Team.route) {
                TeamScreen(
                    onNavigateToEditor = { teamId ->
                        navController.navigate(Screen.TeamEditor.createRoute(teamId))
                    }
                )
            }

            // --- 3. Csapatszerkesztő Képernyő ---
            composable(
                route = "${Screen.TeamEditor.route}/{teamId}",
                arguments = listOf(
                    navArgument("teamId") { type = NavType.IntType } // Megmondjuk, hogy ez egy Int
                )
            ) {
                TeamEditorScreen(
                    onNavigateBack = {
                        navController.popBackStack() // Visszalépés az előző képernyőre
                    }
                )
            }

            // --- 4. ÚJ: Meccs Részletek Képernyő ---
            composable(
                route = "${Screen.MatchDetails.route}/{matchId}",
                arguments = listOf(
                    navArgument("matchId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getInt("matchId") ?: return@composable

                MatchDetailsScreen(
                    matchId = matchId,
                    onNavigateBack = {
                        navController.popBackStack() // Visszalépés a listához
                    }
                )
            }
        }
    }
}
