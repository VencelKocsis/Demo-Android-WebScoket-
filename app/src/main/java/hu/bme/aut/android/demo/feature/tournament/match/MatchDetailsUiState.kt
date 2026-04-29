package hu.bme.aut.android.demo.feature.tournament.match

import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

data class MatchRosterItem(
    val participantId: Int?, // null, ha még nem jelentkezett
    val userId: Int,
    val playerName: String,
    val status: String // "NOT_APPLIED", "APPLIED", "SELECTED", "LOCKED"
)

/** A Meccs Részletek képernyő állapota. */
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
    val guestSelectedCount: Int = 0,

    val homeRoster: List<MatchRosterItem> = emptyList(),
    val guestRoster: List<MatchRosterItem> = emptyList()
)

/** MVI Események a jelentkezéshez és kapitányi jóváhagyáshoz. */
sealed class MatchDetailsEvent {
    object LoadMatch : MatchDetailsEvent()
    object OnApply : MatchDetailsEvent()
    object OnWithdrawApplication: MatchDetailsEvent()
    data class OnCaptainTogglePlayer(val rosterItem: MatchRosterItem) : MatchDetailsEvent()
    object OnFinalizeRoster: MatchDetailsEvent()
    object ClearActionError: MatchDetailsEvent()
}