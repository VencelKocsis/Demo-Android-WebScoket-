package hu.bme.aut.android.demo.data.fcm.service

import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.domain.fcm.usecases.RegisterFcmTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCM_SERVICE"
    private val CHANNEL_ID = "DEMO_CHANNEL"
    private val NOTIFICATION_ID = 101

    // Feltételezve, hogy ez az a monokróm ikon, amit az Image Asset Studio generáltál!
    // Kérlek, ellenőrizd a 'R.drawable' alatti ikonnevet, hogy megegyezik-e a generálttal.
    private val SMALL_ICON_RES_ID = R.drawable.ic_notification_tt

    @Inject
    lateinit var registerFcmTokenUseCase: RegisterFcmTokenUseCase

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob)

    override fun onNewToken(token: String) {
        Log.d(TAG, "Új FCM token: $token")
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Üzenet érkezett: ${remoteMessage.from}")

        // Megjelenítjük a notifikációt, ha van benne adat (Notification payload)
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Új értesítés"
            val body = notification.body ?: "Nincs üzenet"
            // FIGYELEM: Ehhez kell a futásidejű engedélykérés a MainActivity-ben!
            showNotification(title, body)
        }

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Adat payload: ${remoteMessage.data}")
        }
    }

    private fun showNotification(title: String, message: String) {
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(SMALL_ICON_RES_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    /**
     * FCM tokent küld a szerverre.
     * ÁTÍRVA: A fix "test-user-fcm-target" ID-t használja.
     */
    private fun sendTokenToServer(token: String) {
        serviceScope.launch {
            try {
                // A Ktor backendez már ezt az ID-t várja.
                val userId = "test-user-fcm-target"

                Log.i(TAG, "FCM token regisztrálása user=$userId, token=$token")
                registerFcmTokenUseCase(userId, token)
                Log.i(TAG, "✅ FCM token sikeresen regisztrálva.")
            } catch (e: Exception) {
                Log.e(TAG, "Hiba az FCM token regisztrálása során: ${e.message}", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}