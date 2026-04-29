package hu.bme.aut.android.demo.data.network.api.fcm

import hu.bme.aut.android.demo.data.fcm.model.FcmToken

/**
 * Az FCM hálózati műveletek elvont szerződése.
 * * Függetleníti a Repository-t a Retrofit hívásoktól.
 */
interface FcmApiService {
    suspend fun registerFcmToken(registration: FcmToken)
    suspend fun sendPushNotification(payload: Map<String, String>)
}