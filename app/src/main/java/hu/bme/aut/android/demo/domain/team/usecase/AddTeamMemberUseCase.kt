package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.domain.team.repository.TeamRepository
import javax.inject.Inject

/** UseCase egy új játékos hozzáadásához a csapathoz. */
class AddTeamMemberUseCase @Inject constructor(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: Int, userId: Int) = repository.addTeamMember(teamId, userId)
}