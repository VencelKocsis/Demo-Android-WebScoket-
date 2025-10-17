package hu.bme.aut.android.demo.domain.auth.usecases

import com.google.firebase.auth.FirebaseUser
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * Kezeli egy új felhasználó regisztrációját.
 *
 * @param authRepository A hitelesítési műveletekhez használt Repository.
 */
class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * @return Egy Result objektum, ami vagy a FirebaseUser objektumot adja vissza sikeres regisztráció esetén,
     * vagy egy Throwable-t hiba esetén.
     */
    suspend operator fun invoke(email: String, password: String): Result<FirebaseUser> {
        return authRepository.registerUser(email, password)
    }
}