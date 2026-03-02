package hu.bme.aut.android.demo.feature.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.data.auth.repository.AuthRepository
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.ApplyForMatchUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.UpdateParticipantStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamMatchViewModel @Inject constructor(
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase,
    private val getTeamsUseCase: GetTeamsUseCase,
    private val applyForMatchUseCase: ApplyForMatchUseCase,
    private val updateParticipantStatusUseCase: UpdateParticipantStatusUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamMatchUiState(isLoading = true))
    val uiState: StateFlow<TeamMatchUiState> = _uiState.asStateFlow()

    fun onEvent(event: TeamMatchScreenEvent) {
        when (event) {
            is TeamMatchScreenEvent.LoadTeamMatches -> loadTeamMatches()
            is TeamMatchScreenEvent.OnApplyForMatch -> applyForMatch(event.matchId)
            is TeamMatchScreenEvent.OnToggleParticipantStatus -> updateParticipantStatus(
                event.participantId,
                event.currentStatus
            )
        }
    }

    private fun loadTeamMatches() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // 1. Felhasználói adatok és jogosultságok lekérése
                val currentUserUid = authRepository.getCurrentUser()?.uid
                val allTeams = getTeamsUseCase()

                var currentName = ""
                val memberOf = mutableListOf<Int>()
                val captainOf = mutableListOf<Int>()

                allTeams.forEach { team ->
                    val userInTeam = team.members.find { it.uid == currentUserUid }
                    if (userInTeam != null) {
                        currentName = userInTeam.name
                        memberOf.add(team.id)
                        if (userInTeam.isCaptain) {
                            captainOf.add(team.id)
                        }
                    }
                }

                // 2. Meccsek lekérése és csoportosítása
                val teamMatches = getTeamMatchesUseCase()
                val groupedTeamMatches = teamMatches
                    .groupBy { it.roundNumber }
                    .toSortedMap()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        teamMatchesByRound = groupedTeamMatches,
                        currentUserName = currentName,
                        userTeamIds = memberOf,
                        userCaptainTeamIds = captainOf
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

    private fun applyForMatch(matchId: Int) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                applyForMatchUseCase(matchId)
                loadTeamMatches() // Újratöltjük az adatokat, hogy látszódjon a jelentkezés
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Sikertelen jelentkezés: ${e.message}"
                    )
                }
            }
        }
    }

    private fun updateParticipantStatus(participantId: Int, currentStatus: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val newStatus = if (currentStatus == "SELECTED") "APPLIED" else "SELECTED"
                updateParticipantStatusUseCase(participantId, newStatus)
                loadTeamMatches() // Újratöltjük az adatokat, hogy látszódjon a státusz frissítés
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sikertelen státusz frissítés: ${e.message}") }
            }
        }
    }
}