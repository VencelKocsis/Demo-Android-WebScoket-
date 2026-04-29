package hu.bme.aut.android.demo.domain.teammatch.usecase

import hu.bme.aut.android.demo.domain.teammatch.repository.TeamMatchRepository
import javax.inject.Inject

/**
 * A mérkőzés lezárása (Admin/Kapitány funkció).
 */
class FinalizeMatchUseCase @Inject constructor(
    private val repository: TeamMatchRepository
) {
    suspend operator fun invoke(matchId: Int) {
        repository.finalizeMatch(matchId)
    }
}