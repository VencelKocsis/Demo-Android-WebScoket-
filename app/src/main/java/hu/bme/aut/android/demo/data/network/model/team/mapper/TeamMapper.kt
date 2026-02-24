package hu.bme.aut.android.demo.data.network.model.team.mapper

import hu.bme.aut.android.demo.data.network.model.team.MemberDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamWithMembersDTO
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import hu.bme.aut.android.demo.domain.team.model.TeamMember

fun TeamWithMembersDTO.toDomainDetails(): TeamDetails {
    return TeamDetails(
        id = this.teamId,
        name = this.teamName,
        clubName = this.clubName,
        division = this.division,
        members = this.members.map { it.toDomainMember() },
        matchesPlayed = this.matchesPlayed,
        wins = this.wins,
        losses = this.losses,
        draws = this.draws,
        points = this.points
    )
}

fun MemberDTO.toDomainMember(): TeamMember {
    return TeamMember(
        id = this.userId,
        name = this.name,
        isCaptain = this.isCaptain
    )
}