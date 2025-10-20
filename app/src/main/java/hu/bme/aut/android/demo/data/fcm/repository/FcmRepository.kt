package hu.bme.aut.android.demo.data.fcm.repository

import hu.bme.aut.android.demo.data.network.api.ApiService
import javax.inject.Inject

class FcmRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun sendPushNotification(targetEmail: String, title: String, body: String) {
        val payload = mapOf(
            "targetEmail" to targetEmail,
            "title" to title,
            "body" to body
        )
        apiService.sendPushNotification(payload)
    }
}
