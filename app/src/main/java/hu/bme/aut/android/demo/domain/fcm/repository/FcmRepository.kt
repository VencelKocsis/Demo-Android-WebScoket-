package hu.bme.aut.android.demo.domain.fcm.repository

interface FcmRepository {
    suspend fun sendPushNotification(targetEmail: String, title: String, body: String)
}