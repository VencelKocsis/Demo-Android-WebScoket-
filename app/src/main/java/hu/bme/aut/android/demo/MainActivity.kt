package hu.bme.aut.android.demo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import hu.bme.aut.android.demo.feature.auth.AuthViewModel
import hu.bme.aut.android.demo.feature.auth.AuthState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import hu.bme.aut.android.demo.feature.auth.AuthScreen
import hu.bme.aut.android.demo.feature.list_players.PlayersViewModel
// IMPORTÁLJUK AZ ÚJ DEMOSCREEN-T a list_players csomagból
import hu.bme.aut.android.demo.feature.list_players.DemoScreen


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val CHANNEL_ID = "DEMO_CHANNEL"
    private val CHANNEL_NAME = "Demó Értesítések"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "✅ Értesítési engedély megadva.")
        } else {
            Log.w("FCM", "❌ Értesítési engedély megtagadva.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        requestNotificationPermission()

        setContent {
            MaterialTheme {
                // AuthViewModel beolvasása, ami kezeli a hitelesítést
                val authViewModel: AuthViewModel = hiltViewModel()

                // A bejelentkezési állapot figyelése
                val authState by authViewModel.authState.collectAsState()

                // Az alkalmazás gyökér komponense, ami a hitelesítési állapot alapján vált
                AppNavHost(
                    authState = authState,
                    authViewModel = authViewModel
                )
            }
        }
    }

    /**
     * Létrehozza az értesítési csatornát.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Értesítések a Ktor szerver valós idejű eseményeiről"
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Engedélyt kér a push értesítések megjelenítésére (Android 13+).
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!getSystemService(NotificationManager::class.java).areNotificationsEnabled()) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("FCM", "Értesítések már engedélyezve.")
            }
        }
    }
}

/**
 * A gyökér navigációs komponens, ami az AuthState alapján választ képernyőt.
 */
@Composable
fun AppNavHost(
    authState: AuthState,
    authViewModel: AuthViewModel
) {
    when (authState) {
        AuthState.UNKNOWN -> LoadingScreen() // Töltőképernyő (kezdeti ellenőrzés alatt)

        AuthState.UNAUTHENTICATED -> {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = { user -> Log.d("Auth", "Sikeres hitelesítés: ${user.uid}") }
            )
        }

        AuthState.AUTHENTICATED -> {
            // A Fő tartalmi képernyő helyett a teljes DemoScreen-t használjuk
            val playersViewModel: PlayersViewModel = hiltViewModel()
            DemoScreen(
                viewModel = playersViewModel,
                onLogout = authViewModel::signOut // Kijelentkezés
            )
        }
    }
}

// --- HELYESEN DEFINIÁLT PLACEHOLDER Képernyők ---

@Composable
fun LoadingScreen() {
    Text("Betöltés...")
}
