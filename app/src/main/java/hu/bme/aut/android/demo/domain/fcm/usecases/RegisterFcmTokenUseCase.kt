package hu.bme.aut.android.demo.domain.fcm.usecases

import hu.bme.aut.android.demo.domain.fcm.repository.FcmRepository
import javax.inject.Inject

class RegisterFcmTokenUseCase @Inject constructor(
    private val repository: FcmRepository
) {
    /**
     * @param email a felhasználó email címe, amihez a tokent rendeljük
     * @param token a Firebase Cloud Messaging által generált egyedi regisztrációs token.
     */
    suspend operator fun invoke(userId: String, token: String) {
        repository.registerFcmToken(userId, token)
    }
}