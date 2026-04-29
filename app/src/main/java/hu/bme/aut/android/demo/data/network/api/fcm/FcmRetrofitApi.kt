package hu.bme.aut.android.demo.data.network.api.fcm

import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmRetrofitApi {
    @POST("register_fcm_token")
    suspend fun registerFcmToken(@Body registration: FcmToken)

    @POST("send_fcm_notification")
    suspend fun sendPushNotification(@Body payload: Map<String, String>)
}