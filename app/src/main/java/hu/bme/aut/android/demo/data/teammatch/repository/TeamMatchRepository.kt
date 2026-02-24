package hu.bme.aut.android.demo.data.teammatch.repository

import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

interface TeamMatchRepository {
    suspend fun getTeamMatches(): List<TeamMatch>
}