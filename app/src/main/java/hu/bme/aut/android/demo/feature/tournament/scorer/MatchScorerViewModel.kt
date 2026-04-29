package hu.bme.aut.android.demo.feature.tournament.scorer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchByIdUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.SubmitIndividualScoreUseCase
import hu.bme.aut.android.demo.domain.websocket.model.MatchWsEvent
import hu.bme.aut.android.demo.domain.websocket.usecases.ObserveMatchEventUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchScorerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTeamMatchByIdUseCase: GetTeamMatchByIdUseCase,
    private val submitScoreUseCase: SubmitIndividualScoreUseCase,
    private val observeMatchEventsUseCase: ObserveMatchEventUseCase
) : ViewModel() {

    private val individualMatchId: Int = checkNotNull(savedStateHandle["individualMatchId"])
    private val teamMatchId: Int = checkNotNull(savedStateHandle["matchId"])

    private val _uiState = MutableStateFlow(MatchScorerUiState())
    val uiState: StateFlow<MatchScorerUiState> = _uiState

    init {
        onEvent(MatchScorerEvent.LoadMatch)
        observeWs()
    }

    fun onEvent(event: MatchScorerEvent) {
        when (event) {
            is MatchScorerEvent.LoadMatch -> loadMatch()
            is MatchScorerEvent.UpdateSetScore -> updateSetScore(event.index, event.home, event.guest)
            is MatchScorerEvent.SubmitScore -> submitScore(event.isFinal)
        }
    }

    private fun observeWs() {
        viewModelScope.launch {
            observeMatchEventsUseCase().collect { event ->
                when (event) {
                    is MatchWsEvent.IndividualScoreUpdated -> {
                        if (event.individualMatchId == individualMatchId) {
                            val loadedSets = if (event.setScores.isBlank()) {
                                mutableListOf(SetScoreInput())
                            } else {
                                event.setScores.split(",").mapNotNull {
                                    val parts = it.split("-")
                                    if (parts.size == 2) SetScoreInput(parts[0].trim(), parts[1].trim()) else null
                                }.toMutableList()
                            }

                            if (loadedSets.isEmpty()) loadedSets.add(SetScoreInput())

                            val isFinished = event.status == "finished"
                            if (!isFinished && loadedSets.size < 5) {
                                val lastSet = loadedSets.last()
                                if (lastSet.home.isNotEmpty() && lastSet.guest.isNotEmpty()) {
                                    loadedSets.add(SetScoreInput())
                                }
                            }

                            _uiState.update {
                                it.copy(
                                    sets = loadedSets,
                                    homeSetsWon = event.homeScore,
                                    guestSetsWon = event.guestScore,
                                    isFinished = isFinished
                                )
                            }
                        }
                    }

                    is MatchWsEvent.MatchSignatureUpdated -> {
                        if (event.matchId == teamMatchId) {
                            _uiState.update { it.copy(isTeamMatchFinished = event.status == "finished") }
                        }
                    }
                }
            }
        }
    }

    private fun loadMatch() {
        viewModelScope.launch {
            try {
                val parentMatch = getTeamMatchByIdUseCase(teamMatchId)
                val indMatch = parentMatch?.individualMatches?.find { it.id == individualMatchId }

                var loadedSets = indMatch?.setScores?.split(", ")?.mapNotNull {
                    val parts = it.split("-")
                    if (parts.size == 2) SetScoreInput(parts[0], parts[1]) else null
                }?.toMutableList() ?: mutableListOf(SetScoreInput())

                if (loadedSets.isEmpty()) loadedSets.add(SetScoreInput())

                var hWins = 0; var gWins = 0
                loadedSets.forEach { set ->
                    val h = set.home.toIntOrNull() ?: 0
                    val g = set.guest.toIntOrNull() ?: 0
                    if (h > g && h >= 11) hWins++
                    else if (g > h && g >= 11) gWins++
                }

                val lastSet = loadedSets.last()
                val isLastSetFilled = lastSet.home.isNotEmpty() && lastSet.guest.isNotEmpty()
                if (hWins < 3 && gWins < 3 && isLastSetFilled && loadedSets.size < 5) {
                    loadedSets.add(SetScoreInput())
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        match = indMatch,
                        sets = loadedSets,
                        homeSetsWon = hWins,
                        guestSetsWon = gWins,
                        isFinished = indMatch?.status == "finished",
                        isTeamMatchFinished = parentMatch?.status == "finished"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun updateSetScore(index: Int, home: String, guest: String) {
        _uiState.update { state ->
            val mutableSets = state.sets.toMutableList()
            mutableSets[index] = SetScoreInput(home, guest)

            var hWins = 0; var gWins = 0
            mutableSets.forEach { set ->
                val h = set.home.toIntOrNull() ?: 0
                val g = set.guest.toIntOrNull() ?: 0
                if (h > g && h >= 11 && (h - g) >= 2) hWins++
                else if (g > h && g >= 11 && (g - h) >= 2) gWins++
            }

            if (hWins >= 3 || gWins >= 3) {
                val trimmedList = mutableSets.take(index + 1)
                return@update state.copy(sets = trimmedList, homeSetsWon = hWins, guestSetsWon = gWins)
            }

            if (index == mutableSets.lastIndex && home.isNotEmpty() && guest.isNotEmpty() && mutableSets.size < 5) {
                mutableSets.add(SetScoreInput())
            }

            state.copy(sets = mutableSets, homeSetsWon = hWins, guestSetsWon = gWins)
        }
    }

    private fun submitScore(isFinal: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value

            val scoreString = state.sets
                .filter { it.home.isNotEmpty() && it.guest.isNotEmpty() }
                .joinToString(", ") { "${it.home}-${it.guest}" }

            val status = if (isFinal) "finished" else "in_progress"

            try {
                submitScoreUseCase(individualMatchId, state.homeSetsWon, state.guestSetsWon, scoreString, status)
                _uiState.update { it.copy(isSaving = false, isFinished = isFinal) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}