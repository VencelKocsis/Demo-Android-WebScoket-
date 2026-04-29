package hu.bme.aut.android.demo.feature.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.team.model.toSimpleTeam
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
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
import javax.inject.Inject

/**
 * A Csapat képernyő üzleti logikáját (és összetett állapot-kombinációit) kezelő ViewModel.
 * * A `combine` operátort használja több adatfolyam (nyers adat + szűrők) egyesítésére.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeamViewModel @Inject constructor(
    private val getTeamsUseCase: GetTeamsUseCase,
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    private val _selectedTeamId = MutableStateFlow<Int?>(null)

    // Szűrő állapotok
    private val _selectedClub = MutableStateFlow<String?>(null)
    private val _selectedDivision = MutableStateFlow<String?>(null)
    private val _selectedSeasonId = MutableStateFlow<Int?>(-1) // -1 jelzi, hogy még nincs inicializálva

    private val dataFlow = _refreshTrigger.mapLatest {
        val teams = getTeamsUseCase()
        val matches = getTeamMatchesUseCase()
        Resource.success(Pair(teams, matches))
    }.onStart {
        emit(Resource.loading())
    }.catch { e ->
        emit(Resource.error(e))
    }

    // A UI Állapot "reaktív receptje"
    val uiState: StateFlow<TeamScreenState> = combine(
        dataFlow,
        _selectedTeamId,
        _selectedClub,
        _selectedDivision,
        _selectedSeasonId
    ) { dataResource, selectedId, selectedClub, selectedDivision, selectedSeasonFlowVal ->

        val dataPair = dataResource.getOrNull()
        val baseTeams = dataPair?.first ?: emptyList()
        val allMatchesBase = dataPair?.second ?: emptyList()
        val currentUserUid = getCurrentUserUseCase()?.uid

        // --- 1. Szezonok kinyerése a Meccsekből ---
        val finishedMatches = allMatchesBase.filter { it.status == "finished" }

        val seasonPairs = finishedMatches
            .map { Pair(it.seasonId, it.seasonName ?: "Ismeretlen szezon") }
            .distinctBy { it.first }
            .sortedByDescending { it.first }

        // Aktuális szezon meghatározása
        val actualSeasonId = if (selectedSeasonFlowVal == -1) seasonPairs.firstOrNull()?.first else selectedSeasonFlowVal

        // Csak a kiválasztott szezon meccseit tartjuk meg
        val filteredSeasonMatches = if (actualSeasonId != null) {
            finishedMatches.filter { it.seasonId == actualSeasonId }
        } else {
            finishedMatches
        }

        // --- 2. CSAPATOK STATISZTIKÁJÁNAK ÚJRASZÁMOLÁSA ---
        val allTeams = baseTeams.map { team ->
            var w = 0; var l = 0; var d = 0
            val teamMatches = filteredSeasonMatches.filter { it.homeTeamId == team.id || it.guestTeamId == team.id }

            teamMatches.forEach { m ->
                val isHome = m.homeTeamId == team.id
                val myScore = if (isHome) m.homeTeamScore else m.guestTeamScore
                val oppScore = if (isHome) m.guestTeamScore else m.homeTeamScore

                when {
                    myScore > oppScore -> w++
                    myScore < oppScore -> l++
                    else -> d++
                }
            }
            val p = (w * 2) + (d * 1)

            team.copy(matchesPlayed = teamMatches.size, wins = w, draws = d, losses = l, points = p)
        }

        // Elérhető Klubok és Divíziók kinyerése
        val availableClubs = allTeams.map { it.clubName }.distinct().sorted()
        val availableDivisions = allTeams.mapNotNull { it.division }.filter { it.isNotBlank() }.distinct().sorted()

        // Csapatok szűrése
        var filteredTeams = allTeams
        if (selectedClub != null) filteredTeams = filteredTeams.filter { it.clubName == selectedClub }
        if (selectedDivision != null) filteredTeams = filteredTeams.filter { it.division == selectedDivision }

        // Aktuális csapat kiválasztása
        val currentTeam = filteredTeams.find { it.id == selectedId }
            ?: filteredTeams.find { team -> team.members.any { it.uid == currentUserUid } }
            ?: filteredTeams.firstOrNull()

        val isCaptain = currentTeam?.members?.any { it.uid == currentUserUid && it.isCaptain } == true

        // Legutóbbi 3 meccs
        val currentTeamId = currentTeam?.id
        val recentMatches = if (currentTeamId != null) {
            filteredSeasonMatches
                .filter { it.homeTeamId == currentTeamId || it.guestTeamId == currentTeamId }
                .sortedByDescending { it.matchDate }
                .take(3)
                .map { match ->
                    val isHome = match.homeTeamId == currentTeamId
                    val opponentName = if (isHome) match.guestTeamName else match.homeTeamName
                    val myScore = if (isHome) match.homeTeamScore else match.guestTeamScore
                    val oppScore = if (isHome) match.guestTeamScore else match.homeTeamScore

                    MatchResult(
                        matchId = match.id,
                        opponent = opponentName,
                        date = match.matchDate?.substringBefore("T") ?: "",
                        homeScore = myScore,
                        awayScore = oppScore,
                        isWin = myScore > oppScore
                    )
                }
        } else emptyList()

        // Pontszámok története a grafikonhoz
        val pointsHistory = mutableListOf(0f)
        var currentPoints = 0f

        if (currentTeamId != null) {
            filteredSeasonMatches
                .filter { it.homeTeamId == currentTeamId || it.guestTeamId == currentTeamId }
                .sortedBy { it.matchDate }
                .forEach { match ->
                    val isHome = match.homeTeamId == currentTeamId
                    val myScore = if (isHome) match.homeTeamScore else match.guestTeamScore
                    val oppScore = if (isHome) match.guestTeamScore else match.homeTeamScore

                    if (myScore > oppScore) currentPoints += 3f
                    else if (myScore == oppScore) currentPoints += 1f

                    pointsHistory.add(currentPoints)
                }
        }

        val errorMessage = dataResource.exceptionOrNull()?.message
            ?: if (allTeams.isEmpty() && !dataResource.isLoading) "Nincsenek csapatok" else null

        TeamScreenState(
            isLoading = dataResource.isLoading,
            teamList = filteredTeams.map { it.toSimpleTeam() },
            selectedTeam = currentTeam,
            isCurrentUserCaptain = isCaptain,
            errorMessage = errorMessage,
            recentMatches = recentMatches,
            pointsHistory = pointsHistory,
            availableSeasons = seasonPairs,
            selectedSeasonId = actualSeasonId,
            availableClubs = availableClubs,
            availableDivisions = availableDivisions,
            selectedClub = selectedClub,
            selectedDivision = selectedDivision
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TeamScreenState(isLoading = true)
    )

    fun onEvent(event: TeamScreenEvent) {
        when (event) {
            is TeamScreenEvent.LoadInitialData -> _refreshTrigger.value += 1
            is TeamScreenEvent.OnTeamSelected -> _selectedTeamId.value = event.teamId
            is TeamScreenEvent.OnClubSelected -> {
                _selectedClub.value = event.club
                _selectedTeamId.value = null
            }
            is TeamScreenEvent.OnDivisionSelected -> {
                _selectedDivision.value = event.division
                _selectedTeamId.value = null
            }
            is TeamScreenEvent.OnSeasonSelected -> _selectedSeasonId.value = event.seasonId
        }
    }
}