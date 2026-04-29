package hu.bme.aut.android.demo.feature.team

import hu.bme.aut.android.demo.domain.team.model.Team
import hu.bme.aut.android.demo.domain.team.model.TeamDetails

/**
 * Ideiglenes UI modell egy lezárt meccs eredményének megjelenítéséhez.
 */
data class MatchResult(
    val matchId: Int,
    val opponent: String,
    val date: String,
    val homeScore: Int,
    val awayScore: Int,
    val isWin: Boolean
)

/**
 * A Csapat (Team) képernyő egyetlen igazságforrása.
 * Tartalmazza a kiválasztott csapatot, annak statisztikáit, és a szűrők állapotát.
 */
data class TeamScreenState(
    val isLoading: Boolean = false,
    val teamList: List<Team> = emptyList(),
    val selectedTeam: TeamDetails? = null,
    val isCurrentUserCaptain: Boolean = false,
    val errorMessage: String? = null,
    val recentMatches: List<MatchResult> = emptyList(),
    val pointsHistory: List<Float> = emptyList(),

    // Szezon szűrő adatok
    val availableSeasons: List<Pair<Int, String>> = emptyList(),
    val selectedSeasonId: Int? = null,

    // Szűrő adatok
    val availableClubs: List<String> = emptyList(),
    val availableDivisions: List<String> = emptyList(),
    val selectedClub: String? = null,
    val selectedDivision: String? = null
)

/**
 * MVI események (Intents) a Csapat képernyő vezérléséhez.
 */
sealed class TeamScreenEvent {
    object LoadInitialData : TeamScreenEvent()
    data class OnTeamSelected(val teamId: Int) : TeamScreenEvent()
    data class OnClubSelected(val club: String?) : TeamScreenEvent()
    data class OnDivisionSelected(val division: String?) : TeamScreenEvent()
    data class OnSeasonSelected(val seasonId: Int?): TeamScreenEvent()
}