package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.data.network.api.ApiService
import javax.inject.Inject

class RemoveTeamMemberUseCase @Inject constructor(
    private val apiService: ApiService
) {
    suspend operator fun invoke(teamId: Int, userId: Int) {
        apiService.removeTeamMember(teamId, userId)
    }
}