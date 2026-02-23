package hu.bme.aut.android.demo.domain.team.model

data class Team(
    val id: Int,
    val name: String,
    val clubName: String
)

data class TeamMember(
    val id: Int,
    val name: String,
    val isCaptain: Boolean
)

data class TeamDetails(
    val id: Int,
    val name: String,
    val clubName: String,
    val division: String?,
    val members: List<TeamMember>,
    val matchesPlayed: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val points: Int
)

// Extension függvény a dropdown-hoz
fun TeamDetails.toSimpleTeam(): Team {
    return Team(id = this.id, name = this.name, clubName = this.clubName)
}