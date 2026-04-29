package hu.bme.aut.android.demo.data.team.repository

import hu.bme.aut.android.demo.data.network.api.team.TeamApiService
import hu.bme.aut.android.demo.data.team.mapper.toDomainDetails
import hu.bme.aut.android.demo.data.team.mapper.toDomainMember
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import hu.bme.aut.android.demo.domain.team.model.TeamMember
import hu.bme.aut.android.demo.domain.team.repository.TeamRepository
import javax.inject.Inject

/**
 * A csapat adatok lekérdezését és szerkesztését végző implementáció a Data rétegben.
 */
class TeamRepositoryImpl @Inject constructor(
    private val teamApiService: TeamApiService
) : TeamRepository {

    override suspend fun getTeams(): List<TeamDetails> {
        return teamApiService.getTeams().map { it.toDomainDetails() }
    }

    override suspend fun getAvailableUsers(): List<TeamMember> {
        return teamApiService.getAvailableUsers().map { it.toDomainMember() }
    }

    override suspend fun addTeamMember(teamId: Int, userId: Int) {
        teamApiService.addTeamMember(teamId, userId)
    }

    override suspend fun removeTeamMember(teamId: Int, userId: Int) {
        teamApiService.removeTeamMember(teamId, userId)
    }

    override suspend fun updateTeamName(teamId: Int, newName: String) {
        teamApiService.updateTeamName(teamId, newName)
    }
}