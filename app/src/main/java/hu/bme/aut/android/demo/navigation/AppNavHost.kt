package hu.bme.aut.android.demo.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import hu.bme.aut.android.demo.feature.auth.AuthState
import hu.bme.aut.android.demo.feature.auth.LoginScreen
import hu.bme.aut.android.demo.feature.auth.AuthViewModel
import hu.bme.aut.android.demo.feature.leaderboard.LeaderboardScreen
import hu.bme.aut.android.demo.feature.main.MainScreen
import hu.bme.aut.android.demo.feature.profile.PlayerProfileScreen
import hu.bme.aut.android.demo.feature.equipment.RacketEditorScreen
import hu.bme.aut.android.demo.feature.team.editor.TeamEditorScreen
import hu.bme.aut.android.demo.feature.tournament.match.MatchDetailsScreen
import hu.bme.aut.android.demo.feature.tournament.liveMatch.LiveMatchScreen
import hu.bme.aut.android.demo.feature.tournament.scorer.MatchScorerScreen

/**
 * Az alkalmazás gyökér szintű navigációs fája (Root Graph).
 * * Csak két fő ága van: a [Login] (ha nincs bejelentkezve) és a [Main] (ha be van).
 * * Minden más képernyő a Main képernyőből nyílik meg.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Indulási pont meghatározása a hitelesítés állapota alapján
    val startDestination: Any = when (authState) {
        AuthState.UNKNOWN -> Login // Amíg tölt, a Loginon várunk
        AuthState.AUTHENTICATED -> Main
        AuthState.UNAUTHENTICATED -> Login
    }

    // Amíg az AuthState betöltésére várunk (UNKNOWN állapotban a SplashScreen után),
    // érdemes megvárni a tényleges értéket, hogy ne villanjon fel a rossz képernyő.
    if (authState != AuthState.UNKNOWN) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // --- 1. Login Képernyő ---
            composable<Login> {
                LoginScreen(
                    viewModel = authViewModel,
                    onAuthSuccess = {
                        navController.navigate(Main) {
                            popUpTo<Login> { inclusive = true } // Visszagombbal nem lehet visszajönni a Loginra
                        }
                    }
                )
            }

            // --- 2. FőKépernyő (Tartalmazza az alsó menüt) ---
            composable<Main> {
                MainScreen(
                    authViewModel = authViewModel,
                    onLogout = {
                        authViewModel.signOut()
                        navController.navigate(Login) {
                            popUpTo<Main> { inclusive = true }
                        }
                    },
                    onNavigateToTeamEditor = { teamId -> navController.navigate(TeamEditor(teamId)) },
                    onNavigateToMatchDetails = { matchId -> navController.navigate(MatchDetails(matchId)) },
                    onNavigateToPlayerProfile = { playerId -> navController.navigate(PlayerProfile(playerId)) },
                    onNavigateToRacketEditor = { racketId ->
                        navController.navigate(RacketEditor(racketId = racketId))
                    }
                )
            }

            // ==========================================
            // PARAMÉTERES (RÉSZLETEZŐ) KÉPERNYŐK
            // ==========================================

            composable<TeamEditor> { backStackEntry ->
                // A .toRoute() kinyeri a type-safe argumentumokat a navigációból
                val args = backStackEntry.toRoute<TeamEditor>()
                TeamEditorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<MatchDetails> { backStackEntry ->
                val args = backStackEntry.toRoute<MatchDetails>()
                MatchDetailsScreen(
                    matchId = args.matchId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLiveMatch = { navController.navigate(LiveMatch(args.matchId)) }
                )
            }

            composable<LiveMatch> { backStackEntry ->
                val args = backStackEntry.toRoute<LiveMatch>()
                LiveMatchScreen(
                    matchId = args.matchId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToScorer = { individualId ->
                        navController.navigate(MatchScorer(args.matchId, individualId))
                    }
                )
            }

            composable<MatchScorer> { backStackEntry ->
                val args = backStackEntry.toRoute<MatchScorer>()
                MatchScorerScreen(
                    matchId = args.matchId,
                    individualMatchId = args.individualMatchId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<PlayerProfile> { backStackEntry ->
                val args = backStackEntry.toRoute<PlayerProfile>()
                PlayerProfileScreen(
                    playerId = args.playerId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMarket = { navController.navigate(Market) }
                )
            }

            composable<Leaderboard> {
                LeaderboardScreen()
            }

            composable<RacketEditor> {
                // A ViewModel a SavedStateHandle-ön keresztül fogja
                // automatikusan kiolvasni az opcionális racketId-t.
                RacketEditorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}