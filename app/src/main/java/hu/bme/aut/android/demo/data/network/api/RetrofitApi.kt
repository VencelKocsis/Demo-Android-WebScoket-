package hu.bme.aut.android.demo.data.network.api

import hu.bme.aut.android.demo.data.auth.model.UserDTO
import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import hu.bme.aut.android.demo.data.network.model.team.MemberDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamMemberOperationDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamUpdateDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.TeamMatchDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamWithMembersDTO
import hu.bme.aut.android.demo.domain.websocket.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RetrofitApi {
    @GET("players")
    suspend fun getPlayers(): List<PlayerDTO>

    @POST("players")
    suspend fun addPlayer(@Body player: NewPlayerDTO): PlayerDTO

    @DELETE("players/{id}")
    suspend fun deletePlayer(@Path("id") id: Int)

    @PUT("players/{id}")
    suspend fun updatePlayer(@Path("id") id: Int, @Body player: NewPlayerDTO)

    @POST("register_fcm_token")
    suspend fun registerFcmToken(@Body registration: FcmToken)

    @POST("send_fcm_notification")
    suspend fun sendPushNotification(@Body payload: Map<String, String>)

    @GET("teams")
    suspend fun getTeams(): List<TeamWithMembersDTO>

    @GET("matches")
    suspend fun getTeamMatches(): List<TeamMatchDTO>

    @POST("/auth/sync")
    suspend fun syncUser(@Body user: UserDTO): UserDTO

    @PUT("/auth/me")
    suspend fun updateUser(@Body user: UserDTO): UserDTO

    // --- Csapat szerkesztése ---
    @PUT("teams/{id}")
    suspend fun updateTeam(@Path("id") teamId: Int, @Body team: TeamUpdateDTO)

    @GET("users/available")
    suspend fun getAvailableUsers(): List<MemberDTO>

    @POST("teams/{id}/members")
    suspend fun addTeamMember(@Path("id") teamId: Int, @Body member: TeamMemberOperationDTO)

    @DELETE("teams/{id}/members/{userId}")
    suspend fun removeTeamMember(@Path("id") teamId: Int, @Path("userId") userId: Int)
}