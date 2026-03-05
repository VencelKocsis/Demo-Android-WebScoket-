package hu.bme.aut.android.demo.feature.tournament.match

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.data.auth.repository.AuthRepository
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.domain.teammatch.usecase.ApplyForMatchUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.FinalizeMatchUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.UpdateParticipantStatusUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.WithdrawFromMatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchDetailsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val match: TeamMatch? = null,
    val currentUserName: String = "",
    val isUserInvolved: Boolean = false,
    val isHomeCaptain: Boolean = false,
    val isGuestCaptain: Boolean = false,
    val hasApplied: Boolean = false,
    val myStatus: String? = null,
    val homeSelectedCount: Int = 0,
    val guestSelectedCount: Int = 0
)

sealed class MatchDetailsEvent {
    object LoadMatch : MatchDetailsEvent()
    object OnApply : MatchDetailsEvent()
    object OnWithdrawApplication: MatchDetailsEvent()
    data class OnToggleParticipantStatus(val participantId: Int, val currentStatus: String) : MatchDetailsEvent()
    object OnFinalizeRoster: MatchDetailsEvent()
}

@HiltViewModel
class MatchDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase,
    private val getTeamsUseCase: GetTeamsUseCase,
    private val applyForMatchUseCase: ApplyForMatchUseCase,
    private val updateParticipantStatusUseCase: UpdateParticipantStatusUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val withdrawFromMatchUseCase: WithdrawFromMatchUseCase,
    private val finalizeMatchUseCase: FinalizeMatchUseCase
) : ViewModel() {

    private val matchId: Int = checkNotNull(savedStateHandle["matchId"])

    private val _uiState = MutableStateFlow(MatchDetailsUiState())
    val uiState: StateFlow<MatchDetailsUiState> = _uiState.asStateFlow()

    fun onEvent(event: MatchDetailsEvent) {
        when (event) {
            is MatchDetailsEvent.LoadMatch -> loadMatchDetails()
            is MatchDetailsEvent.OnApply -> applyForMatch()
            is MatchDetailsEvent.OnToggleParticipantStatus -> updateStatus(event.participantId, event.currentStatus)
            is MatchDetailsEvent.OnWithdrawApplication -> withdrawApplication()
            is MatchDetailsEvent.OnFinalizeRoster -> finalizeRoster()
        }
    }

    private fun withdrawApplication() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                withdrawFromMatchUseCase(matchId)
                loadMatchDetails() // Újratöltjük az adatokat a siker után
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sikertelen visszavonás") }
            }
        }
    }

    private fun finalizeRoster() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                finalizeMatchUseCase(matchId)
                loadMatchDetails() // Újratöltjük, hogy eltűnjön a gomb, és a státusz 'in_progress' legyen
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sikertelen véglegesítés") }
            }
        }
    }

    private fun loadMatchDetails() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val currentUserUid = getCurrentUserUseCase()?.uid
                Log.d("MatchDetails", "=== MECCS BETÖLTÉSE INDUL ===")
                Log.d("MatchDetails", "1. Jelenlegi Firebase UID: $currentUserUid")

                val allTeams = getTeamsUseCase()
                val allMatches = getTeamMatchesUseCase()

                val match = allMatches.find { it.id == matchId } ?: throw Exception("A meccs nem található!")
                Log.d("MatchDetails", "2. Megnyitott meccs: ${match.homeTeamName} vs ${match.guestTeamName}")
                Log.d("MatchDetails", "   - Hazai ID: ${match.homeTeamId}")
                Log.d("MatchDetails", "   - Vendég ID: ${match.guestTeamId}")

                var currentName = ""
                var userTeams = mutableListOf<Int>()
                var userCaptainTeams = mutableListOf<Int>()

                allTeams.forEach { team ->
                    // Itt feltételezem, hogy a TeamMember modeledben 'uid' néven van a firebase uid.
                    val userInTeam = team.members.find { it.uid == currentUserUid }
                    if (userInTeam != null) {
                        currentName = userInTeam.name
                        userTeams.add(team.id)
                        if (userInTeam.isCaptain) userCaptainTeams.add(team.id)
                        Log.d("MatchDetails", "   -> A felhasználó megtalálva a(z) '${team.name}' csapatban! (Team ID: ${team.id})")
                    }
                }

                Log.d("MatchDetails", "3. Felhasználó neve a DB alapján: '$currentName'")
                Log.d("MatchDetails", "4. Felhasználó csapatai (ID list): $userTeams")

                val isInvolved = userTeams.contains(match.homeTeamId) || userTeams.contains(match.guestTeamId)
                val homeCap = userCaptainTeams.contains(match.homeTeamId)
                val guestCap = userCaptainTeams.contains(match.guestTeamId)

                val myParticipantData = match.participants.find { it.playerName == currentName }

                Log.d("MatchDetails", "5. Jogosultságok:")
                Log.d("MatchDetails", "   - isInvolved (Benne van-e valamelyik csapatban): $isInvolved")
                Log.d("MatchDetails", "   - isHomeCaptain: $homeCap")
                Log.d("MatchDetails", "   - isGuestCaptain: $guestCap")
                Log.d("MatchDetails", "   - hasApplied (Jelentkezett-e már): ${myParticipantData != null}")

                val homeSelectedCount = match.participants.count { it.teamSide == "HOME" && it.status == "SELECTED" }
                val guestSelectedCount = match.participants.count { it.teamSide == "GUEST" && it.status == "SELECTED" }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        match = match,
                        currentUserName = currentName,
                        isUserInvolved = isInvolved,
                        isHomeCaptain = homeCap,
                        isGuestCaptain = guestCap,
                        hasApplied = myParticipantData != null,
                        myStatus = myParticipantData?.status,
                        homeSelectedCount = homeSelectedCount,
                        guestSelectedCount = guestSelectedCount
                    )
                }
                Log.d("MatchDetails", "=== MECCS BETÖLTÉSE KÉSZ ===")
            } catch (e: Exception) {
                Log.e("MatchDetails", "Hiba a betöltéskor: ${e.message}")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Hiba történt") }
            }
        }
    }

    private fun applyForMatch() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                applyForMatchUseCase(matchId)
                loadMatchDetails()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sikertelen jelentkezés") }
            }
        }
    }

    private fun updateStatus(participantId: Int, currentStatus: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val newStatus = if (currentStatus == "SELECTED") "APPLIED" else "SELECTED"
                updateParticipantStatusUseCase(participantId, newStatus)
                loadMatchDetails()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sikertelen státusz frissítés") }
            }
        }
    }
}