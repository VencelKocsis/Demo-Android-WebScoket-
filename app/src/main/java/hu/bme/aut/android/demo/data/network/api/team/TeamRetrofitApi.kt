package hu.bme.aut.android.demo.data.network.api.team

import hu.bme.aut.android.demo.data.network.model.team.MemberDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamMemberOperationDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamUpdateDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamWithMembersDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TeamRetrofitApi {
    @GET("teams")
    suspend fun getTeams(): List<TeamWithMembersDTO>
    @PUT("teams/{id}")
    suspend fun updateTeam(@Path("id") teamId: Int, @Body team: TeamUpdateDTO)

    @GET("users/available")
    suspend fun getAvailableUsers(): List<MemberDTO>

    @POST("teams/{id}/members")
    suspend fun addTeamMember(@Path("id") teamId: Int, @Body member: TeamMemberOperationDTO)

    @DELETE("teams/{id}/members/{userId}")
    suspend fun removeTeamMember(@Path("id") teamId: Int, @Path("userId") userId: Int)

}