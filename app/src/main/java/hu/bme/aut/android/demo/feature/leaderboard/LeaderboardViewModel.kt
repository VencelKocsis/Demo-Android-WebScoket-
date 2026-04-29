package hu.bme.aut.android.demo.feature.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import hu.bme.aut.android.demo.domain.team.model.TeamStats
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A Ranglista üzleti és prezentációs logikájáért felelős ViewModel.
 * * Feladata: Nyers adatok bekérése a Domain rétegből, a pontszámítások és
 * statisztikák elvégzése, majd az [LeaderboardUiState] frissítése a UI számára.
 */
@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val getTeamsUseCase: GetTeamsUseCase,
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        onEvent(LeaderboardEvent.LoadStandings)
    }

    /**
     * A UI-ról érkező interakciók központosított kezelése.
     */
    fun onEvent(event: LeaderboardEvent) {
        when (event) {
            is LeaderboardEvent.LoadStandings -> loadStandings()
            is LeaderboardEvent.OnSeasonSelected -> {
                _uiState.update { it.copy(selectedSeasonId = event.seasonId) }
                applyFilterAndSort()
            }
            is LeaderboardEvent.OnDivisionSelected -> {
                _uiState.update { it.copy(selectedDivision = event.division) }
                applyFilterAndSort()
            }
        }
    }

    private fun loadStandings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val baseTeams = getTeamsUseCase()
                val allMatches = getTeamMatchesUseCase()
                val finishedMatches = allMatches.filter { it.status == "finished" }

                // --- 1. SZEZON STATISZTIKÁK KISZÁMÍTÁSA A TELEFONON ---
                val enhancedTeams = baseTeams.map { team ->
                    val teamMatches = finishedMatches.filter { it.homeTeamId == team.id || it.guestTeamId == team.id }
                    val matchesBySeason = teamMatches.groupBy { it.seasonId }

                    val calculatedSeasonStats = matchesBySeason.mapValues { (_, matches) ->
                        var w = 0; var l = 0; var d = 0
                        matches.forEach { m ->
                            val isHome = m.homeTeamId == team.id
                            val myScore = if (isHome) m.homeTeamScore else m.guestTeamScore
                            val oppScore = if (isHome) m.guestTeamScore else m.homeTeamScore

                            when {
                                myScore > oppScore -> w++
                                myScore < oppScore -> l++
                                else -> d++
                            }
                        }
                        // PONTSZÁMÍTÁS SZABÁLY: (2 pont a győzelem, 1 pont döntetlen, 0 pont vereség)
                        val p = (w * 2) + (d * 1)
                        TeamStats(matches.size, w, l, d, p)
                    }

                    // Frissítjük a csapat modellt az új map-pel
                    team.copy(seasonStats = calculatedSeasonStats)
                }

                // --- 2. FELHASZNÁLÓ DIVÍZIÓJÁNAK KIKERESÉSE ---
                val currentUserUid = getCurrentUserUseCase()?.uid
                var userPrimaryDivision: String? = null
                if (currentUserUid != null) {
                    val userTeam = enhancedTeams.find { team -> team.members.any { it.uid == currentUserUid } }
                    if (userTeam != null) { userPrimaryDivision = userTeam.division }
                }

                // --- 3. SZEZONOK ÉS DIVÍZIÓK KINYERÉSE ---
                val seasonPairs = finishedMatches
                    .map { Pair(it.seasonId, it.seasonName ?: "Ismeretlen szezon") }
                    .distinctBy { it.first }
                    .sortedByDescending { it.first }

                val latestSeasonId = seasonPairs.firstOrNull()?.first
                val divisions = enhancedTeams.mapNotNull { it.division }.filter { it.isNotBlank() }.distinct().sorted()
                val defaultDiv = userPrimaryDivision ?: divisions.firstOrNull()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allTeams = enhancedTeams,
                        availableSeasons = seasonPairs,
                        availableDivisions = divisions,
                        selectedSeasonId = latestSeasonId,
                        selectedDivision = defaultDiv
                    )
                }
                applyFilterAndSort()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Kliens-oldali szűrés és sorba rendezés (pontok, majd győzelmek alapján).
     */
    private fun applyFilterAndSort() {
        val state = _uiState.value
        var currentTeams = state.allTeams

        if (state.selectedDivision != null) {
            currentTeams = currentTeams.filter { it.division == state.selectedDivision }
        }

        val sorted = currentTeams.sortedWith(
            compareByDescending<TeamDetails> { it.getStats(state.selectedSeasonId).points }
                .thenByDescending { it.getStats(state.selectedSeasonId).wins }
        )

        _uiState.update { it.copy(filteredTeams = sorted) }
    }
}