package hu.bme.aut.android.demo.feature.tournament.liveMatch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.websocket.usecases.ObserveMatchEventUseCase
import hu.bme.aut.android.demo.domain.teammatch.model.IndividualMatch
import hu.bme.aut.android.demo.domain.teammatch.model.MatchParticipant
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchByIdUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.SubmitLineupUseCase
import hu.bme.aut.android.demo.domain.websocket.model.MatchWsEvent
import hu.bme.aut.android.demo.domain.teammatch.usecase.SignMatchUseCase
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
    val isSubmitSuccessful: Boolean = false,
    val phase: LiveMatchPhase = LiveMatchPhase.LOADING,
    val match: TeamMatch? = null,
    val myTeamSide: String = "HOME",
    val isSpectator: Boolean = false,
    val isStartingPlayer: Boolean = false,
    val lineupList: List<MatchParticipant> = emptyList(),
    val availablePlayers: List<MatchParticipant> = emptyList(),
    val individualMatches: List<IndividualMatch> = emptyList(),
    val errorMessage: String? = null
)

sealed class LiveMatchEvent {
    object LoadMatchData : LiveMatchEvent()
    data class TogglePlayerSlot(val participant: MatchParticipant, val sendToBench: Boolean = false) : LiveMatchEvent()
    object SubmitLineup : LiveMatchEvent()
    data class OpenIndividualMatchScoring(val individualMatchId: Int) : LiveMatchEvent()
    object SignMatch : LiveMatchEvent()
}

@HiltViewModel
class LiveMatchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTeamMatchByIdUseCase: GetTeamMatchByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val submitLineupUseCase: SubmitLineupUseCase,
    private val observeMatchEventUseCase: ObserveMatchEventUseCase,
    private val signMatchUseCase: SignMatchUseCase,
    private val getTeamsUseCase: GetTeamsUseCase
) : ViewModel() {

    private val matchId: Int = checkNotNull(savedStateHandle["matchId"])

    private val _uiState = MutableStateFlow(LiveMatchUiState())
    val uiState: StateFlow<LiveMatchUiState> = _uiState

    init {
        onEvent(LiveMatchEvent.LoadMatchData)
        observeWs()
    }

    private fun observeWs() {
        viewModelScope.launch {
            observeMatchEventUseCase().collect { event ->
                when (event) {
                    is MatchWsEvent.IndividualScoreUpdated -> {
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
                    is MatchWsEvent.MatchSignatureUpdated -> {
                        if (event.matchId == matchId) {
                            _uiState.update { state ->
                                val updatedMatch = state.match?.copy(
                                    homeTeamSigned = event.homeSigned,
                                    guestTeamSigned = event.guestSigned,
                                    status = event.status
                                )
                                state.copy(match = updatedMatch)
                            }
                        }
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
                        val match = getTeamMatchByIdUseCase(matchId)

                        val currentUserUid = getCurrentUserUseCase()?.uid
                        var teamSide = "HOME"
                        var spectatorFlag = false

                        val myParticipant = match.participants.find { it.firebaseUid == currentUserUid }

                        if (myParticipant != null) {
                            teamSide = myParticipant.teamSide
                        } else {
                            try {
                                val allTeams = getTeamsUseCase()
                                val homeTeam = allTeams.find { it.id == match.homeTeamId }
                                val guestTeam = allTeams.find { it.id == match.guestTeamId }

                                if (guestTeam?.members?.any { it.uid == currentUserUid } == true) {
                                    teamSide = "GUEST"
                                } else if (homeTeam?.members?.any { it.uid == currentUserUid } == true) {
                                    teamSide = "HOME"
                                } else {
                                    spectatorFlag = true
                                }
                            } catch (e: Exception) {
                                spectatorFlag = true
                            }
                        }

                        val startingPlayerFlag = myParticipant?.status == "LOCKED"

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
                                isStartingPlayer = startingPlayerFlag,
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

            is LiveMatchEvent.TogglePlayerSlot -> {
                _uiState.update { state ->
                    val list = state.lineupList.toMutableList()
                    val index = list.indexOf(event.participant)

                    if (index != -1) {
                        if (event.sendToBench) {
                            list.removeAt(index)
                            list.add(event.participant)
                        } else if (index < 3) {
                            val current = list[index]
                            val below = list[index + 1]
                            list[index] = below
                            list[index + 1] = current
                        } else if (index >= 4) {
                            list.removeAt(index)
                            list.add(3, event.participant)
                            val newStarters = list.take(4)
                            val newBench = list.drop(4)
                            list.clear()
                            list.addAll(newStarters.filterNotNull())
                            list.addAll(newBench.filterNotNull())
                        }
                    }
                    state.copy(lineupList = list)
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

                        // SIKERES LEADÁS -> Kigyújtjuk a jelzőt, hogy a UI visszanavigálhasson!
                        _uiState.update { it.copy(isMutating = false, isSubmitSuccessful = true) }

                    } catch (e: Exception) {
                        _uiState.update { it.copy(isMutating = false, errorMessage = "Sikertelen beküldés: ${e.message}") }
                    }
                }
            }

            is LiveMatchEvent.OpenIndividualMatchScoring -> {}

            is LiveMatchEvent.SignMatch -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isMutating = true, errorMessage = null) }
                    try {
                        signMatchUseCase(matchId)
                        _uiState.update { it.copy(isMutating = false) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isMutating = false, errorMessage = "Hiba az aláírásnál: ${e.message}") }
                    }
                }
            }
        }
    }
}