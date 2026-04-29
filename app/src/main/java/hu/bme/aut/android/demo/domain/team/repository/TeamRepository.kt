package hu.bme.aut.android.demo.domain.team.repository

import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import hu.bme.aut.android.demo.domain.team.model.TeamMember

/**
 * A Csapat funkciók tiszta szerződése a Domain rétegben.
 */
interface TeamRepository {
    suspend fun getTeams(): List<TeamDetails>
    suspend fun getAvailableUsers(): List<TeamMember>
    suspend fun addTeamMember(teamId: Int, userId: Int)
    suspend fun removeTeamMember(teamId: Int, userId: Int)
    suspend fun updateTeamName(teamId: Int, newName: String)
}