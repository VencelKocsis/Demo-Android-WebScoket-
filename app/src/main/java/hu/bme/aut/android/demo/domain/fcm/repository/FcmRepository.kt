package hu.bme.aut.android.demo.domain.fcm.repository

interface FcmRepository {
    suspend fun registerFcmToken(email: String, token: String)
    suspend fun sendPushNotification(targetEmail: String, title: String, body: String)
}