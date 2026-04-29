package hu.bme.aut.android.demo.data.teammatch.repository

import hu.bme.aut.android.demo.data.network.api.match.MatchRetrofitApi
import hu.bme.aut.android.demo.data.network.model.teamMatch.AddParticipantDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.LineupSubmitDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.ParticipantStatusUpdateDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.ScoreSubmitDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.mapper.toDomain
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.domain.teammatch.repository.TeamMatchRepository
import javax.inject.Inject

class TeamMatchRepositoryImpl @Inject constructor(
    private val teamMatchRetrofitApi: MatchRetrofitApi
) : TeamMatchRepository {

    override suspend fun getTeamMatches(): List<TeamMatch> {
        val dtoList = teamMatchRetrofitApi.getTeamMatches()
        return dtoList.map { it.toDomain() }
    }

    override suspend fun getTeamMatchById(matchId: Int): TeamMatch {
        val dto = teamMatchRetrofitApi.getTeamMatchById(matchId)
        return dto.toDomain()
    }

    override suspend fun applyForMatch(matchId: Int) {
        teamMatchRetrofitApi.applyToMatch(matchId)
    }

    override suspend fun updateParticipantStatus(participantId: Int, status: String) {
        teamMatchRetrofitApi.updateParticipantStatus(participantId, ParticipantStatusUpdateDTO(status))
    }

    override suspend fun captainAddParticipantToMatch(matchId: Int, userId: Int) {
        val request = AddParticipantDTO(userId = userId)
        teamMatchRetrofitApi.captainAddParticipantToMatch(matchId, request)
    }

    override suspend fun withdrawFromMatch(matchId: Int) {
        teamMatchRetrofitApi.withdrawFromMatch(matchId)
    }

    override suspend fun finalizeMatch(matchId: Int) {
        teamMatchRetrofitApi.finalizeMatch(matchId)
    }

    override suspend fun submitLineup(matchId: Int, teamSide: String, positions: Map<Int, Int>) {
        // Létrehozzuk a hálózati modellt (DTO)
        val request = LineupSubmitDTO(
            teamSide = teamSide,
            positions = positions
        )
        // Elküldjük a Retrofittel
        teamMatchRetrofitApi.submitLineup(matchId, request)
    }

    override suspend fun updateIndividualScore(
        individualMatchId: Int,
        homeScore: Int,
        guestScore: Int,
        setScores: String,
        status: String
    ) {
        // 1. Létrehozzuk a DTO-t a hálózati küldéshez
        val request = ScoreSubmitDTO(
            homeScore = homeScore,
            guestScore = guestScore,
            setScores = setScores,
            status = status
        )

        // 2. Elküldjük a Retrofittel
        teamMatchRetrofitApi.updateIndividualScore(individualMatchId, request)
    }

    override suspend fun signMatch(matchId: Int) {
        teamMatchRetrofitApi.signMatch(matchId)
    }
}