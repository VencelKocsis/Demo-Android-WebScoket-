package hu.bme.aut.android.demo.data.network.api.team

import hu.bme.aut.android.demo.data.network.model.team.TeamMemberOperationDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamUpdateDTO
import javax.inject.Inject

/**
 * A [TeamApiService] megvalósítása, amely elrejti a Retrofit függőséget a Repository elől.
 */
class TeamApiServiceImpl @Inject constructor(
    private val retrofitApi: TeamRetrofitApi
) : TeamApiService {
    override suspend fun getTeams() = retrofitApi.getTeams()
    override suspend fun updateTeamName(teamId: Int, request: TeamUpdateDTO) = retrofitApi.updateTeam(teamId, request)
    override suspend fun getAvailableUsers() = retrofitApi.getAvailableUsers()
    override suspend fun addTeamMember(teamId: Int, request: TeamMemberOperationDTO) = retrofitApi.addTeamMember(teamId, request)
    override suspend fun removeTeamMember(teamId: Int, userId: Int) = retrofitApi.removeTeamMember(teamId, userId)
}