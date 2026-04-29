package hu.bme.aut.android.demo.feature.tournament.liveMatch

import hu.bme.aut.android.demo.domain.teammatch.model.IndividualMatch
import hu.bme.aut.android.demo.domain.teammatch.model.MatchParticipant
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

/** Az élő meccs lehetséges állapotai (Állapotgép / FSM). */
enum class LiveMatchPhase {
    LOADING,
    LINEUP_SETUP,
    WAITING_FOR_OPPONENT,
    MATCH_GRID
}

/** Az élő meccs képernyő állapota. */
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

/** MVI Események a felállás készítéshez és az élő eseményekhez. */
sealed class LiveMatchEvent {
    object LoadMatchData : LiveMatchEvent()
    data class TogglePlayerSlot(val participant: MatchParticipant, val sendToBench: Boolean = false) : LiveMatchEvent()
    object SubmitLineup : LiveMatchEvent()
    data class OpenIndividualMatchScoring(val individualMatchId: Int) : LiveMatchEvent()
    object SignMatch : LiveMatchEvent()
}