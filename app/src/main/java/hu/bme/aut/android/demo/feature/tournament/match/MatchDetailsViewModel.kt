package hu.bme.aut.android.demo.feature.tournament.match

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.domain.teammatch.usecase.ApplyForMatchUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.FinalizeMatchUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.UpdateParticipantStatusUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.WithdrawFromMatchUseCase
import hu.bme.aut.android.demo.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchDetailsUiState(
    val isLoading: Boolean = true,
    val isMutating: Boolean = false,
    val errorMessage: String? = null,
    val actionError: String? = null,
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
    object ClearActionError: MatchDetailsEvent() // A Snackbar eltüntetéséhez
}

@OptIn(ExperimentalCoroutinesApi::class)
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

    // 1. Triggerek és UI akció állapotok
    private val _refreshTrigger = MutableStateFlow(0)
    private val _isMutating = MutableStateFlow(false)
    private val _actionError = MutableStateFlow<String?>(null)

    // 2. Adatfolyam (Ktor hívások)
    private val matchDataFlow = _refreshTrigger.mapLatest {
        // A mapLatest addig "felfüggeszti" (suspend) magát, amíg a hálózat dolgozik.
        // Ezalatt a StateFlow NEM küld ki új értéket, hanem megtartja az előzőt a képernyőn!
        val allTeams = getTeamsUseCase()
        val allMatches = getTeamMatchesUseCase()

        Resource.success(Pair(allTeams, allMatches))
    }.onStart {
        // A legelső induláskor viszont (amikor még tényleg nincs adatunk)
        // ki kell küldeni a töltőképernyőt.
        emit(Resource.loading())
    }.catch { e ->
        emit(Resource.error(e))
    }

    // 3. A Végső UI Állapot deklaratív összerakása
    val uiState: StateFlow<MatchDetailsUiState> = combine(
        matchDataFlow,
        _isMutating,
        _actionError
    ) { dataResource, isMutating, actionError ->

        val dataPair = dataResource.getOrNull()
        val allTeams = dataPair?.first ?: emptyList()
        val allMatches = dataPair?.second ?: emptyList()

        val match = allMatches.find { it.id == matchId }
        val loadError = if (dataPair != null && match == null) "A meccs nem található!" else dataResource.exceptionOrNull()?.message

        // Jogosultságok kiszámítása
        val currentUserUid = getCurrentUserUseCase()?.uid
        var currentName = ""
        val userTeams = mutableListOf<Int>()
        val userCaptainTeams = mutableListOf<Int>()

        if (currentUserUid != null) {
            allTeams.forEach { team ->
                val userInTeam = team.members.find { it.uid == currentUserUid }
                if (userInTeam != null) {
                    currentName = userInTeam.name
                    userTeams.add(team.id)
                    if (userInTeam.isCaptain) userCaptainTeams.add(team.id)
                }
            }
        }

        val isInvolved = match != null && (userTeams.contains(match.homeTeamId) || userTeams.contains(match.guestTeamId))
        val homeCap = match != null && userCaptainTeams.contains(match.homeTeamId)
        val guestCap = match != null && userCaptainTeams.contains(match.guestTeamId)

        val myParticipantData = match?.participants?.find { it.playerName == currentName }

        val homeSelectedCount = match?.participants?.count { it.teamSide == "HOME" && it.status == "SELECTED" } ?: 0
        val guestSelectedCount = match?.participants?.count { it.teamSide == "GUEST" && it.status == "SELECTED" } ?: 0

        MatchDetailsUiState(
            isLoading = dataResource.isLoading,
            isMutating = isMutating,
            errorMessage = loadError,
            actionError = actionError,
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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MatchDetailsUiState(isLoading = true)
    )

    fun onEvent(event: MatchDetailsEvent) {
        when (event) {
            is MatchDetailsEvent.LoadMatch -> _refreshTrigger.value += 1
            is MatchDetailsEvent.ClearActionError -> _actionError.value = null

            is MatchDetailsEvent.OnApply -> executeAction("Sikertelen jelentkezés") {
                applyForMatchUseCase(matchId)
            }
            is MatchDetailsEvent.OnWithdrawApplication -> executeAction("Sikertelen visszavonás") {
                withdrawFromMatchUseCase(matchId)
            }
            is MatchDetailsEvent.OnToggleParticipantStatus -> executeAction("Sikertelen státusz frissítés") {
                val newStatus = if (event.currentStatus == "SELECTED") "APPLIED" else "SELECTED"
                updateParticipantStatusUseCase(event.participantId, newStatus)
            }
            is MatchDetailsEvent.OnFinalizeRoster -> executeAction("Sikertelen véglegesítés") {
                finalizeMatchUseCase(matchId)
            }
        }
    }

    /**
     * Segédfüggvény a mutációs kérések (írások) egységes kezelésére.
     * Bekapcsolja a töltésjelzőt, lefuttatja a hívást, majd újratölti a képernyőt.
     */
    private fun executeAction(errorMessage: String, action: suspend () -> Unit) {
        viewModelScope.launch {
            _isMutating.value = true
            _actionError.value = null
            try {
                action()
                _refreshTrigger.value += 1 // Siker -> Lekérjük az új állapotot
            } catch (e: Exception) {
                Log.e("MatchDetails", "$errorMessage: ${e.message}")
                _actionError.value = errorMessage
            } finally {
                _isMutating.value = false
            }
        }
    }
}