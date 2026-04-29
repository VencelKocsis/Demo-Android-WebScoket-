package hu.bme.aut.android.demo.data.network.api.match

import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

/**
 * A mérkőzések hálózati műveleteinek elvont szerződése.
 * * A Repository ezt az interfészt használja. Teljesen mentes a DTO-któl,
 * csak Domain modellekkel ([TeamMatch]) és primitív típusokkal dolgozik.
 */
interface MatchApiService {
    suspend fun getTeamMatches(): List<TeamMatch>
    suspend fun getTeamMatchById(matchId: Int): TeamMatch
    suspend fun applyToMatch(matchId: Int)
    suspend fun withdrawFromMatch(matchId: Int)
    suspend fun updateParticipantStatus(participantId: Int, status: String)
    suspend fun captainAddParticipantToMatch(matchId: Int, userId: Int)
    suspend fun submitLineup(matchId: Int, teamSide: String, positions: Map<Int, Int>)
    suspend fun updateIndividualScore(individualMatchId: Int, homeScore: Int, guestScore: Int, setScores: String, status: String)
    suspend fun signMatch(matchId: Int)
    suspend fun finalizeMatch(matchId: Int)
}