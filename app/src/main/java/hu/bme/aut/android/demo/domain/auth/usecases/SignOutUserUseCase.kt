package hu.bme.aut.android.demo.domain.auth.usecase

import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * Kezeli a felhasználó kijelentkezését.
 *
 * @param authRepository A hitelesítési műveletekhez használt Repository.
 */
class SignOutUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Meghívja a kijelentkezési műveletet a Repository-n.
     */
    operator fun invoke() {
        authRepository.signOutUser()
    }
}
