package hu.bme.aut.android.demo.data.network.model.teamMatch.mapper

import hu.bme.aut.android.demo.data.network.model.teamMatch.TeamMatchDTO
import hu.bme.aut.android.demo.domain.teammatch.model.IndividualMatch
import hu.bme.aut.android.demo.domain.teammatch.model.MatchParticipant
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
        guestTeamId = this.guestTeamId,
        individualMatches = this.individualMatches?.map { dto ->
            IndividualMatch(
                homePlayerName = dto.homePlayerName,
                guestPlayerName = dto.guestPlayerName,
                homeScore = dto.homeScore,
                guestScore = dto.guestScore
            )
        } ?: emptyList(),

        participants = this.participants?.map { dto ->
            MatchParticipant(
                id = dto.id,
                playerName = dto.playerName,
                teamSide = dto.teamSide,
                status = dto.status
            )
        } ?: emptyList()
    )
}