package hu.bme.aut.android.demo.feature.tournament.teamMatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.domain.teammatch.usecase.ApplyForMatchUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.UpdateParticipantStatusUseCase
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

// --- STATE & EVENTS (Ideálisan egy TeamMatchContract.kt fájlban) ---
data class TeamMatchUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val teamMatchesByRound: Map<Int, List<TeamMatch>> = emptyMap(),
    val currentUserName: String = "",
    val userTeamIds: List<Int> = emptyList(),
    val userCaptainTeamIds: List<Int> = emptyList()
)

sealed class TeamMatchScreenEvent {
    object LoadTeamMatches : TeamMatchScreenEvent()
    data class OnApplyForMatch(val matchId: Int) : TeamMatchScreenEvent()
    data class OnToggleParticipantStatus(val participantId: Int, val currentStatus: String) : TeamMatchScreenEvent()
}

// --- VIEWMODEL ---
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeamMatchViewModel @Inject constructor(
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase,
    private val getTeamsUseCase: GetTeamsUseCase,
    private val applyForMatchUseCase: ApplyForMatchUseCase,
    private val updateParticipantStatusUseCase: UpdateParticipantStatusUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    // 1. Triggerek a reaktív adatfolyamhoz (Unit helyett 0-s számláló!)
    private val _refreshTrigger = MutableStateFlow(0)
    private val _isMutating = MutableStateFlow(false) // Írási műveletek töltésjelzője
    private val _actionError = MutableStateFlow<String?>(null) // Hibák a gombnyomásoknál

    // 2. Tiszta adat-Flow, ami betölti a csapatokat és a meccseket (mapLatest + onStart)
    private val matchDataFlow = _refreshTrigger.mapLatest {
        val allTeams = getTeamsUseCase()
        val teamMatches = getTeamMatchesUseCase()
        Resource.success(Pair(allTeams, teamMatches))
    }.onStart {
        emit(Resource.loading())
    }.catch { e ->
        emit(Resource.error(e))
    }

    // 3. UI Állapot összerakása (combine)
    val uiState: StateFlow<TeamMatchUiState> = combine(
        matchDataFlow,
        _isMutating,
        _actionError
    ) { dataResource, isMutating, actionError ->

        val dataPair = dataResource.getOrNull()
        val allTeams = dataPair?.first ?: emptyList()
        val teamMatches = dataPair?.second ?: emptyList()

        // Adatfeldolgozás
        val currentUserUid = getCurrentUserUseCase()?.uid
        var currentName = ""
        val memberOf = mutableListOf<Int>()
        val captainOf = mutableListOf<Int>()

        if (currentUserUid != null) {
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
        }

        val groupedTeamMatches = teamMatches.groupBy { it.roundNumber }.toSortedMap()

        // Hibaüzenet priorizálása: Előbb a gombnyomás hibája, utána a hálózati betöltés hibája
        val displayError = actionError ?: dataResource.exceptionOrNull()?.message

        TeamMatchUiState(
            isLoading = dataResource.isLoading || isMutating,
            errorMessage = displayError,
            teamMatchesByRound = groupedTeamMatches,
            currentUserName = currentName,
            userTeamIds = memberOf,
            userCaptainTeamIds = captainOf
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TeamMatchUiState(isLoading = true)
    )

    fun onEvent(event: TeamMatchScreenEvent) {
        when (event) {
            is TeamMatchScreenEvent.LoadTeamMatches -> {
                _actionError.value = null
                _refreshTrigger.value += 1 // Növeljük a számlálót
            }

            is TeamMatchScreenEvent.OnApplyForMatch -> {
                viewModelScope.launch {
                    _isMutating.value = true
                    _actionError.value = null
                    try {
                        applyForMatchUseCase(event.matchId)
                        _refreshTrigger.value += 1 // Siker -> Újratöltjük az adatokat!
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _actionError.value = "Sikertelen jelentkezés: ${e.message}"
                    } finally {
                        _isMutating.value = false
                    }
                }
            }

            is TeamMatchScreenEvent.OnToggleParticipantStatus -> {
                viewModelScope.launch {
                    _isMutating.value = true
                    _actionError.value = null
                    try {
                        val newStatus = if (event.currentStatus == "SELECTED") "APPLIED" else "SELECTED"
                        updateParticipantStatusUseCase(event.participantId, newStatus)
                        _refreshTrigger.value += 1 // Siker -> Újratöltjük az adatokat!
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _actionError.value = "Sikertelen státusz frissítés: ${e.message}"
                    } finally {
                        _isMutating.value = false
                    }
                }
            }
        }
    }
}