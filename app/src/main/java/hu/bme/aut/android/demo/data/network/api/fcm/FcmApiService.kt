package hu.bme.aut.android.demo.data.network.api.fcm

import hu.bme.aut.android.demo.data.fcm.model.FcmToken

interface FcmApiService {
    suspend fun registerFcmToken(registration: FcmToken)
    suspend fun sendPushNotification(payload: Map<String, String>)
}