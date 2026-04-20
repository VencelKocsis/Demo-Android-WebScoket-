package hu.bme.aut.android.demo.feature.tournament.teamMatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.domain.teammatch.usecase.ApplyForMatchUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.UpdateParticipantStatusUseCase
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
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- STATE & EVENTS ---
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

sealed class TeamMatchScreenEvent {
    object LoadTeamMatches : TeamMatchScreenEvent()
    data class OnApplyForMatch(val matchId: Int) : TeamMatchScreenEvent()
    data class OnToggleParticipantStatus(val participantId: Int, val currentStatus: String) : TeamMatchScreenEvent()
    data class OnDivisionSelected(val division: String?) : TeamMatchScreenEvent()
    data class OnTeamSelected(val teamId: Int?) : TeamMatchScreenEvent()
}

// --- VIEWMODEL ---
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeamMatchViewModel @Inject constructor(
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase,
    private val getTeamsUseCase: GetTeamsUseCase,
    private val applyForMatchUseCase: ApplyForMatchUseCase,
    private val updateParticipantStatusUseCase: UpdateParticipantStatusUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    private val _isMutating = MutableStateFlow(false)
    private val _actionError = MutableStateFlow<String?>(null)

    private val _selectedDivision = MutableStateFlow<String?>(null)
    private val _selectedTeamId = MutableStateFlow<Int?>(null)
    private val _isFirstLoad = MutableStateFlow(true)

    private val matchDataFlow = _refreshTrigger.mapLatest {
        val allTeams = getTeamsUseCase()
        val teamMatches = getTeamMatchesUseCase()
        Resource.success(Pair(allTeams, teamMatches))
    }.onStart {
        emit(Resource.loading())
    }.catch { e ->
        emit(Resource.error(e))
    }

    val uiState: StateFlow<TeamMatchUiState> = combine(
        matchDataFlow,
        _isMutating,
        _actionError,
        _selectedDivision,
        _selectedTeamId
    ) { dataResource, isMutating, actionError, selDivision, selTeamId ->

        val dataPair = dataResource.getOrNull()
        val allTeams = dataPair?.first ?: emptyList()
        val allMatches = dataPair?.second ?: emptyList()

        val currentUserUid = getCurrentUserUseCase()?.uid
        var currentName = ""
        val memberOf = mutableListOf<Int>()
        val captainOf = mutableListOf<Int>()
        var userPrimaryTeamId: Int? = null
        var userPrimaryDivision: String? = null

        if (currentUserUid != null) {
            allTeams.forEach { team ->
                val userInTeam = team.members.find { it.uid == currentUserUid }
                if (userInTeam != null) {
                    currentName = userInTeam.name
                    memberOf.add(team.id)
                    if (userInTeam.isCaptain) captainOf.add(team.id)

                    if (userPrimaryTeamId == null) {
                        userPrimaryTeamId = team.id
                        userPrimaryDivision = team.division
                    }
                }
            }
        }

        if (_isFirstLoad.value && allTeams.isNotEmpty()) {
            _selectedDivision.value = userPrimaryDivision
            _selectedTeamId.value = userPrimaryTeamId
            _isFirstLoad.value = false
        }

        val divisions = allTeams.mapNotNull { it.division }.filter { it.isNotBlank() }.distinct().sorted()
        val teamDivMap = allTeams.associate { it.id to (it.division ?: "Egyéb") }
        val teamsList = allTeams.map { Pair(it.id, it.name) }.sortedBy { it.second }

        // --- Élő meccsek kigyűjtése a teljes ligából (szűrés előtt!) ---
        val liveMatchesList = allMatches.filter { it.status == "in_progress" }

        val activeSeasonId = allMatches.maxOfOrNull { it.seasonId }

        // --- Normál lista szűrése ---
        var filteredMatches = allMatches

        if (activeSeasonId != null) {
            filteredMatches = filteredMatches.filter { it.seasonId == activeSeasonId }
        }

        if (selDivision != null) {
            filteredMatches = filteredMatches.filter { match ->
                teamDivMap[match.homeTeamId] == selDivision
            }
        }

        if (selTeamId != null) {
            filteredMatches = filteredMatches.filter { match ->
                match.homeTeamId == selTeamId || match.guestTeamId == selTeamId
            }
        }

        val groupedTeamMatches = filteredMatches.groupBy { it.roundNumber }.toSortedMap(compareByDescending { it })

        val displayError = actionError ?: dataResource.exceptionOrNull()?.message

        TeamMatchUiState(
            isLoading = dataResource.isLoading || isMutating,
            errorMessage = displayError,
            teamMatchesByRound = groupedTeamMatches,
            liveMatches = liveMatchesList,
            currentUserName = currentName,
            userTeamIds = memberOf,
            userCaptainTeamIds = captainOf,
            availableDivisions = divisions,
            availableTeams = teamsList,
            teamDivisions = teamDivMap,
            selectedDivision = selDivision ?: userPrimaryDivision,
            selectedTeamId = selTeamId ?: userPrimaryTeamId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TeamMatchUiState(isLoading = true)
    )

    fun onEvent(event: TeamMatchScreenEvent) {
        when (event) {
            is TeamMatchScreenEvent.LoadTeamMatches -> {
                _actionError.value = null
                _refreshTrigger.value += 1
            }
            is TeamMatchScreenEvent.OnDivisionSelected -> {
                _selectedDivision.value = event.division
                _selectedTeamId.value = null
            }
            is TeamMatchScreenEvent.OnTeamSelected -> {
                _selectedTeamId.value = event.teamId
            }
            is TeamMatchScreenEvent.OnApplyForMatch -> {
                viewModelScope.launch {
                    _isMutating.value = true
                    _actionError.value = null
                    try {
                        applyForMatchUseCase(event.matchId)
                        _refreshTrigger.value += 1
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _actionError.value = "Sikertelen jelentkezés: ${e.message}"
                    } finally {
                        _isMutating.value = false
                    }
                }
            }
            is TeamMatchScreenEvent.OnToggleParticipantStatus -> {
                viewModelScope.launch {
                    _isMutating.value = true
                    _actionError.value = null
                    try {
                        val newStatus = if (event.currentStatus == "SELECTED") "APPLIED" else "SELECTED"
                        updateParticipantStatusUseCase(event.participantId, newStatus)
                        _refreshTrigger.value += 1
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _actionError.value = "Sikertelen státusz frissítés: ${e.message}"
                    } finally {
                        _isMutating.value = false
                    }
                }
            }
        }
    }
}