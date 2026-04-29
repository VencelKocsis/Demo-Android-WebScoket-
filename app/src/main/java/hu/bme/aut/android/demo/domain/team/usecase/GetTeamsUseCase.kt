package hu.bme.aut.android.demo.domain.team.usecase

import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import hu.bme.aut.android.demo.domain.team.repository.TeamRepository
import javax.inject.Inject

/** UseCase az összes csapat részletes lekérdezéséhez. */
class GetTeamsUseCase @Inject constructor(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(): List<TeamDetails> = repository.getTeams()
}