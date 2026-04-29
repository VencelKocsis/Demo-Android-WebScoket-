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

/**
 * A Csapatszerkesztő üzleti logikáját (név módosítás, tagok felvétele/kirúgása)
 * kezelő ViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeamEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTeamsUseCase: GetTeamsUseCase,
    private val getAvailableUsersUseCase: GetAvailableUsersUseCase,
    private val updateTeamNameUseCase: UpdateTeamNameUseCase,
    private val addTeamMemberUseCase: AddTeamMemberUseCase,
    private val removeTeamMemberUseCase: RemoveTeamMemberUseCase
) : ViewModel() {

    // Navigációs argumentum kinyerése Type-Safe módon
    private val teamId: Int = checkNotNull(savedStateHandle["teamId"])

    private val _refreshTrigger = MutableStateFlow(0)
    private val _newNameInput = MutableStateFlow("")
    private val _isEditNameDialogVisible = MutableStateFlow(false)
    private val _memberToKick = MutableStateFlow<hu.bme.aut.android.demo.domain.team.model.TeamMember?>(null)
    private val _userToAdd = MutableStateFlow<hu.bme.aut.android.demo.domain.team.model.TeamMember?>(null)
    private val _isMutating = MutableStateFlow(false)

    private val teamDataFlow = _refreshTrigger.mapLatest {
        val allTeams = getTeamsUseCase()
        val team = allTeams.find { it.id == teamId }
        val availableUsers = getAvailableUsersUseCase()
        Resource.success(Pair(team, availableUsers))
    }.onStart { emit(Resource.loading()) }
        .catch { e -> emit(Resource.error(e)) }

    val uiState: StateFlow<TeamEditorState> = combine(
        combine(teamDataFlow, _newNameInput, _isEditNameDialogVisible, ::Triple),
        combine(_memberToKick, _userToAdd, _isMutating, ::Triple)
    ) { (dataResource, newName, editDialog), (kickTarget, addTarget, isMutating) ->

        val dataPair = dataResource.getOrNull()
        val currentTeam = dataPair?.first
        val availableUsers = dataPair?.second ?: emptyList()

        val displayNewName = if (newName.isEmpty() && editDialog) currentTeam?.name ?: "" else newName

        TeamEditorState(
            teamId = teamId,
            teamName = currentTeam?.name ?: "Ismeretlen csapat",
            newNameInput = displayNewName,
            isEditNameDialogVisible = editDialog,
            currentMembers = currentTeam?.members ?: emptyList(),
            availableUsers = availableUsers,
            memberToKick = kickTarget,
            userToAdd = addTarget,
            isLoading = dataResource.isLoading || isMutating
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TeamEditorState(teamId = teamId, isLoading = true)
    )

    fun onEvent(event: TeamEditorEvent) {
        when (event) {
            is TeamEditorEvent.LoadTeamData -> _refreshTrigger.value += 1
            is TeamEditorEvent.OnEditNameClicked -> _isEditNameDialogVisible.value = true
            is TeamEditorEvent.OnDismissEditName -> {
                _isEditNameDialogVisible.value = false
                _newNameInput.value = ""
            }
            is TeamEditorEvent.OnNameInputChanged -> _newNameInput.value = event.newName
            is TeamEditorEvent.OnSaveNameClicked -> {
                viewModelScope.launch {
                    _isMutating.value = true
                    try {
                        updateTeamNameUseCase(teamId = teamId, newName = _newNameInput.value)
                        _isEditNameDialogVisible.value = false
                        _refreshTrigger.value += 1
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        _isMutating.value = false
                    }
                }
            }
            is TeamEditorEvent.OnKickClicked -> _memberToKick.value = event.member
            is TeamEditorEvent.OnDismissKick -> _memberToKick.value = null
            is TeamEditorEvent.OnConfirmKick -> {
                val member = _memberToKick.value ?: return
                viewModelScope.launch {
                    _isMutating.value = true
                    try {
                        removeTeamMemberUseCase(teamId = teamId, userId = member.id)
                        _memberToKick.value = null
                        _refreshTrigger.value += 1
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        _isMutating.value = false
                    }
                }
            }
            is TeamEditorEvent.OnAddClicked -> _userToAdd.value = event.member
            is TeamEditorEvent.OnDismissAdd -> _userToAdd.value = null
            is TeamEditorEvent.OnConfirmAdd -> {
                val user = _userToAdd.value ?: return
                viewModelScope.launch {
                    _isMutating.value = true
                    try {
                        addTeamMemberUseCase(teamId = teamId, userId = user.id)
                        _userToAdd.value = null
                        _refreshTrigger.value += 1
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        _isMutating.value = false
                    }
                }
            }
        }
    }
}