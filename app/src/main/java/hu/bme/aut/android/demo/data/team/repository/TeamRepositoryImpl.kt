package hu.bme.aut.android.demo.data.team.repository

import hu.bme.aut.android.demo.data.network.api.RetrofitApi
import hu.bme.aut.android.demo.data.network.model.team.mapper.toDomainDetails
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val retrofitApi: RetrofitApi
) : TeamRepository {
    override suspend fun getTeams(): List<TeamDetails> {
        // 1. Lekérjük a DTO-kat
        val dtoList = retrofitApi.getTeams()

        // 2. Átalakítjuk a DTO-kat a domain modellekké a meglévő mapperrel
        return dtoList.map { it.toDomainDetails() }
    }
}