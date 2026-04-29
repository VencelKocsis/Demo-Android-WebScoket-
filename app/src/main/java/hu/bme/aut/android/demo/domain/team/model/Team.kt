package hu.bme.aut.android.demo.domain.team.model

/**
 * A csapat alapvető, egyszerűsített üzleti modellje (pl. lenyíló listákhoz).
 */
data class Team(
    val id: Int,
    val name: String,
    val clubName: String
)

/**
 * Egy csapattag tiszta üzleti modellje.
 */
data class TeamMember(
    val id: Int,
    val uid: String, // Firebase azonosító
    val name: String,
    val isCaptain: Boolean
)

/**
 * Külön adatmodell a csapat statisztikáihoz (győzelmek, vereségek, pontok).
 */
data class TeamStats(
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val points: Int = 0
)

/**
 * A csapat részletes, tiszta üzleti modellje.
 * * Ez az osztály teljesen független a JSON formátumoktól vagy DTO-któl.
 * A UI és a UseCase-ek kizárólag ezt használják.
 */
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

    val seasonStats: Map<Int, TeamStats> = emptyMap() // Szezononkénti statisztikák (szezonId -> TeamStats)
) {
    /**
     * Segédfüggvény a UI-nak: Visszaadja a kért szezon statisztikáit,
     * vagy az összesített (alap) statisztikát, ha a szezonId null.
     */
    fun getStats(seasonId: Int?): TeamStats {
        return if (seasonId == null) {
            TeamStats(matchesPlayed, wins, losses, draws, points)
        } else {
            seasonStats[seasonId] ?: TeamStats() // Ha nincs adat a szezonhoz, visszaadunk egy üres statisztikát
        }
    }
}

/**
 * Extension (kiterjesztő) függvény a [TeamDetails]-hez.
 * * Segítségével a részletes modellből könnyen generálhatunk egyszerű [Team] modellt
 * (például egy dropdown menü feltöltéséhez).
 */
fun TeamDetails.toSimpleTeam(): Team {
    return Team(id = this.id, name = this.name, clubName = this.clubName)
}