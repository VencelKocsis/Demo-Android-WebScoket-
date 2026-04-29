package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.data.network.api.team.TeamApiService
import hu.bme.aut.android.demo.domain.team.model.TeamMember
import javax.inject.Inject

class GetAvailableUsersUseCase @Inject constructor(
    private val teamApiService: TeamApiService
) {
    suspend operator fun invoke(): List<TeamMember> {
        return teamApiService.getAvailableUsers().map { dto ->
            TeamMember(
                id = dto.userId,
                uid = dto.firebaseUid,
                name = dto.name,
                isCaptain = dto.isCaptain
            )
        }
    }
}