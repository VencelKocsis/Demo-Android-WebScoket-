package hu.bme.aut.android.demo.data.team.repository

import hu.bme.aut.android.demo.data.network.api.team.TeamRetrofitApi
import hu.bme.aut.android.demo.data.network.model.team.mapper.toDomainDetails
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import hu.bme.aut.android.demo.domain.team.repository.TeamRepository
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val teamRetrofitApi: TeamRetrofitApi
) : TeamRepository {
    override suspend fun getTeams(): List<TeamDetails> {
        // 1. Lekérjük a DTO-kat
        val dtoList = teamRetrofitApi.getTeams()

        // 2. Átalakítjuk a DTO-kat a domain modellekké a meglévő mapperrel
        return dtoList.map { it.toDomainDetails() }
    }
}