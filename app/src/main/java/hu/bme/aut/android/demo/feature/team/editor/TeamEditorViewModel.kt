package hu.bme.aut.android.demo.feature.team.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.team.usecase.AddTeamMemberUseCase
import hu.bme.aut.android.demo.domain.team.usecase.GetAvailableUsersUseCase
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.team.usecase.RemoveTeamMemberUseCase
import hu.bme.aut.android.demo.domain.team.usecase.UpdateTeamNameUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTeamsUseCase: GetTeamsUseCase,
    private val getAvailableUsersUseCase: GetAvailableUsersUseCase,
    private val updateTeamNameUseCase: UpdateTeamNameUseCase,
    private val addTeamMemberUseCase: AddTeamMemberUseCase,
    private val removeTeamMemberUseCase: RemoveTeamMemberUseCase
) : ViewModel() {

    // A Dagger Hilt automatikusan kiszedi nekünk a teamId-t a Navigációs argumentumokból!
    private val teamId: Int = checkNotNull(savedStateHandle["teamId"])

    private val _uiState = MutableStateFlow(TeamEditorState(teamId = teamId))
    val uiState: StateFlow<TeamEditorState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // 1. Csapat adatok lekérése a meglévő UseCase-el
                val allTeams = getTeamsUseCase()
                val currentTeam = allTeams.find { it.id == teamId }

                // 2. Szabad játékosok lekérése az új UseCase-el
                val availableDomainUsers = getAvailableUsersUseCase()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        teamName = currentTeam?.name ?: "",
                        newNameInput = currentTeam?.name ?: "",
                        currentMembers = currentTeam?.members ?: emptyList(),
                        availableUsers = availableDomainUsers
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEvent(event: TeamEditorEvent) {
        when (event) {
            //  Csapatnév szerkeztés logika
            is TeamEditorEvent.OnEditNameClicked -> {
                _uiState.update {
                    it.copy(
                        isEditNameDialogVisible = true,
                        newNameInput = it.teamName
                    )
                }
            }

            is TeamEditorEvent.OnDismissEditName -> {
                _uiState.update { it.copy(isEditNameDialogVisible = false) }
            }

            is TeamEditorEvent.OnNameInputChanged -> {
                _uiState.update { it.copy(newNameInput = event.newName) }
            }
            is TeamEditorEvent.OnSaveNameClicked -> {
                viewModelScope.launch {
                    try {
                        val newName = _uiState.value.newNameInput
                        _uiState.update { it.copy(isLoading = true) }

                        updateTeamNameUseCase(teamId = teamId, newName = newName)
                        _uiState.update {
                            it.copy(
                                teamName = newName,
                                isLoading = false,
                                isEditNameDialogVisible = false
                            )
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }

            // --- Kirúgás logika ---
            is TeamEditorEvent.OnKickClicked -> {
                _uiState.update { it.copy(memberToKick = event.member) }
            }
            is TeamEditorEvent.OnDismissKick -> {
                _uiState.update { it.copy(memberToKick = null) }
            }
            is TeamEditorEvent.OnConfirmKick -> {
                val memberToKick = _uiState.value.memberToKick ?: return

                viewModelScope.launch {
                    try {
                        _uiState.update { it.copy(isLoading = true) }

                        removeTeamMemberUseCase(teamId = teamId, userId = memberToKick.id)

                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                currentMembers = state.currentMembers.filter { it.id != memberToKick.id },
                                availableUsers = state.availableUsers + memberToKick.copy(isCaptain = false),
                                memberToKick = null
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _uiState.update { it.copy(isLoading = false, memberToKick = null) }
                    }
                }
            }

            // --- Hozzáadás logika ---
            is TeamEditorEvent.OnAddClicked -> {
                _uiState.update { it.copy(userToAdd = event.member) }
            }
            is TeamEditorEvent.OnDismissAdd -> {
                _uiState.update { it.copy(userToAdd = null) }
            }
            is TeamEditorEvent.OnConfirmAdd -> {
                val userToAdd = _uiState.value.userToAdd ?: return

                viewModelScope.launch {
                    try {
                        _uiState.update { it.copy(isLoading = true) }

                        addTeamMemberUseCase(teamId = teamId, userId = userToAdd.id)

                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                currentMembers = state.currentMembers + userToAdd.copy(isCaptain = false),
                                availableUsers = state.availableUsers.filter { it.id != userToAdd.id },
                                userToAdd = null
                            )
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        _uiState.update { it.copy(isLoading = false, userToAdd = null) }
                    }
                }
            }
        }
    }
}