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
import hu.bme.aut.android.demo.feature.racketEditor.RacketEditorScreen
import hu.bme.aut.android.demo.feature.team.editor.TeamEditorScreen
import hu.bme.aut.android.demo.feature.tournament.match.MatchDetailsScreen
import hu.bme.aut.android.demo.feature.tournament.liveMatch.LiveMatchScreen
import hu.bme.aut.android.demo.feature.tournament.scorer.MatchScorerScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    val startDestination: Any = when (authState) {
        AuthState.UNKNOWN -> Login
        AuthState.AUTHENTICATED -> Main
        AuthState.UNAUTHENTICATED -> Login
    }

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
                            popUpTo<Login> { inclusive = true }
                        }
                    }
                )
            }

            // --- 2. FőKépernyő ---
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

                    // --- JAVÍTVA: Kinyerjük a racketId-t és átadjuk a RacketEditor Route-nak! ---
                    onNavigateToRacketEditor = { racketId ->
                        navController.navigate(RacketEditor(racketId = racketId))
                    }
                )
            }

            // --- 3. Csapatszerkesztő ---
            composable<TeamEditor> { backStackEntry ->
                val args = backStackEntry.toRoute<TeamEditor>()
                TeamEditorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- 4. Meccs Részletek ---
            composable<MatchDetails> { backStackEntry ->
                val args = backStackEntry.toRoute<MatchDetails>()
                MatchDetailsScreen(
                    matchId = args.matchId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLiveMatch = { navController.navigate(LiveMatch(args.matchId)) }
                )
            }

            // --- 5. Élő Mérkőzés ---
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

            // --- 6. Pontozó ---
            composable<MatchScorer> { backStackEntry ->
                val args = backStackEntry.toRoute<MatchScorer>()
                MatchScorerScreen(
                    matchId = args.matchId,
                    individualMatchId = args.individualMatchId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- 7. Játékos Profil ---
            composable<PlayerProfile> { backStackEntry ->
                val args = backStackEntry.toRoute<PlayerProfile>()
                PlayerProfileScreen(
                    playerId = args.playerId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- 8. Ranglista ---
            composable<Leaderboard> {
                LeaderboardScreen()
            }

            // --- 9. Felszerelés ---
            composable<RacketEditor> {
                // A ViewModel (RacketEditorViewModel) a SavedStateHandle-ön keresztül
                // fogja tudni automatikusan kiolvasni a racketId-t
                RacketEditorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}