package hu.bme.aut.android.demo.feature.tournament.liveMatch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.data.network.api.RetrofitApi
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.websocket.usecases.ObserveMatchEventUseCase
import hu.bme.aut.android.demo.domain.teammatch.model.IndividualMatch
import hu.bme.aut.android.demo.domain.teammatch.model.MatchParticipant
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.SubmitLineupUseCase
import hu.bme.aut.android.demo.domain.websocket.model.MatchWsEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LiveMatchPhase {
    LOADING,
    LINEUP_SETUP,
    WAITING_FOR_OPPONENT,
    MATCH_GRID
}

data class LiveMatchUiState(
    val isLoading: Boolean = true,
    val isMutating: Boolean = false,
    val phase: LiveMatchPhase = LiveMatchPhase.LOADING,
    val match: TeamMatch? = null,
    val myTeamSide: String = "HOME",
    val isSpectator: Boolean = false,
    val lineupList: List<MatchParticipant> = emptyList(),
    val availablePlayers: List<MatchParticipant> = emptyList(),
    val individualMatches: List<IndividualMatch> = emptyList(),
    val errorMessage: String? = null
)

sealed class LiveMatchEvent {
    object LoadMatchData : LiveMatchEvent()
    data class MovePlayer(val fromIndex: Int, val toIndex: Int) : LiveMatchEvent()
    object SubmitLineup : LiveMatchEvent()
    data class OpenIndividualMatchScoring(val individualMatchId: Int) : LiveMatchEvent()
}

@HiltViewModel
class LiveMatchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val submitLineupUseCase: SubmitLineupUseCase,
    private val observeMatchEventsUseCase: ObserveMatchEventUseCase,
    private val retrofitApi: RetrofitApi
) : ViewModel() {

    private val matchId: Int = checkNotNull(savedStateHandle["matchId"])

    private val _uiState = MutableStateFlow(LiveMatchUiState())
    val uiState: StateFlow<LiveMatchUiState> = _uiState

    init {
        // 1. HTTP kérés elindítása
        onEvent(LiveMatchEvent.LoadMatchData)
        // 2. JAVÍTÁS: Valós idejű WebSocket figyelés elindítása!
        observeWs()
    }

    private fun observeWs() {
        viewModelScope.launch {
            observeMatchEventsUseCase().collect { event ->
                when (event) {
                    is MatchWsEvent.IndividualScoreUpdated -> {
                        // Kikeressük azt a meccset a listából, aminek frissült az eredménye
                        val updatedMatches = _uiState.value.individualMatches.map { match ->
                            if (match.id == event.individualMatchId) {
                                match.copy(
                                    homeScore = event.homeScore,
                                    guestScore = event.guestScore,
                                    setScores = event.setScores,
                                    status = event.status
                                )
                            } else {
                                match
                            }
                        }

                        _uiState.update { it.copy(individualMatches = updatedMatches) }
                    }
                }
            }
        }
    }

    fun onEvent(event: LiveMatchEvent) {
        when (event) {
            is LiveMatchEvent.LoadMatchData -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    try {
                        val matches = getTeamMatchesUseCase()
                        val match = matches.find { it.id == matchId } ?: throw Exception("Meccs nem található")

                        val currentUserUid = getCurrentUserUseCase()?.uid

                        var teamSide = "HOME"
                        var spectatorFlag = false

                        val myParticipant = match.participants.find { it.firebaseUid == currentUserUid }

                        if (myParticipant != null) {
                            teamSide = myParticipant.teamSide
                        } else {
                            try {
                                val allTeams = retrofitApi.getTeams() // JAVÍTVA: retrofitApi használata
                                val homeTeam = allTeams.find { it.teamId == match.homeTeamId }
                                val guestTeam = allTeams.find { it.teamId == match.guestTeamId }

                                if (guestTeam?.members?.any { it.firebaseUid == currentUserUid } == true) {
                                    teamSide = "GUEST"
                                } else if (homeTeam?.members?.any { it.firebaseUid == currentUserUid } == true) {
                                    teamSide = "HOME"
                                } else {
                                    spectatorFlag = true
                                }
                            } catch (e: Exception) {
                                spectatorFlag = true
                            }
                        }

                        val myTeamPlayers = match.participants.filter {
                            it.teamSide == teamSide && (it.status == "SELECTED" || it.status == "LOCKED")
                        }

                        val currentPhase = when {
                            match.individualMatches.isNotEmpty() -> LiveMatchPhase.MATCH_GRID
                            myTeamPlayers.any { it.status == "LOCKED" } -> LiveMatchPhase.WAITING_FOR_OPPONENT
                            else -> LiveMatchPhase.LINEUP_SETUP
                        }

                        val lockedPlayers = myTeamPlayers.filter { it.status == "LOCKED" }.sortedBy { it.position }
                        val otherPlayers = myTeamPlayers.filter { it.status != "LOCKED" }
                        val initialLineupList = lockedPlayers + otherPlayers

                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isMutating = false,
                                phase = currentPhase,
                                match = match,
                                myTeamSide = teamSide,
                                isSpectator = spectatorFlag,
                                availablePlayers = myTeamPlayers,
                                lineupList = if (state.lineupList.isEmpty()) initialLineupList else state.lineupList,
                                individualMatches = match.individualMatches
                            )
                        }

                    } catch (e: Exception) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                    }
                }
            }

            is LiveMatchEvent.MovePlayer -> {
                _uiState.update { state ->
                    val mutableList = state.lineupList.toMutableList()
                    if (event.fromIndex in mutableList.indices && event.toIndex in mutableList.indices) {
                        val item = mutableList.removeAt(event.fromIndex)
                        mutableList.add(event.toIndex, item)
                    }
                    state.copy(lineupList = mutableList)
                }
            }

            is LiveMatchEvent.SubmitLineup -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isMutating = true, errorMessage = null) }
                    try {
                        val state = _uiState.value

                        if (state.isSpectator) throw Exception("Nézőként nem módosíthatod a sorrendet!")

                        val startingFour = state.lineupList.take(4)
                        if (startingFour.size < 4) throw Exception("Legalább 4 játékosnak kell lennie a keretben!")

                        val positionsMap = startingFour.mapIndexed { index, player ->
                            (index + 1) to player.userId
                        }.toMap()

                        submitLineupUseCase(matchId, state.myTeamSide, positionsMap)

                        onEvent(LiveMatchEvent.LoadMatchData)

                    } catch (e: Exception) {
                        _uiState.update { it.copy(isMutating = false, errorMessage = "Sikertelen beküldés: ${e.message}") }
                    }
                }
            }

            is LiveMatchEvent.OpenIndividualMatchScoring -> {}
        }
    }
}