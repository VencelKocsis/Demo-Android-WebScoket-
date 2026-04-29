package hu.bme.aut.android.demo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import hu.bme.aut.android.demo.navigation.AppNavHost
import hu.bme.aut.android.demo.navigation.MatchDetails
import hu.bme.aut.android.demo.ui.theme.DemoTheme
import kotlinx.coroutines.delay

/**
 * Az alkalmazás egyetlen Activity-je (Single Activity Architecture).
 * * Clean Architecture szempontból ez a legkülső, Framework (Keretrendszer) réteghez tartozik.
 * * Feladata kizárólag az operációs rendszerrel való kapcsolattartás (Engedélyek, Intentek, Értesítési csatornák)
 * és a Jetpack Compose UI (AppNavHost) elindítása.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val CHANNEL_ID = "DEMO_CHANNEL"
    private val CHANNEL_NAME = "Demó Értesítések"

    /**
     * Rendszerszintű hívás a Push Értesítések (Android 13+) engedélyezéséhez.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "✅ Értesítési engedély megadva.")
        } else {
            Log.w("FCM", "❌ Értesítési engedély megtagadva.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Rendszer beállítások inicializálása
        createNotificationChannel()
        requestNotificationPermission()
        enableEdgeToEdge()

        // Operációs rendszertől kapott Intent adatok kiolvasása (Pl.: Push értesítésre kattintás)
        val targetMatchId = intent.extras?.getString("NAVIGATE_TO_MATCH")?.toIntOrNull()

        // A deklaratív UI (Jetpack Compose) belépési pontja
        setContent {
            DemoTheme {
                val navController = rememberNavController()

                // Ha az alkalmazást egy FCM értesítés indította el, automatikusan
                // a megfelelő meccs részleteire navigálunk.
                LaunchedEffect(targetMatchId) {
                    if (targetMatchId != null) {
                        Log.d("MainActivity", "Navigálás a meccs részleteire: $targetMatchId")

                        // Kis késleltetés, hogy a Compose NavHost biztosan felépüljön a navigáció előtt
                        delay(150)

                        try {
                            // Type-Safe navigáció használata a megadott képernyőre
                            navController.navigate(MatchDetails(matchId = targetMatchId)) {
                                launchSingleTop = true
                            }

                            // Töröljük az extrát, hogy egy konfigurációváltás (pl. képernyő elforgatás)
                            // esetén ne navigáljon újra.
                            intent.removeExtra("NAVIGATE_TO_MATCH")
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Hiba a navigációnál: ${e.message}")
                        }
                    }
                }

                // A fő navigációs gráf elindítása
                AppNavHost(navController = navController)
            }
        }
    }

    /**
     * Létrehozza a Push Értesítésekhez szükséges csatornát az Android (8.0+) rendszerben.
     * Enélkül a Firebase hiába küld üzenetet, a telefon nem jeleníti meg.
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
     * Android 13 (TIRAMISU) felett futásidőben kell engedélyt kérni a vizuális értesítésekhez.
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