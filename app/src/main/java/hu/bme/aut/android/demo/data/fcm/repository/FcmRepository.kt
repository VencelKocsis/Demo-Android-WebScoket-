package hu.bme.aut.android.demo.data.fcm.repository

import hu.bme.aut.android.demo.data.network.api.RetrofitApi
import javax.inject.Inject

class FcmRepository @Inject constructor(
    private val retrofitApi: RetrofitApi
) {
    suspend fun sendPushNotification(targetEmail: String, title: String, body: String) {
        val payload = mapOf(
            "targetEmail" to targetEmail,
            "title" to title,
            "body" to body
        )
        retrofitApi.sendPushNotification(payload)
    }
}
