package hu.bme.aut.android.demo.navigation

import kotlinx.serialization.Serializable

// --- FŐ KÉPERNYŐK ---
@Serializable
data object Login

@Serializable
data object Main

// --- ALSÓ MENÜS (BOTTOM NAV) KÉPERNYŐK ---
@Serializable
data object Tournament

@Serializable
data object Team

@Serializable
data object History

@Serializable
data object Profile

// --- PARAMÉTERES KÉPERNYŐK ---
@Serializable
data class TeamEditor(val teamId: Int)

@Serializable
data class MatchDetails(val matchId: Int)

@Serializable
data class LiveMatch(val matchId: Int)

@Serializable
data class MatchScorer(val matchId: Int, val individualMatchId: Int)