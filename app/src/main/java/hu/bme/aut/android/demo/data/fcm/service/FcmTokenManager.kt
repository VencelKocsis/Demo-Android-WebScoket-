package hu.bme.aut.android.demo.data.fcm.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import hu.bme.aut.android.demo.domain.fcm.usecases.RegisterFcmTokenUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase
) {
    private val TAG = "FcmTokenManager"

    suspend fun syncTokenWithServer(email: String) {
        var success = false
        var retryCount = 0
        val maxRetries = 5
        var currentDelay = 2000L

        while (!success && retryCount < maxRetries) {
            try {
                // 1. Token lekérése a Firebase-től
                val token = FirebaseMessaging.getInstance().token.await()

                if (token.isNotEmpty()) {
                    // 2. Küldés a Ktor backendnek
                    registerFcmTokenUseCase(email, token)
                    Log.i(TAG, "✅ FCM Token sikeresen szinkronizálva: $email")
                    success = true
                }
            } catch (e: Exception) {
                retryCount++
                Log.e(TAG, "❌ Hiba az FCM szinkronizáció alatt ($retryCount. próbálkozás): ${e.message}")

                if (retryCount < maxRetries) {
                    delay(currentDelay)
                    currentDelay *= 2 // Exponenciális várakozás
                }
            }
        }
    }
}