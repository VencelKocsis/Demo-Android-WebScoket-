package hu.bme.aut.android.demo.data.fcm.repository

import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import hu.bme.aut.android.demo.data.network.api.fcm.FcmRetrofitApi
import hu.bme.aut.android.demo.domain.fcm.repository.FcmRepository
import javax.inject.Inject

/**
 * Az [FcmRepository] interfész megvalósítása a Data rétegben.
 * Feladata a push értesítésekhez szükséges tokenek regisztrálása és
 * a push üzenetek küldésének kezdeményezése a backend API felé.
 */
class FcmRepositoryImpl @Inject constructor(
    private val fcmRetrofitApi: FcmRetrofitApi
) : FcmRepository {

    /**
     * Regisztrálja vagy frissíti a felhasználó aktuális eszközének FCM tokenjét a backend adatbázisában.
     */
    override suspend fun registerFcmToken(email: String, token: String) {
        fcmRetrofitApi.registerFcmToken(FcmToken(email, token))
    }

    /**
     * Manuálisan kezdeményez egy push értesítés küldését a backendtől egy célzott e-mail címre.
     * (Ezt használhatjuk pl. tesztelésre vagy egyedi kliens-alapú triggerekhez).
     */
    override suspend fun sendPushNotification(targetEmail: String, title: String, body: String) {
        val payload = mapOf(
            "targetEmail" to targetEmail,
            "title" to title,
            "body" to body
        )
        fcmRetrofitApi.sendPushNotification(payload)
    }
}