package hu.bme.aut.android.demo.domain.teammatch.usecase

import hu.bme.aut.android.demo.domain.teammatch.repository.TeamMatchRepository
import javax.inject.Inject

/**
 * A csapat felállásának (Lineup) és a játékosok sorrendjének véglegesítése.
 */
class SubmitLineupUseCase @Inject constructor(
    private val repository: TeamMatchRepository
) {
    suspend operator fun invoke(matchId: Int, teamSide: String, positions: Map<Int, Int>) {
        repository.submitLineup(matchId, teamSide, positions)
    }
}