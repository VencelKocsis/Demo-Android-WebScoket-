package hu.bme.aut.android.demo.data.network.api

import hu.bme.aut.android.demo.data.auth.model.UserDTO
import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import hu.bme.aut.android.demo.data.network.model.team.MemberDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamMemberOperationDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamUpdateDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamWithMembersDTO
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val retrofitApi: RetrofitApi // A Retrofit által generált klienst kérjük el
) : ApiService {
    override suspend fun getPlayers(): List<PlayerDTO> = retrofitApi.getPlayers()
    override suspend fun addPlayer(player: NewPlayerDTO): PlayerDTO = retrofitApi.addPlayer(player)
    override suspend fun deletePlayer(id: Int) = retrofitApi.deletePlayer(id)
    override suspend fun updatePlayer(id: Int, player: NewPlayerDTO) = retrofitApi.updatePlayer(id, player)

    override suspend fun registerFcmToken(registration: FcmToken) = retrofitApi.registerFcmToken(registration)
    override suspend fun sendPushNotification(payload: Map<String, String>) = retrofitApi.sendPushNotification(payload)

    override suspend fun getTeams(): List<TeamWithMembersDTO> = retrofitApi.getTeams()
    override suspend fun updateTeamName(teamId: Int, request: TeamUpdateDTO) = retrofitApi.updateTeam(teamId, request)
    override suspend fun getAvailableUsers(): List<MemberDTO> = retrofitApi.getAvailableUsers()
    override suspend fun addTeamMember(teamId: Int, request: TeamMemberOperationDTO) = retrofitApi.addTeamMember(teamId, request)
    override suspend fun removeTeamMember(teamId: Int, userId: Int) = retrofitApi.removeTeamMember(teamId, userId)

    override suspend fun syncUser(user: UserDTO): UserDTO = retrofitApi.syncUser(user)
    override suspend fun updateUser(user: UserDTO): UserDTO = retrofitApi.updateUser(user)
}