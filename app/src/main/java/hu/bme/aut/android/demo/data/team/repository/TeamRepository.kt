package hu.bme.aut.android.demo.data.team.repository

import hu.bme.aut.android.demo.domain.team.model.TeamDetails

interface TeamRepository {
    suspend fun getTeams(): List<TeamDetails>
}