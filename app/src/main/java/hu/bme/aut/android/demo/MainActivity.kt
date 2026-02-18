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
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import hu.bme.aut.android.demo.navigation.AppNavHost
import hu.bme.aut.android.demo.ui.theme.DemoTheme

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
            // A saját témánkat használjuk (DemoTheme), ami a ui/theme mappában van
            DemoTheme {
                // 1. Létrehozzuk a NavController-t, ami kezeli a navigációt
                val navController = rememberNavController()

                // 2. Meghívjuk a KÜLÖN fájlban lévő AppNavHost-ot (navigation package)
                // Fontos: Itt NEM kérünk le ViewModelt vagy AuthState-et,
                // mert azt az AppNavHost.kt intézi belül Hilt segítségével.
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