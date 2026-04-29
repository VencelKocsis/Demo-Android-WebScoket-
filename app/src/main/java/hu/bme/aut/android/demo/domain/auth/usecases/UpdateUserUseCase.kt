package hu.bme.aut.android.demo.domain.auth.usecases

import hu.bme.aut.android.demo.domain.auth.model.User
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase a felhasználói profiladatok (pl. vezetéknév, keresztnév) módosításához és
 * a változások backendre történő mentéséhez.
 */
class UpdateUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(user: User): User {
        return authRepository.updateUser(user)
    }
}