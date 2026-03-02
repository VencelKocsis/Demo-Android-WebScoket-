package hu.bme.aut.android.demo.feature.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.data.auth.repository.AuthRepository
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import hu.bme.aut.android.demo.domain.team.model.toSimpleTeam
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val getTeamsUseCase: GetTeamsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Fontos: Itt a TeamScreenState-et használjuk, amit a UI-ban is definiáltál
    private val _uiState = MutableStateFlow(TeamScreenState(isLoading = true))
    val uiState: StateFlow<TeamScreenState> = _uiState.asStateFlow()

    // Itt tároljuk el a nyers domain listát, hogy ne kelljen újra lekérdezni a hálózatról
    private var allTeamsDomain: List<TeamDetails> = emptyList()

    fun onEvent(event: TeamScreenEvent) {
        when (event) {
            is TeamScreenEvent.LoadInitialData -> loadTeams()
            is TeamScreenEvent.OnTeamSelected -> selectTeam(event.teamId)
        }
    }

    private fun loadTeams() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // 1. Csapatok lekérése
                val domainTeams = getTeamsUseCase()
                allTeamsDomain = domainTeams

                // 2. Aktuális felhasználó lekérése (Firebase email alapján azonosítunk)
                val currentUserUid = authRepository.getCurrentUser()?.uid

                if (domainTeams.isNotEmpty()) {
                    // 3. A legördülő menühöz való egyszerűsített lista
                    val dropdownList = domainTeams.map { it.toSimpleTeam() }

                    // 4. Az első csapat kiválasztása
                    val firstTeam = domainTeams.first()

                    val teamToSelect = domainTeams.find { team ->
                        team.members.any { member -> member.uid == currentUserUid }
                    } ?: domainTeams.first() // Ha nincs a csapatban, az elsőt választjuk

                    // Kiszámoljuk, hogy a user kapitány-e a kiválasztott csapatban
                    val isCaptain = teamToSelect.members.any { it.uid == currentUserUid && it.isCaptain }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            teamList = dropdownList,
                            selectedTeam = teamToSelect,
                            isCurrentUserCaptain = isCaptain,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Nincsenek csapatok") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun selectTeam(teamId: Int) {
        val selected = allTeamsDomain.find { it.id == teamId }
        val currentUserUid = authRepository.getCurrentUser()?.uid
        val isCaptain = selected?.members?.any { it.uid == currentUserUid && it.isCaptain } == true

        _uiState.update { it.copy(selectedTeam = selected, isCurrentUserCaptain = isCaptain) }
    }
}