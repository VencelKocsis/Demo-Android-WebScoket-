package hu.bme.aut.android.demo.feature.tournament.teamMatch

import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

/** A Bajnokság képernyő egyetlen igazságforrása. */
data class TeamMatchUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val teamMatchesByRound: Map<Int, List<TeamMatch>> = emptyMap(),
    val liveMatches: List<TeamMatch> = emptyList(),

    val currentUserName: String = "",
    val userTeamIds: List<Int> = emptyList(),
    val userCaptainTeamIds: List<Int> = emptyList(),

    val availableDivisions: List<String> = emptyList(),
    val availableTeams: List<Pair<Int, String>> = emptyList(),
    val teamDivisions: Map<Int, String> = emptyMap(),

    val selectedDivision: String? = null,
    val selectedTeamId: Int? = null
)

/** MVI események a Bajnokság képernyőhöz. */
sealed class TeamMatchScreenEvent {
    object LoadTeamMatches : TeamMatchScreenEvent()
    data class OnApplyForMatch(val matchId: Int) : TeamMatchScreenEvent()
    data class OnToggleParticipantStatus(val participantId: Int, val currentStatus: String) : TeamMatchScreenEvent()
    data class OnDivisionSelected(val division: String?) : TeamMatchScreenEvent()
    data class OnTeamSelected(val teamId: Int?) : TeamMatchScreenEvent()
}