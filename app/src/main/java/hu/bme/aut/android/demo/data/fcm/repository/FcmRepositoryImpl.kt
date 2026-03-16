package hu.bme.aut.android.demo.data.fcm.repository

import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import hu.bme.aut.android.demo.data.network.api.RetrofitApi
import hu.bme.aut.android.demo.domain.fcm.repository.FcmRepository
import javax.inject.Inject

class FcmRepositoryImpl @Inject constructor(
    private val retrofitApi: RetrofitApi
) : FcmRepository {
    override suspend fun registerFcmToken(email: String, token: String) {
        retrofitApi.registerFcmToken(FcmToken(email, token))
    }

    override suspend fun sendPushNotification(targetEmail: String, title: String, body: String) {
        val payload = mapOf(
            "targetEmail" to targetEmail,
            "title" to title,
            "body" to body
        )
        retrofitApi.sendPushNotification(payload)
    }
}
