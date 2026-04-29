package hu.bme.aut.android.demo.domain.teammatch.usecase

import hu.bme.aut.android.demo.domain.teammatch.repository.TeamMatchRepository
import javax.inject.Inject

/**
 * Játékos jelentkezése egy adott mérkőzésre.
 */
class ApplyForMatchUseCase @Inject constructor(
    private val repository: TeamMatchRepository
) {
    suspend operator fun invoke(matchId: Int) {
        repository.applyForMatch(matchId)
    }
}