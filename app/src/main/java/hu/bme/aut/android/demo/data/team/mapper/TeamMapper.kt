package hu.bme.aut.android.demo.data.team.mapper

import hu.bme.aut.android.demo.data.team.model.MemberDTO
import hu.bme.aut.android.demo.data.team.model.TeamWithMembersDTO
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import hu.bme.aut.android.demo.domain.team.model.TeamMember

/**
 * A hálózati adatátviteli objektumot (DTO) alakítja át a tiszta [TeamDetails] Domain modellé.
 * * Ez biztosítja, hogy a Domain réteg (és a UI) sose találkozzon a hálózati formátummal.
 */
fun TeamWithMembersDTO.toDomainDetails(): TeamDetails {
    return TeamDetails(
        id = this.teamId,
        name = this.teamName,
        clubName = this.clubName,
        division = this.division,
        members = this.members.map { it.toDomainMember() }, // A lista elemeit is mappoljuk
        matchesPlayed = this.matchesPlayed,
        wins = this.wins,
        losses = this.losses,
        draws = this.draws,
        points = this.points
    )
}

/**
 * A hálózati csapattag objektumot (DTO) alakítja át a tiszta [TeamMember] Domain modellé.
 */
fun MemberDTO.toDomainMember(): TeamMember {
    return TeamMember(
        id = this.userId,
        uid = this.firebaseUid,
        name = this.name,
        isCaptain = this.isCaptain
    )
}