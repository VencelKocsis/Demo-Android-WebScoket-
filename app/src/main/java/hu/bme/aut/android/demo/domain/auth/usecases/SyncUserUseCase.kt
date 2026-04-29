package hu.bme.aut.android.demo.domain.auth.usecases

import hu.bme.aut.android.demo.domain.auth.model.User
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase a felhasználó adatainak szinkronizálására a saját backend szerverünkkel.
 * * Regisztráció vagy bejelentkezés után hívjuk meg, hogy biztosítsuk:
 * a Firebase-ben hitelesített felhasználó a mi PostgreSQL adatbázisunkban is létezik
 * (és a profiladatai naprakészek).
 */
class SyncUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(user: User): User {
        return authRepository.syncUser(user)
    }
}