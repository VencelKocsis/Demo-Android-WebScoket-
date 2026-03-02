package hu.bme.aut.android.demo.data.auth.repository

import hu.bme.aut.android.demo.data.auth.model.UserDTO
import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.data.network.api.RetrofitApi
import hu.bme.aut.android.demo.data.network.model.team.MemberDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamMemberOperationDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamUpdateDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamWithMembersDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.TeamMatchDTO
import hu.bme.aut.android.demo.domain.websocket.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val retrofitApi: RetrofitApi // A Retrofit által generált klienst kérjük el
) : ApiService {
    override suspend fun getPlayers(): List<PlayerDTO> {
        return retrofitApi.getPlayers()
    }

    override suspend fun addPlayer(player: NewPlayerDTO): PlayerDTO {
        return retrofitApi.addPlayer(player)
    }

    override suspend fun deletePlayer(id: Int) {
        return retrofitApi.deletePlayer(id)
    }

    override suspend fun updatePlayer(
        id: Int,
        player: NewPlayerDTO
    ) {
        return retrofitApi.updatePlayer(id, player)
    }

    override suspend fun registerFcmToken(registration: FcmToken) {
        return retrofitApi.registerFcmToken(registration)
    }

    override suspend fun sendPushNotification(payload: Map<String, String>) {
        retrofitApi.sendPushNotification(payload)
    }

    override suspend fun getTeams(): List<TeamWithMembersDTO> {
        return retrofitApi.getTeams()
    }

    override suspend fun updateTeamName(
        teamId: Int,
        request: TeamUpdateDTO
    ) {
        retrofitApi.updateTeam(teamId, request)
    }

    override suspend fun getAvailableUsers(): List<MemberDTO> {
        return retrofitApi.getAvailableUsers()
    }

    override suspend fun addTeamMember(
        teamId: Int,
        request: TeamMemberOperationDTO
    ) {
        retrofitApi.addTeamMember(teamId, request)
    }

    override suspend fun removeTeamMember(teamId: Int, userId: Int) {
        retrofitApi.removeTeamMember(teamId, userId)
    }

    override suspend fun getTeamMatches(): List<TeamMatchDTO> {
        return retrofitApi.getTeamMatches()
    }

    override suspend fun syncUser(user: UserDTO): UserDTO {
        return retrofitApi.syncUser(user)
    }

    override suspend fun updateUser(user: UserDTO): UserDTO {
        return retrofitApi.updateUser(user)
    }
}