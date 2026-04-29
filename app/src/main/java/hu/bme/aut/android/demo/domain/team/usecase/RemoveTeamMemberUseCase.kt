package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.data.network.api.team.TeamApiService
import javax.inject.Inject

class RemoveTeamMemberUseCase @Inject constructor(
    private val teamApiService: TeamApiService
) {
    suspend operator fun invoke(teamId: Int, userId: Int) {
        teamApiService.removeTeamMember(teamId, userId)
    }
}