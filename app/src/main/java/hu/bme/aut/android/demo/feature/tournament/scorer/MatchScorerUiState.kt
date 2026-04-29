package hu.bme.aut.android.demo.feature.tournament.scorer

import hu.bme.aut.android.demo.domain.teammatch.model.IndividualMatch

data class SetScoreInput(val home: String = "", val guest: String = "")

/** A Pontozó (Scorer) képernyő állapota. */
data class MatchScorerUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val match: IndividualMatch? = null,
    val sets: List<SetScoreInput> = listOf(SetScoreInput()),
    val homeSetsWon: Int = 0,
    val guestSetsWon: Int = 0,
    val isFinished: Boolean = false,
    val isTeamMatchFinished: Boolean = false
)

/** MVI Események a Pontozóhoz (Új!) */
sealed class MatchScorerEvent {
    object LoadMatch : MatchScorerEvent()
    data class UpdateSetScore(val index: Int, val home: String, val guest: String) : MatchScorerEvent()
    data class SubmitScore(val isFinal: Boolean) : MatchScorerEvent()
}