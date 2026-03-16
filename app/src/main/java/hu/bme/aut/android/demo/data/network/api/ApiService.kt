package hu.bme.aut.android.demo.data.network.api

import hu.bme.aut.android.demo.data.auth.model.UserDTO
import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import hu.bme.aut.android.demo.data.network.model.team.MemberDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamMemberOperationDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamUpdateDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamWithMembersDTO

interface ApiService {
    suspend fun registerFcmToken(registration: FcmToken)
    suspend fun sendPushNotification(payload: Map<String, String>)

    // Team
    suspend fun getTeams(): List<TeamWithMembersDTO>
    suspend fun updateTeamName(teamId: Int, request: TeamUpdateDTO)
    suspend fun getAvailableUsers(): List<MemberDTO>
    suspend fun addTeamMember(teamId: Int, request: TeamMemberOperationDTO)
    suspend fun removeTeamMember(teamId: Int, userId: Int)
    suspend fun signMatch(matchId: Int)

    // Auth
    suspend fun syncUser(user: UserDTO): UserDTO
    suspend fun updateUser(user: UserDTO): UserDTO
}