package hu.bme.aut.android.demo.data.network.api.team

import hu.bme.aut.android.demo.data.team.model.MemberDTO
import hu.bme.aut.android.demo.data.team.model.TeamWithMembersDTO

/**
 * A csapatok hálózati műveleteinek tiszta szerződése.
 * A [TeamRepository] ezt használja a hálózati réteg elérésére.
 */
interface TeamApiService {
    suspend fun getTeams(): List<TeamWithMembersDTO>
    suspend fun updateTeamName(teamId: Int, newName: String)
    suspend fun getAvailableUsers(): List<MemberDTO>
    suspend fun addTeamMember(teamId: Int, userId: Int)
    suspend fun removeTeamMember(teamId: Int, userId: Int)
}