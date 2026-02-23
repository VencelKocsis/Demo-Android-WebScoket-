package hu.bme.aut.android.demo.data.team.repository

import hu.bme.aut.android.demo.data.network.api.RetrofitApi
import hu.bme.aut.android.demo.data.network.model.TeamWithMembersDTO
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val retrofitApi: RetrofitApi
) : TeamRepository {
    override suspend fun getTeams(): List<TeamWithMembersDTO> {
        return retrofitApi.getTeams()
    }
}