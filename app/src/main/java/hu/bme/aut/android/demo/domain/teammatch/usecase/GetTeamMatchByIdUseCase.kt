package hu.bme.aut.android.demo.domain.teammatch.usecase

import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.domain.teammatch.repository.TeamMatchRepository
import jakarta.inject.Inject

class GetTeamMatchByIdUseCase @Inject constructor(
    private val repository: TeamMatchRepository
) {
    suspend operator fun invoke(matchId: Int): TeamMatch {
        return repository.getTeamMatchById(matchId)
    }
}