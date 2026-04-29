package hu.bme.aut.android.demo.data.network.api.match

import hu.bme.aut.android.demo.data.network.model.teamMatch.AddParticipantDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.LineupSubmitDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.ParticipantStatusUpdateDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.ScoreSubmitDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.TeamMatchDTO

/**
 * A mérkőzések hálózati műveleteinek elvont szerződése.
 * * A Repository ezt az interfészt használja a hálózati folyamatok irányítására.
 */
interface MatchApiService {
    suspend fun getTeamMatches(): List<TeamMatchDTO>
    suspend fun getTeamMatchById(matchId: Int): TeamMatchDTO
    suspend fun applyToMatch(matchId: Int)
    suspend fun withdrawFromMatch(matchId: Int)
    suspend fun updateParticipantStatus(participantId: Int, statusUpdate: ParticipantStatusUpdateDTO)
    suspend fun captainAddParticipantToMatch(matchId: Int, request: AddParticipantDTO)
    suspend fun submitLineup(matchId: Int, request: LineupSubmitDTO)
    suspend fun updateIndividualScore(individualMatchId: Int, request: ScoreSubmitDTO)
    suspend fun signMatch(matchId: Int)
    suspend fun finalizeMatch(matchId: Int)
}