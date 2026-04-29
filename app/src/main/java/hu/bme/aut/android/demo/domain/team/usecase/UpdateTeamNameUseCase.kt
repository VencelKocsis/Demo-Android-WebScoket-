package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.data.network.api.team.TeamApiService
import hu.bme.aut.android.demo.data.network.model.team.TeamUpdateDTO
import javax.inject.Inject

class UpdateTeamNameUseCase @Inject constructor(
    private val teamApiService: TeamApiService
) {
    suspend operator fun invoke(teamId: Int, newName: String) {
        teamApiService.updateTeamName(teamId, TeamUpdateDTO(newName))
    }
}