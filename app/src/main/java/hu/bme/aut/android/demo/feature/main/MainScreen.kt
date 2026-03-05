package hu.bme.aut.android.demo.feature.main

import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import hu.bme.aut.android.demo.feature.auth.AuthViewModel
import hu.bme.aut.android.demo.feature.history.HistoryScreen
import hu.bme.aut.android.demo.feature.history.demoData
import hu.bme.aut.android.demo.feature.profile.ProfileScreen
import hu.bme.aut.android.demo.feature.team.TeamScreen
import hu.bme.aut.android.demo.feature.tournament.TeamMatchScreen
import hu.bme.aut.android.demo.navigation.Screen
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.Manifest
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    onLogout: () -> Unit, // Callback a kijelentkezéshez (a Profilról érhető el)
    authViewModel: AuthViewModel,
    onNavigateToTeamEditor: (Int) -> Unit = {},
    onNavigateToMatchDetails: (Int) -> Unit = {}
) {
    // Külön NavController a belső (alsó menüs) navigációhoz
    val bottomNavController = rememberNavController()
    val scope = rememberCoroutineScope()

    // --- 1. ÉRTESÍTÉSI ENGEDÉLY KÉRÉSE (Android 13+) ---
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Log.d("MainScreen", "Push Notification engedély megadva az operációs rendszertől!")
            } else {
                Log.w("MainScreen", "Push Notification engedély MEGTAGADVA!")
            }
        }
    )

    // --- 2. FCM TOKEN LEKÉRÉSE ÉS SZINKRONIZÁLÁSA ---
    LaunchedEffect(Unit) {
        var tokenRetrieved = false
        var retryCount = 0
        val maxRetries = 3

        while (!tokenRetrieved && retryCount < maxRetries) {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                val currentUserEmail = authViewModel.getCurrentUser()?.email

                if (currentUserEmail != null) {
                    authViewModel.registerFcmToken(currentUserEmail, token)
                    Log.d("MainScreen", "FCM Token sikeresen regisztrálva: $currentUserEmail")
                    tokenRetrieved = true
                } else {
                    Log.w("MainScreen", "Még nincs bejelentkezett felhasználó, várjunk...")
                    delay(2000) // Várjunk 2 másodpercet, hátha az Auth még frissül
                }
            } catch (e: Exception) {
                retryCount++
                Log.e("MainScreen", "Hiba az FCM token lekérésekor ($retryCount. próbálkozás): ${e.message}")
                delay(5000) // 5 másodperc múlva újrapróbáljuk
            }
        }
    }

    // A menüpontok listája
    val bottomNavItems = listOf(
        Screen.Tournament,
        Screen.Team,
        Screen.History,
        Screen.Profile
    )

    // TODO ha egy új telefonra telepítem az alkalmazást, akkor bedob a főképernyőre,
    /* annak ellenére, hogy nincs bejelentkezve egy fiókba, ahelyett hogy a
        register/login képernyőn kezdene a felhasználó
     */

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                        label = { Text(screen.title!!) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                // Visszaugrás a gráf kezdőpontjára, hogy ne teljen meg a stack
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
        // Belső NavHost az alsó menü lapjaihoz
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = bottomNavController,
                startDestination = Screen.Tournament.route
            ) {
                // --- 1. JAVÍTOTT: Bajnokság (Tournament) képernyő ---
                composable(Screen.Tournament.route) {
                    TeamMatchScreen(
                        onNavigateToMatchDetails = onNavigateToMatchDetails
                    )
                }

                // --- 2. Csapat (Team) képernyő ---
                composable(Screen.Team.route) {
                    TeamScreen(
                        onNavigateToEditor = onNavigateToTeamEditor
                    )
                }

                // --- 3. Előzmények (History) képernyő ---
                composable(Screen.History.route) {
                    HistoryScreen(results = demoData) // TODO: ViewModelből adatok
                }

                // --- 4. Profil (Profile) képernyő ---
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        onLogoutClick = onLogout
                    )
                }
            }
        }
    }
}