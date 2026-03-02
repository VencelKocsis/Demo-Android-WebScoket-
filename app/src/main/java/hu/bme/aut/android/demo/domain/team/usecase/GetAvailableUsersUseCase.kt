package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.domain.team.model.TeamMember
import javax.inject.Inject

class GetAvailableUsersUseCase @Inject constructor(
    private val apiService: ApiService
) {
    suspend operator fun invoke(): List<TeamMember> {
        return apiService.getAvailableUsers().map { dto ->
            TeamMember(
                id = dto.userId,
                uid = dto.firebaseUid,
                name = dto.name,
                isCaptain = dto.isCaptain
            )
        }
    }
}