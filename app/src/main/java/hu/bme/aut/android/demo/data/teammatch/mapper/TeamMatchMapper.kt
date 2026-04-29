package hu.bme.aut.android.demo.data.teammatch.mapper

import hu.bme.aut.android.demo.data.teammatch.model.TeamMatchDTO
import hu.bme.aut.android.demo.domain.teammatch.model.IndividualMatch
import hu.bme.aut.android.demo.domain.teammatch.model.MatchParticipant
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

/**
 * A hálózati adatátviteli objektumot ([TeamMatchDTO]) alakítja át a tiszta
 * [TeamMatch] Domain modellé.
 * * Mivel a modell komplex, a belső listákat ([IndividualMatchDTO], [MatchParticipantDTO])
 * is iterálva Domain modellekké kell alakítani.
 */
fun TeamMatchDTO.toDomain(): TeamMatch {
    return TeamMatch(
        id = this.id,
        seasonId = this.seasonId,
        seasonName = this.seasonName ?: "Ismeretlen szezon",
        roundNumber = this.roundNumber,
        homeTeamName = this.homeTeamName,
        guestTeamName = this.guestTeamName,
        homeTeamScore = this.homeScore,
        guestTeamScore = this.guestScore,
        matchDate = this.date,
        status = this.status,
        location = this.location,
        homeTeamId = this.homeTeamId,
        guestTeamId = this.guestTeamId,
        homeTeamSigned = this.homeTeamSigned,
        guestTeamSigned = this.guestTeamSigned,

        // A belső (beágyazott) egyéni meccs listák konvertálása
        individualMatches = this.individualMatches?.map { dto ->
            IndividualMatch(
                id = dto.id,
                homePlayerName = dto.homePlayerName,
                guestPlayerName = dto.guestPlayerName,
                homeScore = dto.homeScore,
                guestScore = dto.guestScore,
                setScores = dto.setScores,
                status = dto.status ?: "pending",
                orderNumber = dto.orderNumber
            )
        } ?: emptyList(),

        // A belső (beágyazott) résztvevők listájának konvertálása
        participants = this.participants?.map { dto ->
            MatchParticipant(
                id = dto.id,
                userId = dto.userId,
                firebaseUid = dto.firebaseUid,
                playerName = dto.playerName,
                teamSide = dto.teamSide,
                status = dto.status,
                position = dto.position
            )
        } ?: emptyList()
    )
}