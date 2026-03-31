package hu.bme.aut.android.demo.feature.main

import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.feature.auth.AuthState
import hu.bme.aut.android.demo.feature.auth.AuthViewModel
import hu.bme.aut.android.demo.feature.history.HistoryScreen
import hu.bme.aut.android.demo.feature.history.demoData
import hu.bme.aut.android.demo.feature.profile.ProfileScreen
import hu.bme.aut.android.demo.feature.team.TeamScreen
import hu.bme.aut.android.demo.feature.tournament.teamMatch.TeamMatchScreen
import hu.bme.aut.android.demo.navigation.History
import hu.bme.aut.android.demo.navigation.Profile
import hu.bme.aut.android.demo.navigation.Team
import hu.bme.aut.android.demo.navigation.Tournament
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay

// Segéd osztály az alsó menüpontoknak
private data class BottomNavItem(
    val route: Any,
    @StringRes val titleResId: Int,
    val icon: ImageVector
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    authViewModel: AuthViewModel,
    onNavigateToTeamEditor: (Int) -> Unit = {},
    onNavigateToMatchDetails: (Int) -> Unit = {}
) {
    val bottomNavController = rememberNavController()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // BIZTONSÁGI ŐR
    LaunchedEffect(authState) {
        if (authState == AuthState.UNAUTHENTICATED || authViewModel.getCurrentUser() == null) {
            onLogout()
        }
    }

    // ÉRTESÍTÉSI ENGEDÉLY
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Log.d("MainScreen", "Push Notification engedély megadva az operációs rendszertől!")
                // Itt akár egy Snackbar is lehet hogy "Értesítések bekapcsolva"
            } else {
                Log.w("MainScreen", "Push Notification engedély MEGTAGADVA!")
            }
        }
    )

    // FCM TOKEN
    LaunchedEffect(Unit) {
        var tokenRetrieved = false
        var retryCount = 0
        while (!tokenRetrieved && retryCount < 3) {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                val currentUserEmail = authViewModel.getCurrentUser()?.email
                if (currentUserEmail != null) {
                    authViewModel.registerFcmToken(currentUserEmail, token)
                    tokenRetrieved = true
                } else {
                    delay(2000)
                }
            } catch (e: Exception) {
                retryCount++
                delay(5000)
            }
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(Tournament, R.string.championship, Icons.Default.EmojiEvents),
        BottomNavItem(Team, R.string.club, Icons.Default.Group),
        BottomNavItem(History, R.string.history, Icons.Default.History),
        BottomNavItem(Profile, R.string.profile, Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    // Az új módszerrel megvizsgáljuk, hogy az adott objektum osztálya szerepel-e a hierarchiában
                    val isSelected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true

                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = stringResource(item.titleResId)) },
                        label = { Text(stringResource(item.titleResId)) },
                        selected = isSelected,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = bottomNavController,
                startDestination = Tournament
            ) {
                composable<Tournament> {
                    TeamMatchScreen(onNavigateToMatchDetails = onNavigateToMatchDetails)
                }
                composable<Team> {
                    TeamScreen(
                        onNavigateToEditor = onNavigateToTeamEditor,
                        onNavigateToMatch = onNavigateToMatchDetails
                    )
                }
                composable<History> {
                    HistoryScreen(results = demoData)
                }
                composable<Profile> {
                    ProfileScreen(authViewModel = authViewModel, onLogoutClick = onLogout)
                }
            }
        }
    }
}