package hu.bme.aut.android.demo.feature.leaderboard

import hu.bme.aut.android.demo.domain.team.model.TeamDetails

/**
 * A Ranglista képernyő (LeaderboardScreen) egyetlen igazságforrása.
 * Tartalmazza a betöltési állapotot, az összes számított csapatot, a szűrt listát,
 * valamint a szűrők (szezon, divízió) aktuális állapotát.
 */
data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val allTeams: List<TeamDetails> = emptyList(),
    val filteredTeams: List<TeamDetails> = emptyList(),

    val availableSeasons: List<Pair<Int, String>> = emptyList(),
    val availableDivisions: List<String> = emptyList(),

    val selectedSeasonId: Int? = null,
    val selectedDivision: String? = null
)

/**
 * MVI (Model-View-Intent) események a Ranglista képernyőhöz.
 * * Ezeket a felhasználói cselekvéseket küldi a UI a ViewModel felé.
 */
sealed class LeaderboardEvent {
    object LoadStandings : LeaderboardEvent()
    data class OnSeasonSelected(val seasonId: Int?) : LeaderboardEvent()
    data class OnDivisionSelected(val division: String?) : LeaderboardEvent()
}