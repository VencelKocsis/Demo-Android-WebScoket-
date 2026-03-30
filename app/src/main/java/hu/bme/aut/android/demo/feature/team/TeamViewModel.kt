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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeamViewModel @Inject constructor(
    private val getTeamsUseCase: GetTeamsUseCase,
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    private val _selectedTeamId = MutableStateFlow<Int?>(null)

    // A mapLatest-ben lekérjük a Csapatokat ÉS a Meccseket is egyszerre
    private val dataFlow = _refreshTrigger.mapLatest {
        val teams = getTeamsUseCase()
        val matches = getTeamMatchesUseCase()
        Resource.success(Pair(teams, matches))
    }.onStart {
        emit(Resource.loading())
    }.catch { e ->
        emit(Resource.error(e))
    }

    val uiState: StateFlow<TeamScreenState> = combine(
        dataFlow,
        _selectedTeamId
    ) { dataResource, selectedId ->

        val dataPair = dataResource.getOrNull()
        val teams = dataPair?.first ?: emptyList()
        val allMatches = dataPair?.second ?: emptyList()
        val currentUserUid = getCurrentUserUseCase()?.uid

        val currentTeam = teams.find { it.id == selectedId }
            ?: teams.find { team -> team.members.any { it.uid == currentUserUid } }
            ?: teams.firstOrNull()

        val isCaptain = currentTeam?.members?.any { it.uid == currentUserUid && it.isCaptain } == true

        // --- A legutóbbi 3 befejezett meccs kiszámítása ---
        val selectedTeamId = currentTeam?.id
        val recentMatches = if (selectedTeamId != null) {
            allMatches
                .filter { it.status == "finished" && (it.homeTeamId == selectedTeamId || it.guestTeamId == selectedTeamId) }
                .sortedByDescending { it.matchDate } // Rendezzük dátum szerint csökkenőbe (legújabb elöl)
                .take(3) // Csak a legutóbbi 3-at tartjuk meg
                .map { match ->
                    val isHome = match.homeTeamId == selectedTeamId

                    val opponentName = if (isHome) match.guestTeamName else match.homeTeamName
                    val myScore = if (isHome) match.homeTeamScore else match.guestTeamScore
                    val oppScore = if (isHome) match.guestTeamScore else match.homeTeamScore

                    MatchResult(
                        matchId = match.id,
                        opponent = opponentName,
                        date = match.matchDate?.substringBefore("T") ?: "", // Dátum formázása
                        homeScore = myScore,
                        awayScore = oppScore,
                        isWin = myScore > oppScore
                    )
                }
        } else {
            emptyList()
        }
        // -------------------------------------------------------------

        val errorMessage = dataResource.exceptionOrNull()?.message
            ?: if (teams.isEmpty() && !dataResource.isLoading) "Nincsenek csapatok" else null

        TeamScreenState(
            isLoading = dataResource.isLoading,
            teamList = teams.map { it.toSimpleTeam() },
            selectedTeam = currentTeam,
            isCurrentUserCaptain = isCaptain,
            errorMessage = errorMessage,
            recentMatches = recentMatches
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
        }
    }
}