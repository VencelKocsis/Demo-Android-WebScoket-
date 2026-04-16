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

        val data = remoteMessage.data

        if (data.isNotEmpty()) {
            val type = data["type"]
            val matchId = data["matchId"]

            when (type) {
                "PLAYER_SELECTED" -> {
                    val matchName = data["matchName"] ?: ""

                    val title = getString(R.string.player_selected_title)
                    val body = getString(R.string.player_selected_body, matchName)

                    showNotification(title, body, matchId)
                }

                "MATCH_STARTED" -> {
                    val homeTeam = data["homeTeam"] ?: ""
                    val guestTeam = data["guestTeam"] ?: ""

                    val title = getString(R.string.match_started_title)
                    val body = getString(R.string.match_started_body, homeTeam, guestTeam)

                    showNotification(title, body, matchId)
                }

                "MATCH_FINISHED" -> {
                    val result = data["result"] // "WIN", "LOSS", "DRAW"
                    val homeTeam = data["homeTeam"] ?: ""
                    val guestTeam = data["guestTeam"] ?: ""
                    val homeScore = data["homeScore"] ?: "0"
                    val guestScore = data["guestScore"] ?: "0"

                    val title = getString(R.string.match_finished_title)
                    val body = when (result) {
                        "WIN" -> getString(R.string.match_win, homeTeam, guestTeam, homeScore, guestScore)
                        "LOSS" -> getString(R.string.match_loss, homeTeam, guestTeam, homeScore, guestScore)
                        else -> getString(R.string.match_draw, homeTeam, guestTeam, homeScore, guestScore)
                    }

                    showNotification(title, body, matchId)
                }

                "MANUAL_TEST" -> {
                    val testTitle = data["title"] ?: "Teszt Értesítés"
                    val testBody = data["body"] ?: "Ez egy manuális teszt üzenet."
                    showNotification(testTitle, testBody, null)
                }

                else -> {
                    showNotification(getString(R.string.app_name), "Új értesítés érkezett", matchId)
                }
            }
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