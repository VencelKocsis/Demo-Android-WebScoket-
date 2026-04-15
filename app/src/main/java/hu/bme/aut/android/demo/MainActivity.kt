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

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        requestNotificationPermission()

        enableEdgeToEdge()

        // Kiolvassuk a PendingIntent-ből a meccs ID-t
        val targetMatchId = intent.extras?.getString("NAVIGATE_TO_MATCH")?.toIntOrNull()

        setContent {
            DemoTheme {
                val navController = rememberNavController()

                LaunchedEffect(targetMatchId) {
                    if (targetMatchId != null) {
                        Log.d("MainActivity", "Navigálás a meccs részleteire: $targetMatchId")

                        delay(150)

                        try {
                            navController.navigate(MatchDetails(matchId = targetMatchId)) {
                                launchSingleTop = true
                            }

                            intent.removeExtra("NAVIGATE_TO_MATCH")
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Hiba a navigációnál: ${e.message}")
                        }
                    }
                }

                AppNavHost(navController = navController)
            }
        }
    }

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