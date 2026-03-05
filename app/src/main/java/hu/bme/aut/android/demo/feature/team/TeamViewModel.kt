package hu.bme.aut.android.demo.feature.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.team.model.toSimpleTeam
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val getTeamsUseCase: GetTeamsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    // 1. Különálló, reaktív állapotok
    private val _refreshTrigger = MutableStateFlow(Unit) // Trigger az újratöltéshez
    private val _selectedTeamId = MutableStateFlow<Int?>(null) // A kiválasztott csapat azonosítója

    // 2. Csapatok lekérdezése (Minden refreshTrigger-nél újra lefut)
    private val teamsFlow = _refreshTrigger.flatMapLatest {
        flow {
            emit(Resource.loading())
            val teams = getTeamsUseCase()
            emit(Resource.success(teams))
        }.catch { e ->
            emit(Resource.error(e))
        }
    }

    // 3. A UI Állapot "összegyúrása" (Deklaratív megközelítés)
    val uiState: StateFlow<TeamScreenState> = combine(
        teamsFlow,
        _selectedTeamId
    ) { teamsResource, selectedId ->

        val teams = teamsResource.getOrNull() ?: emptyList()
        val currentUserUid = getCurrentUserUseCase()?.uid

        // Meghatározzuk, melyik csapat legyen kiválasztva
        val currentTeam = teams.find {it.id == selectedId }
            ?: teams.find { team -> team.members.any { it.uid == currentUserUid } }
            ?: teams.firstOrNull()

        // Meghatározzuk, hogy a jelenlegi felhasználó kapitány-e a kiválasztott csapatban
        val isCaptain = currentTeam?.members?.any { it.uid == currentUserUid && it.isCaptain } == true

        // Beállítjuk a hibaüzenetet, ha van (vagy ha üres a lista betöltés után)
        val errorMessage = teamsResource.exceptionOrNull()?.message
            ?: if (teams.isEmpty() && !teamsResource.isLoading) "Nincsenek csapatok" else null

        TeamScreenState(
            isLoading = teamsResource.isLoading,
            teamList = teams.map { it.toSimpleTeam() },
            selectedTeam = currentTeam,
            isCurrentUserCaptain = isCaptain,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Automatikus indítás és leállítás
        initialValue = TeamScreenState(isLoading = true)
    )

    fun onEvent(event: TeamScreenEvent) {
        when (event) {
            is TeamScreenEvent.LoadInitialData -> _refreshTrigger.value = Unit
            is TeamScreenEvent.OnTeamSelected -> _selectedTeamId.value = event.teamId
        }
    }
}