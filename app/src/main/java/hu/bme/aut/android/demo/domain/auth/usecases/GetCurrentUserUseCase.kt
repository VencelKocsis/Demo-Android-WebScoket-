package hu.bme.aut.android.demo.domain.auth.usecases

import com.google.firebase.auth.FirebaseUser
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase a jelenleg bejelentkezett aktív munkamenet (felhasználó) lekérdezéséhez.
 * Indításkor vagy automatikus bejelentkezés ellenőrzésekor használjuk.
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }
}