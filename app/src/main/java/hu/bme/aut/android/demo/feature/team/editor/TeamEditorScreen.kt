package hu.bme.aut.android.demo.feature.team.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.domain.team.model.TeamMember

// --- ÁLLAPOT ÉS ESEMÉNYEK ---
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

@Composable
fun TeamEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: TeamEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TeamEditorContent(
        state = state,
        onEvent = viewModel::onEvent,
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

        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { onEvent(TeamEditorEvent.LoadTeamData) },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // Mivel az egész képernyő görgethető, egy LazyColumn-ba rakunk mindent
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // --- 0. CSAPAT NEVÉNEK MÓDOSÍTÁSA ---
                    item {
                        Text("Csapat neve", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onEvent(TeamEditorEvent.OnEditNameClicked) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Text(
                                    text = state.teamName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(1f)
                                )
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
                    }

                    // --- 1. ÚJ TAG FELVÉTELE ---
                    item {
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
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                if (state.availableUsers.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Nincs szabad játékos", color = Color.Gray) },
                                        onClick = { expanded = false }
                                    )
                                } else {
                                    state.availableUsers.forEach { user ->
                                        DropdownMenuItem(
                                            text = { Text(user.name, fontWeight = FontWeight.Medium) },
                                            onClick = {
                                                onEvent(TeamEditorEvent.OnAddClicked(user))
                                                expanded = false
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // --- 2. JELENLEGI TAGOK LISTÁJA ---
                    item {
                        Text("Jelenlegi csapattagok", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(state.currentMembers, key = { it.uid }) { member ->
                        EditorPlayerCardRow(
                            name = member.name,
                            isCaptain = member.isCaptain,
                            onRemoveClick = {
                                if (!member.isCaptain) {
                                    onEvent(TeamEditorEvent.OnKickClicked(member))
                                }
                            }
                        )
                    }
                }
            }
        } // --- PullToRefreshBox VÉGE ---

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
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { onEvent(TeamEditorEvent.OnSaveNameClicked) },
                        enabled = state.newNameInput.isNotBlank() && state.newNameInput != state.teamName
                    ) {
                        Text("Mentés", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TeamEditorEvent.OnDismissEditName) }) {
                        Text("Mégse", color = Color.Gray)
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
                        Text("Eltávolítás", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TeamEditorEvent.OnDismissKick) }) {
                        Text("Mégse", color = Color.Gray)
                    }
                }
            )
        }

        state.userToAdd?.let { user ->
            AlertDialog(
                onDismissRequest = { onEvent(TeamEditorEvent.OnDismissAdd) },
                title = { Text("Új tag felvétele") },
                text = { Text("Szeretnéd felvenni ${user.name} játékost a csapatba?") },
                confirmButton = {
                    TextButton(onClick = { onEvent(TeamEditorEvent.OnConfirmAdd) }) {
                        Text("Felvétel", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TeamEditorEvent.OnDismissAdd) }) {
                        Text("Mégse", color = Color.Gray)
                    }
                }
            )
        }
    }
}

// --- KÁRTYÁS DIZÁJN A JÁTÉKOSOKHOZ ---
@Composable
fun EditorPlayerCardRow(
    name: String,
    isCaptain: Boolean,
    onRemoveClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Kapitány jelvény dinamikus színei
    val (captainBg, captainText) = if (isDark) {
        Color(0xFFFFC107).copy(alpha = 0.2f) to Color(0xFFFFD54F)
    } else {
        Color(0xFFFFB300) to Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Avatar ikon
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Név és státusz
                Column {
                    Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (isCaptain) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(captainBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("KAPITÁNY", color = captainText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Csak akkor mutatunk kuka ikont, ha NEM ő a kapitány
            if (!isCaptain) {
                IconButton(onClick = onRemoveClick, modifier = Modifier.testTag("kick_$name")) {
                    Icon(Icons.Default.Delete, contentDescription = "Eltávolítás", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}