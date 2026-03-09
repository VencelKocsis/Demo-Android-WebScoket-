package hu.bme.aut.android.demo.domain.teammatch.usecase

import hu.bme.aut.android.demo.domain.teammatch.repository.TeamMatchRepository
import javax.inject.Inject

class SubmitIndividualScoreUseCase @Inject constructor(
    private val repository: TeamMatchRepository
) {
    suspend operator fun invoke(individualMatchId: Int, homeScore: Int, guestScore: Int, setScores: String, status: String) {
        repository.updateIndividualScore(individualMatchId, homeScore, guestScore, setScores, status)
    }
}