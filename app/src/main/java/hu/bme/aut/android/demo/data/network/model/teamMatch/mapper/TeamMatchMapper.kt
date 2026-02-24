package hu.bme.aut.android.demo.data.network.model.teamMatch.mapper

import hu.bme.aut.android.demo.data.network.model.teamMatch.TeamMatchDTO
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

fun TeamMatchDTO.toDomain(): TeamMatch {
    return TeamMatch(
        id = this.id,
        seasonId = this.seasonId,
        roundNumber = this.roundNumber,
        homeTeamId = this.homeTeamId,
        guestTeamId = this.guestTeamId,
        homeTeamName = this.homeTeamName,
        guestTeamName = this.guestTeamName,
        homeTeamScore = this.homeTeamScore,
        guestTeamScore = this.guestTeamScore,
        location = this.location,
        matchDate = this.matchDate,
        status = this.status
    )
}