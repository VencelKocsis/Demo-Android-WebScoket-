package hu.bme.aut.android.demo.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val allMatches: List<TeamMatch> = emptyList(),
    val filteredMatches: List<TeamMatch> = emptyList(),

    // JAVÍTVA: Csak ID helyett (ID, Szezon Név) párosokat tárolunk!
    val availableSeasons: List<Pair<Int, String>> = emptyList(),
    val availableDivisions: List<String> = emptyList(),
    val availableTeams: List<Pair<Int, String>> = emptyList(),

    val teamDivisions: Map<Int, String> = emptyMap(),

    val selectedSeasonId: Int? = null,
    val selectedDivision: String? = null,
    val selectedTeamId: Int? = null
)

// ... (Az Eventek és az init blokk marad a régi) ...
sealed class HistoryScreenEvent {
    object LoadHistory : HistoryScreenEvent()
    data class OnSeasonSelected(val seasonId: Int?) : HistoryScreenEvent()
    data class OnDivisionSelected(val division: String?) : HistoryScreenEvent()
    data class OnTeamSelected(val teamId: Int?) : HistoryScreenEvent()
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase,
    private val getTeamsUseCase: GetTeamsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init { onEvent(HistoryScreenEvent.LoadHistory) }

    fun onEvent(event: HistoryScreenEvent) {
        // ... (Marad ugyanaz) ...
        when (event) {
            is HistoryScreenEvent.LoadHistory -> loadHistory()
            is HistoryScreenEvent.OnSeasonSelected -> {
                _uiState.update { it.copy(selectedSeasonId = event.seasonId) }
                applyFilters()
            }
            is HistoryScreenEvent.OnDivisionSelected -> {
                _uiState.update { it.copy(selectedDivision = event.division, selectedTeamId = null) }
                applyFilters()
            }
            is HistoryScreenEvent.OnTeamSelected -> {
                _uiState.update { it.copy(selectedTeamId = event.teamId) }
                applyFilters()
            }
        }
    }

    // --- SEGÉDFÜGGVÉNY A SZEZON NEVEKHEZ ---
    private fun getSeasonName(seasonId: Int): String {
        // TODO: Ha majd be lesz kötve a /seasons végpont Androidon, ide jöhet az automatikus keresés.
        // Addig a DB alapján "beégetjük" a fő szezonokat:
        return when(seasonId) {
            1 -> "2025 Ősz"
            2 -> "2026 Tavasz"
            else -> "Szezon #$seasonId"
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val allTeams = getTeamsUseCase()
                val teamDivMap = allTeams.associate { it.id to (it.division ?: "Egyéb") }

                val allMatches = getTeamMatchesUseCase()
                val finishedMatches = allMatches
                    .filter { it.status == "finished" }
                    .sortedByDescending { it.matchDate ?: "" }

                // JAVÍTVA: A szezon ID-kat átalakítjuk Párokká (ID, Név)
                val seasonIds = finishedMatches.map { it.seasonId }.distinct().sortedDescending()
                val seasonPairs = seasonIds.map { id -> Pair(id, getSeasonName(id)) }
                val latestSeasonId = seasonIds.firstOrNull()

                val divisions = allTeams.mapNotNull { it.division }.filter { it.isNotBlank() }.distinct().sorted()

                val teams = finishedMatches.flatMap {
                    listOf(it.homeTeamId to it.homeTeamName, it.guestTeamId to it.guestTeamName)
                }.distinctBy { it.first }.sortedBy { it.second }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allMatches = finishedMatches,
                        availableSeasons = seasonPairs, // Az új, szöveges listát adjuk át!
                        availableDivisions = divisions,
                        availableTeams = teams,
                        teamDivisions = teamDivMap,
                        selectedSeasonId = latestSeasonId,
                        selectedDivision = null,
                        selectedTeamId = null
                    )
                }
                applyFilters()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Ismeretlen hiba") }
            }
        }
    }

    private fun applyFilters() {
        // ... (Marad ugyanaz, ahogy volt) ...
        val state = _uiState.value
        var filtered = state.allMatches

        if (state.selectedSeasonId != null) {
            filtered = filtered.filter { it.seasonId == state.selectedSeasonId }
        }
        if (state.selectedDivision != null) {
            filtered = filtered.filter { match ->
                state.teamDivisions[match.homeTeamId] == state.selectedDivision
            }
        }
        if (state.selectedTeamId != null) {
            filtered = filtered.filter {
                it.homeTeamId == state.selectedTeamId || it.guestTeamId == state.selectedTeamId
            }
        }

        _uiState.update { it.copy(filteredMatches = filtered) }
    }
}