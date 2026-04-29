package hu.bme.aut.android.demo.domain.teammatch.usecase

import hu.bme.aut.android.demo.domain.teammatch.repository.TeamMatchRepository
import javax.inject.Inject

/**
 * Csapatkapitányi funkció: egy játékos manuális hozzáadása a meccskerethez.
 */
class CaptainAddParticipantUseCase @Inject constructor(
    private val repository : TeamMatchRepository
) {
    suspend operator fun invoke(matchId: Int, userId: Int) {
        repository.captainAddParticipantToMatch(matchId, userId)
    }
}