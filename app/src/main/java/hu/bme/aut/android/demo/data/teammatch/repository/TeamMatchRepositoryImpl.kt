package hu.bme.aut.android.demo.data.teammatch.repository

import hu.bme.aut.android.demo.data.network.api.match.MatchApiService
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.domain.teammatch.repository.TeamMatchRepository
import javax.inject.Inject

/**
 * A [TeamMatchRepository] interfész megvalósítása a Data rétegben.
 * * Mivel a [MatchApiService] már elvégezte a DTO-k gyártását és a mappolást,
 * ez az osztály hihetetlenül tiszta maradt: csak hívja a Service-t.
 */
class TeamMatchRepositoryImpl @Inject constructor(
    private val matchApiService: MatchApiService
) : TeamMatchRepository {

    override suspend fun getTeamMatches(): List<TeamMatch> = matchApiService.getTeamMatches()

    override suspend fun getTeamMatchById(matchId: Int): TeamMatch = matchApiService.getTeamMatchById(matchId)

    override suspend fun applyForMatch(matchId: Int) = matchApiService.applyToMatch(matchId)

    override suspend fun updateParticipantStatus(participantId: Int, status: String) =
        matchApiService.updateParticipantStatus(participantId, status)

    override suspend fun captainAddParticipantToMatch(matchId: Int, userId: Int) =
        matchApiService.captainAddParticipantToMatch(matchId, userId)

    override suspend fun withdrawFromMatch(matchId: Int) = matchApiService.withdrawFromMatch(matchId)

    override suspend fun finalizeMatch(matchId: Int) = matchApiService.finalizeMatch(matchId)

    override suspend fun submitLineup(matchId: Int, teamSide: String, positions: Map<Int, Int>) =
        matchApiService.submitLineup(matchId, teamSide, positions)

    override suspend fun updateIndividualScore(individualMatchId: Int, homeScore: Int, guestScore: Int, setScores: String, status: String) =
        matchApiService.updateIndividualScore(individualMatchId, homeScore, guestScore, setScores, status)

    override suspend fun signMatch(matchId: Int) = matchApiService.signMatch(matchId)
}