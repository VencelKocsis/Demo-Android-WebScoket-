package hu.bme.aut.android.demo.domain.auth.usecases

import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * Kezeli a felhasználó biztonságos kijelentkeztetését az eszközről.
 *
 * @param authRepository A hitelesítési műveletekhez használt Repository.
 */
class SignOutUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Meghívja a kijelentkezési műveletet a Repository-n, törölve a helyi munkamenetet.
     */
    operator fun invoke() {
        authRepository.signOutUser()
    }
}
