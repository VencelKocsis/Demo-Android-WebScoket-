package hu.bme.aut.android.demo.data.teammatch.repository

import hu.bme.aut.android.demo.data.network.api.RetrofitApi
import hu.bme.aut.android.demo.data.network.model.teamMatch.LineupSubmitDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.ParticipantStatusUpdateDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.ScoreSubmitDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.mapper.toDomain
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.domain.teammatch.repository.TeamMatchRepository
import javax.inject.Inject

class TeamMatchRepositoryImpl @Inject constructor(
    private val retrofitApi: RetrofitApi
) : TeamMatchRepository {

    override suspend fun getTeamMatches(): List<TeamMatch> {
        val dtoList = retrofitApi.getTeamMatches()
        return dtoList.map { it.toDomain() }
    }

    override suspend fun applyForMatch(matchId: Int) {
        retrofitApi.applyToMatch(matchId)
    }

    override suspend fun updateParticipantStatus(participantId: Int, status: String) {
        retrofitApi.updateParticipantStatus(participantId, ParticipantStatusUpdateDTO(status))
    }

    override suspend fun withdrawFromMatch(matchId: Int) {
        retrofitApi.withdrawFromMatch(matchId)
    }

    override suspend fun finalizeMatch(matchId: Int) {
        retrofitApi.finalizeMatch(matchId)
    }

    override suspend fun submitLineup(matchId: Int, teamSide: String, positions: Map<Int, Int>) {
        // Létrehozzuk a hálózati modellt (DTO)
        val request = LineupSubmitDTO(
            teamSide = teamSide,
            positions = positions
        )
        // Elküldjük a Retrofittel
        retrofitApi.submitLineup(matchId, request)
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
        retrofitApi.updateIndividualScore(individualMatchId, request)
    }
}