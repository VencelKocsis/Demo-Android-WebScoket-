package hu.bme.aut.android.demo.feature.history

import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

/**
 * A History (Előzmények) képernyő egyetlen igazságforrása.
 * Tartalmazza a betöltési állapotokat, az összes meccset, a szűrt meccseket,
 * és a szűrő menük (dropdown) opcióit.
 */
data class HistoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val allMatches: List<TeamMatch> = emptyList(),
    val filteredMatches: List<TeamMatch> = emptyList(),

    val availableSeasons: List<Pair<Int, String>> = emptyList(),
    val availableDivisions: List<String> = emptyList(),
    val availableTeams: List<Pair<Int, String>> = emptyList(),

    // Segéd-map a gyors szűréshez (CsapatId -> Divízió Név)
    val teamDivisions: Map<Int, String> = emptyMap(),

    val selectedSeasonId: Int? = null,
    val selectedDivision: String? = null,
    val selectedTeamId: Int? = null
)

/**
 * MVI (Model-View-Intent) események (Intents).
 * Ezek a felhasználói cselekvések, amiket a UI küld a ViewModel felé.
 * Így a ViewModel API-ja egyetlen [onEvent] függvényre redukálódik.
 */
sealed class HistoryScreenEvent {
    object LoadHistory : HistoryScreenEvent()
    data class OnSeasonSelected(val seasonId: Int?) : HistoryScreenEvent()
    data class OnDivisionSelected(val division: String?) : HistoryScreenEvent()
    data class OnTeamSelected(val teamId: Int?) : HistoryScreenEvent()
}