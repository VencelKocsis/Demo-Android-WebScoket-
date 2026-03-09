package hu.bme.aut.android.demo.domain.teammatch.usecase

import hu.bme.aut.android.demo.domain.teammatch.repository.TeamMatchRepository
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import javax.inject.Inject

class GetTeamMatchesUseCase @Inject constructor(
    private val repository: TeamMatchRepository
) {
    suspend operator fun invoke(): List<TeamMatch> {
        return repository.getTeamMatches()
    }
}