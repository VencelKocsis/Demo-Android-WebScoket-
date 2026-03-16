package hu.bme.aut.android.demo.feature.tournament.scorer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.teammatch.model.IndividualMatch
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.SubmitIndividualScoreUseCase
import hu.bme.aut.android.demo.domain.websocket.model.MatchWsEvent
import hu.bme.aut.android.demo.domain.websocket.usecases.ObserveMatchEventUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Egy szett állapota (String, mert TextBox-ba gépeljük)
data class SetScoreInput(val home: String = "", val guest: String = "")

data class MatchScorerUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val match: IndividualMatch? = null,
    val sets: List<SetScoreInput> = listOf(SetScoreInput()), // Kezdésnek 1 szett van
    val homeSetsWon: Int = 0,
    val guestSetsWon: Int = 0,
    val isFinished: Boolean = false
)

@HiltViewModel
class MatchScorerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase,
    private val submitScoreUseCase: SubmitIndividualScoreUseCase,
    private val observeMatchEventsUseCase: ObserveMatchEventUseCase
) : ViewModel() {

    private val individualMatchId: Int = checkNotNull(savedStateHandle["individualMatchId"])
    private val teamMatchId: Int = checkNotNull(savedStateHandle["matchId"])

    private val _uiState = MutableStateFlow(MatchScorerUiState())
    val uiState: StateFlow<MatchScorerUiState> = _uiState

    init {
        loadMatch()
        observeWs()
    }

    private fun observeWs() {
        viewModelScope.launch {
            observeMatchEventsUseCase().collect { event ->
                when (event) {
                    is MatchWsEvent.IndividualScoreUpdated -> {
                        if (event.individualMatchId == individualMatchId) {

                            // JAVÍTVA: Bolondbiztos szövegfeldolgozás
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
                }
            }
        }
    }

    fun loadMatch() {
        viewModelScope.launch {
            try {
                val matches = getTeamMatchesUseCase()
                val parentMatch = matches.find { it.id == teamMatchId }
                val indMatch = parentMatch?.individualMatches?.find { it.id == individualMatchId }

                // Szétszedjük a szerverről kapott szetteket
                var loadedSets = indMatch?.setScores?.split(", ")?.mapNotNull {
                    val parts = it.split("-")
                    if (parts.size == 2) SetScoreInput(parts[0], parts[1]) else null
                }?.toMutableList() ?: mutableListOf(SetScoreInput())

                // Biztosítjuk, hogy legalább 1 sor legyen
                if (loadedSets.isEmpty()) {
                    loadedSets.add(SetScoreInput())
                }

                // Kiszámoljuk az állást a betöltött adatok alapján
                var hWins = 0
                var gWins = 0
                loadedSets.forEach { set ->
                    val h = set.home.toIntOrNull() ?: 0
                    val g = set.guest.toIntOrNull() ?: 0
                    if (h > g && h >= 11) hWins++
                    else if (g > h && g >= 11) gWins++
                }

                // JAVÍTÁS: Ha a meccs még nem dőlt el (nincs 3 nyert szett) ÉS az utolsó sor ki van töltve,
                // ÉS még nem értük el az 5 szettes határt, akkor adunk egy üres sort
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
                        isFinished = indMatch?.status == "finished"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateSetScore(index: Int, home: String, guest: String) {
        _uiState.update { state ->
            val mutableSets = state.sets.toMutableList()
            mutableSets[index] = SetScoreInput(home, guest)

            // Számoljuk újra a nyert szetteket!
            var hWins = 0
            var gWins = 0
            mutableSets.forEach { set ->
                val h = set.home.toIntOrNull() ?: 0
                val g = set.guest.toIntOrNull() ?: 0

                // Egyszerűsített asztalitenisz szabály: 11 pont kell, de legalább 2 pont különbség (pl. 12-10).
                // Itt most az alap 11-es határt nézzük.
                if (h > g && h >= 11 && (h - g) >= 2) hWins++
                else if (g > h && g >= 11 && (g - h) >= 2) gWins++
            }

            // JAVÍTÁS: Szigorú logikát vezetünk be az üres sorokra

            // 1. Ha már valakinek megvan a 3 nyert szettje, akkor levágjuk a listát az aktuális sornál!
            // Így nem marad bent felesleges üres 6. (vagy 4., 5.) szett.
            if (hWins >= 3 || gWins >= 3) {
                // Csak az eddig kitöltött sorokat tartjuk meg (indexig bezárólag)
                val trimmedList = mutableSets.take(index + 1)
                return@update state.copy(
                    sets = trimmedList,
                    homeSetsWon = hWins,
                    guestSetsWon = gWins
                )
            }

            // 2. Ha az utolsó szett is ki lett töltve, nincs még győztes, és nem értük el az 5. szettet, új sort nyitunk.
            if (index == mutableSets.lastIndex && home.isNotEmpty() && guest.isNotEmpty() && mutableSets.size < 5) {
                mutableSets.add(SetScoreInput())
            }

            state.copy(
                sets = mutableSets,
                homeSetsWon = hWins,
                guestSetsWon = gWins
            )
        }
    }

    fun submitScore(isFinal: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value

            // "11-8, 9-11" formátum előállítása, az üres sorokat kihagyjuk
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