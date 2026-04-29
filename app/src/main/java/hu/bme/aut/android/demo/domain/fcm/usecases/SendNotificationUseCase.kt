package hu.bme.aut.android.demo.domain.fcm.usecases

import hu.bme.aut.android.demo.domain.fcm.repository.FcmRepository
import javax.inject.Inject

/**
 * UseCase egy célzott push értesítés manuális kiküldéséhez egy adott felhasználónak.
 * * Hasznos lehet például a Piac funkciónál az eladónak küldött értesítésekhez, vagy teszteléshez.
 */
class SendPushNotificationUseCase @Inject constructor(
    private val repository: FcmRepository
) {
    /**
     * @param targetEmail A címzett felhasználó e-mail címe.
     * @param title Az értesítés fejléce (címe).
     * @param body Az értesítés törzse (szövege).
     */
    suspend operator fun invoke(targetEmail: String, title: String, body: String) {
        repository.sendPushNotification(targetEmail, title, body)
    }
}