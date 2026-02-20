package hu.bme.aut.android.demo.data.team.repository

import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.data.network.model.TeamWithMembersDTO
import hu.bme.aut.android.demo.domain.team.repository.TeamRepository
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : TeamRepository {
    override suspend fun getTeams(): List<TeamWithMembersDTO> {
        return apiService.getTeams()
    }
}