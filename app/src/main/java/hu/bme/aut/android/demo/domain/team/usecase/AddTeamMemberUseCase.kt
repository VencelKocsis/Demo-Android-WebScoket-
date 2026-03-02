package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.data.network.model.team.TeamMemberOperationDTO
import javax.inject.Inject

class AddTeamMemberUseCase @Inject constructor(
    private val apiService: ApiService
) {
    suspend operator fun invoke(teamId: Int, userId: Int) {
        apiService.addTeamMember(teamId, TeamMemberOperationDTO(userId))
    }
}