package hu.bme.aut.android.demo.data.network.api.match

import hu.bme.aut.android.demo.data.network.model.teamMatch.AddParticipantDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.LineupSubmitDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.ParticipantStatusUpdateDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.ScoreSubmitDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.TeamMatchDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * A mérkőzésekkel kapcsolatos műveletek alacsony szintű (Retrofit) hálózati interfésze.
 */
interface MatchRetrofitApi {
    @POST("matches/{matchId}/apply")
    suspend fun applyToMatch(@Path("matchId") matchId: Int)

    @PUT("matches/participants/{participantId}/status")
    suspend fun updateParticipantStatus(@Path("participantId") participantId: Int, @Body statusUpdate: ParticipantStatusUpdateDTO)

    @POST("/matches/{matchId}/participants")
    suspend fun captainAddParticipantToMatch(@Path("matchId") matchId: Int, @Body request: AddParticipantDTO)

    @POST("matches/{matchId}/lineup")
    suspend fun submitLineup(@Path("matchId") matchId: Int, @Body request: LineupSubmitDTO)

    @PUT("matches/individual/{id}/score")
    suspend fun updateIndividualScore(@Path("id") individualMatchId: Int, @Body request: ScoreSubmitDTO)

    @POST("/matches/{matchId}/sign")
    suspend fun signMatch(@Path("matchId") matchId: Int)

    @DELETE("matches/{matchId}/apply")
    suspend fun withdrawFromMatch(@Path("matchId") matchId: Int)

    @POST("matches/{matchId}/finalize")
    suspend fun finalizeMatch(@Path("matchId") matchId: Int)

    @GET("matches")
    suspend fun getTeamMatches(): List<TeamMatchDTO>

    @GET("matches/{id}")
    suspend fun getTeamMatchById(@Path("id") matchId: Int): TeamMatchDTO
}