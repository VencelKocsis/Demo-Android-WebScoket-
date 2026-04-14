package hu.bme.aut.android.demo.data.fcm.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import hu.bme.aut.android.demo.MainActivity
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.domain.fcm.usecases.RegisterFcmTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.jvm.java

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCM_SERVICE"
    private val CHANNEL_ID = "DEMO_CHANNEL"
    private val NOTIFICATION_ID = 101

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
            val matchId = remoteMessage.data["matchId"]
            showNotification(title, body, matchId)
        }

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Adat payload: ${remoteMessage.data}")
        }
    }

    private fun showNotification(title: String, message: String, matchId: String?) {
        // 1. Intent elkészítése, ami megnyitja a MainActivity-t
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Ha a backend küldött matchId-t, átadjuk az indításnak!
            if (matchId != null) {
                putExtra("NAVIGATE_TO_MATCH", matchId)
            }
        }

        // 2. PendingIntent: Ez csomagolja be az Intentet az értesítés számára
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Értesítés építése a setContentIntent-tel
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(SMALL_ICON_RES_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Magas prioritás, hogy felülről leugorjon!
            .setAutoCancel(true) // Kattintásra eltűnik az értesítés
            .setContentIntent(pendingIntent) // <--- EZ NYITJA MEG AZ APPOT!

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    /**
     * FCM tokent küld a szerverre.
     */
    private fun sendTokenToServer(token: String) {
        serviceScope.launch {
            try {
                val email = FirebaseAuth.getInstance().currentUser?.email ?: return@launch
                Log.i(TAG, "Aktuális felhasználó e-mail: $email, token: $token")

                registerFcmTokenUseCase(email, token)
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