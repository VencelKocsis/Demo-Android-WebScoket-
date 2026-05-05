package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.domain.team.repository.TeamRepository
import javax.inject.Inject

/**
 * UseCase egy meglévő játékos eltávolításához a csapatból.
 * * A hívás hatására a backend automatikusan Push értesítést küld az érintettnek!
 */
class RemoveTeamMemberUseCase @Inject constructor(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: Int, userId: Int) = repository.removeTeamMember(teamId, userId)
}