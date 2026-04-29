package hu.bme.aut.android.demo.data.network.api.team

import hu.bme.aut.android.demo.data.network.model.team.MemberDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamMemberOperationDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamUpdateDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamWithMembersDTO

interface TeamApiService {
    suspend fun getTeams(): List<TeamWithMembersDTO>
    suspend fun updateTeamName(teamId: Int, request: TeamUpdateDTO)
    suspend fun getAvailableUsers(): List<MemberDTO>
    suspend fun addTeamMember(teamId: Int, request: TeamMemberOperationDTO)
    suspend fun removeTeamMember(teamId: Int, userId: Int)
}