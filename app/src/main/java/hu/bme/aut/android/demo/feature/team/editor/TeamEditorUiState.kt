package hu.bme.aut.android.demo.feature.team.editor

import hu.bme.aut.android.demo.domain.team.model.TeamMember

/**
 * A Csapatszerkesztő képernyő egyetlen igazságforrása.
 * Tartalmazza a jelenlegi tagokat, az elérhető játékosokat, és a dialógusok állapotait.
 */
data class TeamEditorState(
    val teamId: Int = 0,
    val teamName: String = "",
    val newNameInput: String = "",
    val isEditNameDialogVisible: Boolean = false,
    val currentMembers: List<TeamMember> = emptyList(),
    val availableUsers: List<TeamMember> = emptyList(),
    val memberToKick: TeamMember? = null,
    val userToAdd: TeamMember? = null,
    val isLoading: Boolean = false
)

/**
 * MVI események (Intents) a Csapatszerkesztő képernyő vezérléséhez.
 */
sealed class TeamEditorEvent {
    object LoadTeamData : TeamEditorEvent()

    data class OnKickClicked(val member: TeamMember) : TeamEditorEvent()
    object OnConfirmKick : TeamEditorEvent()
    object OnDismissKick : TeamEditorEvent()

    data class OnAddClicked(val member: TeamMember) : TeamEditorEvent()
    object OnConfirmAdd : TeamEditorEvent()
    object OnDismissAdd : TeamEditorEvent()

    object OnEditNameClicked : TeamEditorEvent()
    object OnDismissEditName : TeamEditorEvent()
    data class OnNameInputChanged(val newName: String) : TeamEditorEvent()
    object OnSaveNameClicked : TeamEditorEvent()
}