package hu.bme.aut.android.demo.domain.auth.usecases

import com.google.firebase.auth.FirebaseUser
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * Kezeli egy meglévő felhasználó bejelentkeztetését az alkalmazásba.
 *
 * @param authRepository A hitelesítési műveletekhez használt Repository.
 */
class SignInUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * @return Egy [Result] objektum, ami sikeres bejelentkezés esetén a [FirebaseUser]
     * objektumot adja vissza, hiba esetén (pl. hibás jelszó) pedig egy kivételt.
     */
    suspend operator fun invoke(email: String, password: String): Result<FirebaseUser> {
        return authRepository.signInUser(email, password)
    }
}
