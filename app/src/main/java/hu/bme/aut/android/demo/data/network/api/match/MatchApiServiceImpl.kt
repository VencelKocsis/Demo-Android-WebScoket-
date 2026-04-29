package hu.bme.aut.android.demo.data.network.api.match

import hu.bme.aut.android.demo.data.network.model.teamMatch.AddParticipantDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.LineupSubmitDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.ParticipantStatusUpdateDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.ScoreSubmitDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.TeamMatchDTO
import javax.inject.Inject

class MatchApiServiceImpl @Inject constructor(
    private val matchRetrofitApi: MatchRetrofitApi
) : MatchApiService {
    override suspend fun getTeamMatches(): List<TeamMatchDTO> = matchRetrofitApi.getTeamMatches()
    override suspend fun getTeamMatchById(matchId: Int): TeamMatchDTO = matchRetrofitApi.getTeamMatchById(matchId)
    override suspend fun applyToMatch(matchId: Int) = matchRetrofitApi.applyToMatch(matchId)
    override suspend fun withdrawFromMatch(matchId: Int) = matchRetrofitApi.withdrawFromMatch(matchId)
    override suspend fun updateParticipantStatus(participantId: Int, statusUpdate: ParticipantStatusUpdateDTO) = matchRetrofitApi.updateParticipantStatus(participantId, statusUpdate)
    override suspend fun captainAddParticipantToMatch(matchId: Int, request: AddParticipantDTO) = matchRetrofitApi.captainAddParticipantToMatch(matchId, request)
    override suspend fun submitLineup(matchId: Int, request: LineupSubmitDTO) = matchRetrofitApi.submitLineup(matchId, request)
    override suspend fun updateIndividualScore(individualMatchId: Int, request: ScoreSubmitDTO) = matchRetrofitApi.updateIndividualScore(individualMatchId, request)
    override suspend fun signMatch(matchId: Int) = matchRetrofitApi.signMatch(matchId)
    override suspend fun finalizeMatch(matchId: Int) = matchRetrofitApi.finalizeMatch(matchId)
}