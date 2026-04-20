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

    val availableSeasons: List<Pair<Int, String>> = emptyList(),
    val availableDivisions: List<String> = emptyList(),
    val availableTeams: List<Pair<Int, String>> = emptyList(),

    val teamDivisions: Map<Int, String> = emptyMap(),

    val selectedSeasonId: Int? = null,
    val selectedDivision: String? = null,
    val selectedTeamId: Int? = null
)

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

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val allTeams = getTeamsUseCase()
                val teamDivMap = allTeams.associate { it.id to (it.division ?: "Egyéb") }

                val allMatches = getTeamMatchesUseCase()

                // CSAK a befejezett meccsekből jön adat (ha a 2026-os még nem indult el, itt kiesik)
                val finishedMatches = allMatches
                    .filter { it.status == "finished" }
                    .sortedByDescending { it.matchDate ?: "" }

                val seasonPairs = finishedMatches
                    .map { Pair(it.seasonId, it.seasonName ?: "Ismeretlen szezon") }
                    .distinctBy { it.first } // Kiszűrjük a duplikációkat
                    .sortedByDescending { it.first } // Legújabb szezon van legfelül

                val latestSeasonId = seasonPairs.firstOrNull()?.first

                val divisions = allTeams.mapNotNull { it.division }.filter { it.isNotBlank() }.distinct().sorted()

                val teams = finishedMatches.flatMap {
                    listOf(it.homeTeamId to it.homeTeamName, it.guestTeamId to it.guestTeamName)
                }.distinctBy { it.first }.sortedBy { it.second }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allMatches = finishedMatches,
                        availableSeasons = seasonPairs,
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