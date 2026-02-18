package hu.bme.aut.android.demo.feature.main

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
import hu.bme.aut.android.demo.feature.history.HistoryScreen
import hu.bme.aut.android.demo.feature.history.demoData
import hu.bme.aut.android.demo.feature.profile.ProfileScreen
import hu.bme.aut.android.demo.feature.team.TeamScreen
import hu.bme.aut.android.demo.feature.tournament.TournamentScreen
import hu.bme.aut.android.demo.navigation.Screen

@Composable
fun MainScreen(
    onLogout: () -> Unit // Callback a kijelentkezéshez (a Profilról érhető el)
) {
    // Külön NavController a belső (alsó menüs) navigációhoz
    val bottomNavController = rememberNavController()

    // A menüpontok listája
    val bottomNavItems = listOf(
        Screen.Tournament,
        Screen.Team,
        Screen.History,
        Screen.Profile
    )

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
                composable(Screen.Tournament.route) {
                    TournamentScreen() // Paramétereket később bekötjük ViewModelből
                }
                composable(Screen.Team.route) {
                    TeamScreen()
                }
                composable(Screen.History.route) {
                    HistoryScreen(results = demoData) // TODO: ViewModelből adatok
                }
                composable(Screen.Profile.route) {
                    ProfileScreen()
                    // TODO: A ProfileScreen-be bele kell majd drótozni az onLogout hívást
                }
            }
        }
    }
}

