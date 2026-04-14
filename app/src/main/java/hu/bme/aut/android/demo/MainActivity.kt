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
import hu.bme.aut.android.demo.ui.theme.DemoTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val CHANNEL_ID = "DEMO_CHANNEL"
    private val CHANNEL_NAME = "Demó Értesítések"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) Log.d("FCM", "✅ Értesítési engedély megadva.")
        else Log.w("FCM", "❌ Értesítési engedély megtagadva.")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        requestNotificationPermission()
        enableEdgeToEdge()

        val targetMatchId = intent.extras?.getString("NAVIGATE_TO_MATCH")

        setContent {
            DemoTheme {
                val navController = rememberNavController()

                // DEEP LINK KEZELÉSE (Ha értesítésből nyitották meg)
                LaunchedEffect(targetMatchId) {
                    if (targetMatchId != null) {
                        Log.d("MainActivity", "Navigálás a meccs részleteire: $targetMatchId")
                        navController.navigate("match_details/$targetMatchId") {
                            launchSingleTop = true
                        }
                    }
                }

                AppNavHost(navController = navController)
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