package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.domain.team.repository.TeamRepository
import javax.inject.Inject

/** UseCase a csapat nevének szerkesztéséhez. */
class UpdateTeamNameUseCase @Inject constructor(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: Int, newName: String) = repository.updateTeamName(teamId, newName)
}