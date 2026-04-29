package hu.bme.aut.android.demo.domain.fcm.usecases

import hu.bme.aut.android.demo.domain.fcm.repository.FcmRepository
import javax.inject.Inject

/**
 * UseCase az eszköz Firebase Cloud Messaging (FCM) tokenjének regisztrálásához a backend szerveren.
 * * Ezt általában bejelentkezéskor vagy a token automatikus megújulásakor (onNewToken) hívjuk meg,
 * hogy a szerver tudja, hová kell küldeni a push értesítéseket az adott felhasználónak.
 */
class RegisterFcmTokenUseCase @Inject constructor(
    private val repository: FcmRepository
) {
    /**
     * @param email A felhasználó e-mail címe, amihez a tokent rendeljük.
     * @param token A Firebase Cloud Messaging által generált egyedi regisztrációs token.
     */
    suspend operator fun invoke(email: String, token: String) {
        repository.registerFcmToken(email, token)
    }
}