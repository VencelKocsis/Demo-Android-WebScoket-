package hu.bme.aut.android.demo.domain.auth.usecases

import hu.bme.aut.android.demo.domain.auth.model.User
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(uid: String): User {
        return authRepository.getUserById(uid)
    }
}