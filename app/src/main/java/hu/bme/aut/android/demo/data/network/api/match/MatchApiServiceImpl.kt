package hu.bme.aut.android.demo.data.network.api.match

import hu.bme.aut.android.demo.data.network.model.teamMatch.AddParticipantDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.LineupSubmitDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.ParticipantStatusUpdateDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.ScoreSubmitDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.mapper.toDomain
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import javax.inject.Inject

/**
 * A [MatchApiService] konkrét megvalósítása ("A Tolmács").
 * * Becsomagolja a Repository-tól kapott egyszerű adatokat DTO objektumokká,
 * és kiküldi őket a Retrofit felé. A válaszokat pedig Domain modellekké alakítja.
 */
class MatchApiServiceImpl @Inject constructor(
    private val matchRetrofitApi: MatchRetrofitApi
) : MatchApiService {

    override suspend fun getTeamMatches(): List<TeamMatch> =
        matchRetrofitApi.getTeamMatches().map { it.toDomain() }

    override suspend fun getTeamMatchById(matchId: Int): TeamMatch =
        matchRetrofitApi.getTeamMatchById(matchId).toDomain()

    override suspend fun applyToMatch(matchId: Int) = matchRetrofitApi.applyToMatch(matchId)

    override suspend fun withdrawFromMatch(matchId: Int) = matchRetrofitApi.withdrawFromMatch(matchId)

    override suspend fun updateParticipantStatus(participantId: Int, status: String) {
        matchRetrofitApi.updateParticipantStatus(participantId, ParticipantStatusUpdateDTO(status))
    }

    override suspend fun captainAddParticipantToMatch(matchId: Int, userId: Int) {
        matchRetrofitApi.captainAddParticipantToMatch(matchId, AddParticipantDTO(userId))
    }

    override suspend fun submitLineup(matchId: Int, teamSide: String, positions: Map<Int, Int>) {
        matchRetrofitApi.submitLineup(matchId, LineupSubmitDTO(teamSide, positions))
    }

    override suspend fun updateIndividualScore(individualMatchId: Int, homeScore: Int, guestScore: Int, setScores: String, status: String) {
        val request = ScoreSubmitDTO(homeScore, guestScore, setScores, status)
        matchRetrofitApi.updateIndividualScore(individualMatchId, request)
    }

    override suspend fun signMatch(matchId: Int) = matchRetrofitApi.signMatch(matchId)

    override suspend fun finalizeMatch(matchId: Int) = matchRetrofitApi.finalizeMatch(matchId)
}