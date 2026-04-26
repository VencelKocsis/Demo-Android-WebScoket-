package hu.bme.aut.android.demo.data.network.api

import hu.bme.aut.android.demo.data.auth.model.UserDTO
import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import hu.bme.aut.android.demo.data.network.model.team.MemberDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamMemberOperationDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamUpdateDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamWithMembersDTO
import hu.bme.aut.android.demo.data.racket.model.RacketDTO
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val retrofitApi: RetrofitApi // A Retrofit által generált klienst kérjük el
) : ApiService {
    override suspend fun registerFcmToken(registration: FcmToken) = retrofitApi.registerFcmToken(registration)
    override suspend fun sendPushNotification(payload: Map<String, String>) = retrofitApi.sendPushNotification(payload)

    override suspend fun getTeams(): List<TeamWithMembersDTO> = retrofitApi.getTeams()
    override suspend fun updateTeamName(teamId: Int, request: TeamUpdateDTO) = retrofitApi.updateTeam(teamId, request)
    override suspend fun getAvailableUsers(): List<MemberDTO> = retrofitApi.getAvailableUsers()
    override suspend fun addTeamMember(teamId: Int, request: TeamMemberOperationDTO) = retrofitApi.addTeamMember(teamId, request)
    override suspend fun removeTeamMember(teamId: Int, userId: Int) = retrofitApi.removeTeamMember(teamId, userId)
    override suspend fun signMatch(matchId: Int) = retrofitApi.signMatch(matchId)

    override suspend fun syncUser(user: UserDTO): UserDTO = retrofitApi.syncUser(user)
    override suspend fun updateUser(user: UserDTO): UserDTO = retrofitApi.updateUser(user)
    override suspend fun getUserById(uid: String): UserDTO = retrofitApi.getUserById(uid)

    override suspend fun saveEquipment(racketDTO: RacketDTO) = retrofitApi.saveEquipment(racketDTO)

    override suspend fun deleteEquipment(racketId: Int) = retrofitApi.deleteEquipment(racketId)
}