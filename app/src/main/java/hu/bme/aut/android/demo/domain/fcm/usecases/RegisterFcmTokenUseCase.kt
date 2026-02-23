package hu.bme.aut.android.demo.domain.fcm.usecases

import hu.bme.aut.android.demo.data.websocket.repository.PlayerRepository
import javax.inject.Inject

class RegisterFcmTokenUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    /**
     * @param token a Firebase Cloud Messaging által generált egyedi regisztrációs token.
     */
    suspend operator fun invoke(email: String, token: String) {
        repository.registerFcmToken(email, token)
    }
}