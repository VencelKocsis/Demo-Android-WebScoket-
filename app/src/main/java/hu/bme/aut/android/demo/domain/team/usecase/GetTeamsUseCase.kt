package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.data.team.repository.TeamRepository
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import javax.inject.Inject

class GetTeamsUseCase @Inject constructor(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(): List<TeamDetails> {
        return repository.getTeams()
    }
}