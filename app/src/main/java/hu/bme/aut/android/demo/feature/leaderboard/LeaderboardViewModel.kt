package hu.bme.aut.android.demo.feature.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val allTeams: List<TeamDetails> = emptyList(),
    val filteredTeams: List<TeamDetails> = emptyList(),
    val availableDivisions: List<String> = emptyList(),
    val selectedDivision: String? = null
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val getTeamsUseCase: GetTeamsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init { loadStandings() }

    fun loadStandings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val teams = getTeamsUseCase()

                val divisions = teams.mapNotNull { it.division }.filter { it.isNotBlank() }.distinct().sorted()
                val defaultDiv = divisions.firstOrNull()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allTeams = teams,
                        availableDivisions = divisions,
                        selectedDivision = defaultDiv
                    )
                }
                applyFilterAndSort()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectDivision(division: String?) {
        _uiState.update { it.copy(selectedDivision = division) }
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        val state = _uiState.value
        var currentTeams = state.allTeams

        if (state.selectedDivision != null) {
            currentTeams = currentTeams.filter { it.division == state.selectedDivision }
        }

        // 1. Pontszám alapján csökkenő, 2. Győzelmek száma alapján csökkenő
        val sorted = currentTeams.sortedWith(compareByDescending<TeamDetails> { it.points }.thenByDescending { it.wins })

        _uiState.update { it.copy(filteredTeams = sorted) }
    }
}