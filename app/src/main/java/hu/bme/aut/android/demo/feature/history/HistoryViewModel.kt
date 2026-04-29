package hu.bme.aut.android.demo.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Az Előzmények képernyő üzleti logikáját kezelő ViewModel.
 * * Csak a [HistoryScreenEvent] eseményeken keresztül kommunikál a UI-al (MVI minta).
 * * Helyi (kliens oldali) szűrést alkalmaz az adatokon, ami kiszámíthatóvá és gyorssá teszi a működést.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase,
    private val getTeamsUseCase: GetTeamsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        onEvent(HistoryScreenEvent.LoadHistory)
    }

    /**
     * A UI-ról érkező események egyetlen belépési pontja.
     */
    fun onEvent(event: HistoryScreenEvent) {
        when (event) {
            is HistoryScreenEvent.LoadHistory -> loadHistory()
            is HistoryScreenEvent.OnSeasonSelected -> {
                _uiState.update { it.copy(selectedSeasonId = event.seasonId) }
                applyFilters()
            }
            is HistoryScreenEvent.OnDivisionSelected -> {
                // Divízió váltásakor a korábban kiválasztott csapatot törölni kell
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

                // CSAK a befejezett meccsekből jön adat
                val finishedMatches = allMatches
                    .filter { it.status == "finished" }
                    .sortedByDescending { it.matchDate ?: "" }

                val seasonPairs = finishedMatches
                    .map { Pair(it.seasonId, it.seasonName ?: "Ismeretlen szezon") }
                    .distinctBy { it.first }
                    .sortedByDescending { it.first }

                val latestSeasonId = seasonPairs.firstOrNull()?.first

                val divisions = allTeams.mapNotNull { it.division }.filter { it.isNotBlank() }.distinct().sorted()

                val teams = finishedMatches.flatMap {
                    listOf(it.homeTeamId to it.homeTeamName, it.guestTeamId to it.guestTeamName)
                }.distinctBy { it.first }.sortedBy { it.second }

                // Szálbiztos állapotfrissítés (.update)
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

    /**
     * Helyi (kliens oldali) szűrő logika. Lépésről lépésre szűri az eredeti listát,
     * majd frissíti a megjelenített állapotot (filteredMatches).
     */
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