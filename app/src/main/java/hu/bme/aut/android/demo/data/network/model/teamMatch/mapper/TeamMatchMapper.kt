package hu.bme.aut.android.demo.data.network.model.teamMatch.mapper

import hu.bme.aut.android.demo.data.network.model.teamMatch.TeamMatchDTO
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

fun TeamMatchDTO.toDomain(): TeamMatch {
    return TeamMatch(
        id = this.id,
        roundNumber = this.roundNumber,
        homeTeamName = this.homeTeamName,
        guestTeamName = this.guestTeamName,
        homeTeamScore = this.homeScore,
        guestTeamScore = this.guestScore,
        matchDate = this.date,
        status = this.status,
        location = this.location,
        seasonId = this.seasonId,
        homeTeamId = this.homeTeamId,
        guestTeamId = this.guestTeamId
        // TODO home and guest team members list
    )
}