package hu.bme.aut.android.demo.domain.teammatch.usecase

import hu.bme.aut.android.demo.domain.teammatch.repository.TeamMatchRepository
import javax.inject.Inject

/**
 * Egy meccsre jelentkezett játékos státuszának módosítása (pl. SELECTED, LOCKED).
 */
class UpdateParticipantStatusUseCase @Inject constructor(
    private val repository: TeamMatchRepository
) {
    suspend operator fun invoke(participantId: Int, status: String) {
        repository.updateParticipantStatus(participantId, status)
    }
}