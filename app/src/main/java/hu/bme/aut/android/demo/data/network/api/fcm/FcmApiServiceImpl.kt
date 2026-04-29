package hu.bme.aut.android.demo.data.network.api.fcm

import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import javax.inject.Inject

class FcmApiServiceImpl @Inject constructor(
    private val fcmRetrofitApi: FcmRetrofitApi
) : FcmApiService {
    override suspend fun registerFcmToken(registration: FcmToken) = fcmRetrofitApi.registerFcmToken(registration)
    override suspend fun sendPushNotification(payload: Map<String, String>) = fcmRetrofitApi.sendPushNotification(payload)
}