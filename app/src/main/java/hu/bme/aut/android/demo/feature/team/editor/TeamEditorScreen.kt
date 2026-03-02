package hu.bme.aut.android.demo.feature.team.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.android.demo.domain.team.model.TeamMember

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

sealed class TeamEditorEvent {
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

@Composable
fun TeamEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: TeamEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    TeamEditorContent(
        state = state,
        onEvent = { viewModel.onEvent(it) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamEditorContent(
    state: TeamEditorState,
    onEvent: (TeamEditorEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Csapat szerkesztése") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Visszalépés")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            // --- 0. CSAPAT NEVÉNEK MÓDOSÍTÁSA ---
            Text("Csapat neve", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.teamName,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onEvent(TeamEditorEvent.OnEditNameClicked) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Név szerkesztése",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // --- 1. ÚJ TAG FELVÉTELE ---
            Text("Új tag felvétele", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = "Válassz szabad játékost...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    if (state.availableUsers.isEmpty()) {
                        DropdownMenuItem(text = { Text("Nincs szabad játékos") }, onClick = { expanded = false })
                    } else {
                        state.availableUsers.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user.name) },
                                onClick = {
                                    onEvent(TeamEditorEvent.OnAddClicked(user))
                                    expanded = false
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. JELENLEGI TAGOK LISTÁJA ---
            Text("Jelenlegi csapattagok", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(state.currentMembers) { member ->
                    ListItem(
                        headlineContent = { Text(member.name) },
                        supportingContent = { Text(if (member.isCaptain) "Kapitány" else "Játékos") },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingContent = {
                            if (!member.isCaptain) {
                                IconButton(onClick = { onEvent(TeamEditorEvent.OnKickClicked(member)) }) {
                                    Icon(Icons.Default.Dangerous, contentDescription = "Eltávolítás", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }

        // --- DIALÓGUSOK ---
        if (state.isEditNameDialogVisible) {
            AlertDialog(
                onDismissRequest = { onEvent(TeamEditorEvent.OnDismissEditName) },
                title = { Text("Csapatnév módosítása") },
                text = {
                    OutlinedTextField(
                        value = state.newNameInput,
                        onValueChange = { onEvent(TeamEditorEvent.OnNameInputChanged(it)) },
                        singleLine = true,
                        label = { Text("Új név") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { onEvent(TeamEditorEvent.OnSaveNameClicked) },
                        // Csak akkor aktív a Mentés, ha nem üres, és meg is változott a név
                        enabled = state.newNameInput.isNotBlank() && state.newNameInput != state.teamName
                    ) {
                        Text("Mentés")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TeamEditorEvent.OnDismissEditName) }) {
                        Text("Mégse")
                    }
                }
            )
        }

        state.memberToKick?.let { member ->
            AlertDialog(
                onDismissRequest = { onEvent(TeamEditorEvent.OnDismissKick) },
                title = { Text("Tag eltávolítása") },
                text = { Text("Biztosan el szeretnéd távolítani ${member.name} játékost a csapatból?") },
                confirmButton = {
                    TextButton(onClick = { onEvent(TeamEditorEvent.OnConfirmKick) }) {
                        Text("Eltávolítás", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TeamEditorEvent.OnDismissKick) }) { Text("Mégse") }
                }
            )
        }

        state.userToAdd?.let { user ->
            AlertDialog(
                onDismissRequest = { onEvent(TeamEditorEvent.OnDismissAdd) },
                title = { Text("Új tag felvétele") },
                text = { Text("Szeretnéd felvenni ${user.name} játékost a csapatba?") },
                confirmButton = {
                    TextButton(onClick = { onEvent(TeamEditorEvent.OnConfirmAdd) }) { Text("Felvétel") }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TeamEditorEvent.OnDismissAdd) }) { Text("Mégse") }
                }
            )
        }
    }
}