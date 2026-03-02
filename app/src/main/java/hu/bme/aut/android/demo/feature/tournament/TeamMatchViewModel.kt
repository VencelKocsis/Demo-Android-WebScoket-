package hu.bme.aut.android.demo.feature.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamMatchViewModel @Inject constructor(
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamMatchUiState(isLoading = true))
    val uiState: StateFlow<TeamMatchUiState> = _uiState.asStateFlow()

    fun onEvent(event: TeamMatchScreenEvent) {
        when (event) {
            is TeamMatchScreenEvent.LoadTeamMatches -> LoadTeamMatches()
        }
    }

    private fun LoadTeamMatches() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val teamMatches = getTeamMatchesUseCase()

                val groupedTeamMatches = teamMatches
                    .groupBy { it.roundNumber }
                    .toSortedMap()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        teamMatchesByRound = groupedTeamMatches
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Ismeretlen hiba történt"
                    )
                }
            }
        }
    }
}