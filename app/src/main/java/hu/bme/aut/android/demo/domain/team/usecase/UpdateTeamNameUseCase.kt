package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.data.network.model.team.TeamUpdateDTO
import javax.inject.Inject

class UpdateTeamNameUseCase @Inject constructor(
    private val apiService: ApiService
) {
    suspend operator fun invoke(teamId: Int, newName: String) {
        apiService.updateTeamName(teamId, TeamUpdateDTO(newName))
    }
}