package hu.bme.aut.android.demo.domain.auth.usecases

import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase a jelszó-visszaállítási folyamat elindításához.
 * * Üzleti logikát is tartalmaz: ellenőrzi, hogy az e-mail cím ne legyen üres,
 * mielőtt továbbítaná a kérést a Repository-nak, elkerülve a felesleges hálózati hívásokat.
 */
class ForgotPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Kérjük, add meg az e-mail címedet a jelszó visszaállításához!"))
        }
        return authRepository.sendPasswordResetEmail(email)
    }
}