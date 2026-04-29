package hu.bme.aut.android.demo.navigation

import kotlinx.serialization.Serializable

/**
 * Type-Safe (típusbiztos) navigációs útvonalak a Compose Navigation 2.8.0+ verziójához.
 * * A String alapú útvonalak helyett [@Serializable] Kotlin objektumokat használunk,
 * így a fordító ellenőrzi a paraméterek típusát és meglétét.
 */

// ==========================================
// FŐ GYÖKÉR (ROOT) KÉPERNYŐK
// ==========================================
@Serializable
data object Login

@Serializable
data object Main


// ==========================================
// ALSÓ MENÜS (BOTTOM NAV) KÉPERNYŐK
// ==========================================
@Serializable
data object Tournament

@Serializable
data object Team

@Serializable
data object History

@Serializable
data object Profile

@Serializable
data object Leaderboard


// ==========================================
// RÉSZLETEZŐ / PARAMÉTERES KÉPERNYŐK
// ==========================================
@Serializable
data class TeamEditor(val teamId: Int)

@Serializable
data class MatchDetails(val matchId: Int)

@Serializable
data class LiveMatch(val matchId: Int)

@Serializable
data class MatchScorer(val matchId: Int, val individualMatchId: Int)

@Serializable
data class PlayerProfile(val playerId: String)

@Serializable
data class RacketEditor(val racketId: Int? = null) // null = új ütő felvitele

@Serializable
data object Market