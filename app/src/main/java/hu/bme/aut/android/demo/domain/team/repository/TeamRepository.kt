package hu.bme.aut.android.demo.domain.team.repository

import hu.bme.aut.android.demo.data.network.model.TeamWithMembersDTO

interface TeamRepository {
    suspend fun getTeams(): List<TeamWithMembersDTO>
}