package hu.bme.aut.android.demo.data.teammatch.repository

import hu.bme.aut.android.demo.data.network.api.RetrofitApi
import hu.bme.aut.android.demo.data.network.model.teamMatch.TeamMatchDTO
import hu.bme.aut.android.demo.data.network.model.teamMatch.mapper.toDomain
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import javax.inject.Inject

class TeamMatchRepositoryImpl @Inject constructor(
    private val retrofitApi: RetrofitApi
) : TeamMatchRepository {

    override suspend fun getTeamMatches(): List<TeamMatch> {
        // 1. Lekérjük a DTO-t az API-ból
        val dtoList = retrofitApi.getTeamMatches()

        // 2. Átalakítjuk Domain modellekre
        return dtoList.map { it.toDomain() }
    }
}