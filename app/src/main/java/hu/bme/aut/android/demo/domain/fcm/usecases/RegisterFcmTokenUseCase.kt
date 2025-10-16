package hu.bme.aut.android.demo.domain.fcm.usecases

import hu.bme.aut.android.demo.domain.websocket.repository.PlayerRepository
import javax.inject.Inject

class RegisterFcmTokenUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    /**
     * @param token a Firebase Cloud Messaging által generált egyedi regisztrációs token.
     */
    suspend operator fun invoke(userId: String, token: String) {
        repository.registerFcmToken(userId, token)
    }
}