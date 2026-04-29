package hu.bme.aut.android.demo.domain.teammatch.repository

import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

/**
 * A mérkőzésekkel kapcsolatos adatelérési műveletek tiszta szerződése a Domain rétegben.
 * * Elrejti az üzleti logika elől, hogy az adatok honnan származnak (pl. REST API, WebSocket, Mock).
 */
interface TeamMatchRepository {
    suspend fun getTeamMatches(): List<TeamMatch>
    suspend fun getTeamMatchById(matchId: Int): TeamMatch
    suspend fun applyForMatch(matchId: Int)
    suspend fun updateParticipantStatus(participantId: Int, status: String)
    suspend fun captainAddParticipantToMatch(matchId: Int, userId: Int)
    suspend fun withdrawFromMatch(matchId: Int)
    suspend fun finalizeMatch(matchId: Int)
    suspend fun submitLineup(matchId: Int, teamSide: String, positions: Map<Int, Int>)
    suspend fun updateIndividualScore(individualMatchId: Int, homeScore: Int, guestScore: Int, setScores: String, status: String)
    suspend fun signMatch(matchId: Int)
}