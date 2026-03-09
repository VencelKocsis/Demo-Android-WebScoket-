package hu.bme.aut.android.demo.domain.team.repository

import hu.bme.aut.android.demo.domain.team.model.TeamDetails

interface TeamRepository {
    suspend fun getTeams(): List<TeamDetails>
}