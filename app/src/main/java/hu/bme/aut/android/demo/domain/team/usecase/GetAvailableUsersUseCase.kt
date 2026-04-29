package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.domain.team.model.TeamMember
import hu.bme.aut.android.demo.domain.team.repository.TeamRepository
import javax.inject.Inject

/** UseCase azon felhasználók lekéréséhez, akik nincsenek még egy csapatban sem. */
class GetAvailableUsersUseCase @Inject constructor(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(): List<TeamMember> = repository.getAvailableUsers()
}