package hu.bme.aut.android.demo.data.network.api.team

import hu.bme.aut.android.demo.data.team.model.TeamMemberOperationDTO
import hu.bme.aut.android.demo.data.team.model.TeamUpdateDTO
import javax.inject.Inject

/**
 * A [TeamApiService] megvalósítása, amely a kapott primitív adatokat
 * DTO formátumra csomagolja, és elrejti a Retrofit függőséget a Repository elől.
 */
class TeamApiServiceImpl @Inject constructor(
    private val retrofitApi: TeamRetrofitApi
) : TeamApiService {
    override suspend fun getTeams() = retrofitApi.getTeams()

    override suspend fun updateTeamName(teamId: Int, newName: String) =
        retrofitApi.updateTeam(teamId, TeamUpdateDTO(newName))

    override suspend fun getAvailableUsers() = retrofitApi.getAvailableUsers()

    override suspend fun addTeamMember(teamId: Int, userId: Int) =
        retrofitApi.addTeamMember(teamId, TeamMemberOperationDTO(userId))

    override suspend fun removeTeamMember(teamId: Int, userId: Int) =
        retrofitApi.removeTeamMember(teamId, userId)
}