package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.data.network.model.TeamWithMembersDTO
import hu.bme.aut.android.demo.domain.team.repository.TeamRepository
import javax.inject.Inject

class GetTeamsUseCase @Inject constructor(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(): List<TeamWithMembersDTO> {
        return repository.getTeams()
    }
}