package hu.bme.aut.android.demo.domain.team.model

data class Team(
    val id: Int,
    val name: String,
    val clubName: String
)

data class TeamMember(
    val id: Int,
    val uid: String,
    val name: String,
    val isCaptain: Boolean
)

// külön adatmodell a statisztikákhoz
data class TeamStats(
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val points: Int = 0
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
    val points: Int,

    val seasonStats: Map<Int, TeamStats> = emptyMap() // Szezononkénti statisztikák (szezonId -> TeamStats
) {
    // Segégfüggvény a UI-nak
    fun getStats(seasonId: Int?): TeamStats {
        return if (seasonId == null) {
            TeamStats(matchesPlayed, wins, losses, draws, points)
        } else {
            seasonStats[seasonId] ?: TeamStats() // Ha nincs adat a szezonhoz, visszaadunk egy üres statisztikát
        }
    }
}

// Extension függvény a dropdown-hoz
fun TeamDetails.toSimpleTeam(): Team {
    return Team(id = this.id, name = this.name, clubName = this.clubName)
}