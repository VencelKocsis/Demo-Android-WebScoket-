package hu.bme.aut.android.demo.feature.team

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.domain.team.model.Team
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import hu.bme.aut.android.demo.ui.common.PerformanceGraph
import hu.bme.aut.android.demo.ui.common.StatItem
import hu.bme.aut.android.demo.ui.theme.CaptainYellow
import hu.bme.aut.android.demo.ui.theme.ErrorRedLight
import hu.bme.aut.android.demo.ui.theme.ErrorRedSolid
import hu.bme.aut.android.demo.ui.theme.SuccessGreenDark
import hu.bme.aut.android.demo.ui.theme.SuccessGreenLight
import hu.bme.aut.android.demo.ui.theme.SuccessGreenSolid
import hu.bme.aut.android.demo.ui.theme.WarningOrangeDark
import kotlin.collections.isNotEmpty

// --- Adatmodellek ---
data class MatchResult(
    val matchId: Int,
    val opponent: String,
    val date: String,
    val homeScore: Int,
    val awayScore: Int,
    val isWin: Boolean
)

data class TeamScreenState(
    val isLoading: Boolean = false,
    val teamList: List<Team> = emptyList(),
    val selectedTeam: TeamDetails? = null,
    val isCurrentUserCaptain: Boolean = false,
    val errorMessage: String? = null,
    val recentMatches: List<MatchResult> = emptyList(),
    val pointsHistory: List<Float> = emptyList(),

    // Szűrő adatok
    val availableClubs: List<String> = emptyList(),
    val availableDivisions: List<String> = emptyList(),
    val selectedClub: String? = null,
    val selectedDivision: String? = null
)

sealed class TeamScreenEvent {
    object LoadInitialData : TeamScreenEvent()
    data class OnTeamSelected(val teamId: Int) : TeamScreenEvent()
    data class OnClubSelected(val club: String?) : TeamScreenEvent()
    data class OnDivisionSelected(val division: String?) : TeamScreenEvent()
}

// --- 1. Állapotfüggő (Stateful) Composable ---
@Composable
fun TeamScreen(
    viewModel: TeamViewModel = hiltViewModel(),
    onNavigateToEditor: (Int) -> Unit = {},
    onNavigateToMatch: (Int) -> Unit = {},
    onNavigateToPlayerProfile: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(TeamScreenEvent.LoadInitialData)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    TeamScreenContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToEditor = onNavigateToEditor,
        onNavigateToMatch = onNavigateToMatch,
        onNavigateToPlayerProfile = onNavigateToPlayerProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreenContent(
    state: TeamScreenState,
    onEvent: (TeamScreenEvent) -> Unit,
    onNavigateToEditor: (Int) -> Unit,
    onNavigateToMatch: (Int) -> Unit,
    onNavigateToPlayerProfile: (String) -> Unit
) {
    var clubExpanded by remember { mutableStateOf(false) }
    var divisionExpanded by remember { mutableStateOf(false) }
    var teamExpanded by remember { mutableStateOf(false) }
    var showGraphInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.club)) }) },
        floatingActionButton = {
            if (state.isCurrentUserCaptain && state.selectedTeam != null) {
                FloatingActionButton(
                    onClick = { onNavigateToEditor(state.selectedTeam.id) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Csapat szerkesztése")
                }
            }
        }
    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { onEvent(TeamScreenEvent.LoadInitialData) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.errorMessage != null && state.teamList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // --- 1. SZŰRŐ ÉS VÁLASZTÓ KÁRTYA ---
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Felső sor: Klub és Divízió szűrő
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // KLUB SZŰRŐ
                                    ExposedDropdownMenuBox(
                                        expanded = clubExpanded, onExpandedChange = { clubExpanded = !clubExpanded },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = state.selectedClub ?: "Összes Klub",
                                            onValueChange = {}, readOnly = true, label = { Text("Klub") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clubExpanded) },
                                            modifier = Modifier.fillMaxWidth().menuAnchor(), colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                        )
                                        ExposedDropdownMenu(expanded = clubExpanded, onDismissRequest = { clubExpanded = false }) {
                                            DropdownMenuItem(text = { Text("Összes Klub", fontWeight = FontWeight.Bold) }, onClick = { onEvent(TeamScreenEvent.OnClubSelected(null)); clubExpanded = false })
                                            state.availableClubs.forEach { clubName ->
                                                DropdownMenuItem(text = { Text(clubName) }, onClick = { onEvent(TeamScreenEvent.OnClubSelected(clubName)); clubExpanded = false })
                                            }
                                        }
                                    }

                                    // DIVÍZIÓ SZŰRŐ
                                    ExposedDropdownMenuBox(
                                        expanded = divisionExpanded, onExpandedChange = { divisionExpanded = !divisionExpanded },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = state.selectedDivision ?: "Összes",
                                            onValueChange = {}, readOnly = true, label = { Text("Divízió") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = divisionExpanded) },
                                            modifier = Modifier.fillMaxWidth().menuAnchor(), colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                        )
                                        ExposedDropdownMenu(expanded = divisionExpanded, onDismissRequest = { divisionExpanded = false }) {
                                            DropdownMenuItem(text = { Text("Összes", fontWeight = FontWeight.Bold) }, onClick = { onEvent(TeamScreenEvent.OnDivisionSelected(null)); divisionExpanded = false })
                                            state.availableDivisions.forEach { div ->
                                                DropdownMenuItem(text = { Text(div) }, onClick = { onEvent(TeamScreenEvent.OnDivisionSelected(div)); divisionExpanded = false })
                                            }
                                        }
                                    }
                                }

                                // Alsó sor: A tényleges Csapat választó (Csak a szűrt elemeket mutatja)
                                ExposedDropdownMenuBox(
                                    expanded = teamExpanded,
                                    onExpandedChange = { teamExpanded = !teamExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = state.selectedTeam?.name ?: stringResource(R.string.select_team),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(stringResource(R.string.selected_team)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teamExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    )

                                    ExposedDropdownMenu(
                                        expanded = teamExpanded,
                                        onDismissRequest = { teamExpanded = false }
                                    ) {
                                        if (state.teamList.isEmpty()) {
                                            DropdownMenuItem(text = { Text("Nincs találat", color = Color.Gray) }, onClick = { teamExpanded = false })
                                        } else {
                                            state.teamList.forEach { team ->
                                                DropdownMenuItem(
                                                    text = { Text(team.name, fontWeight = FontWeight.Medium) },
                                                    onClick = {
                                                        onEvent(TeamScreenEvent.OnTeamSelected(team.id))
                                                        teamExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- 2. KIVÁLASZTOTT CSAPAT ADATAI ---
                    state.selectedTeam?.let { team ->
                        item {
                            Text(team.clubName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            if (!team.division.isNullOrEmpty()) {
                                Text(
                                    text = stringResource(R.string.division, team.division),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                            // SZÍNES STATISZTIKAI KÁRTYA
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatItem(label = stringResource(R.string.match), value = team.matchesPlayed.toString(), type = "neutral")
                                    StatItem(label = stringResource(R.string.victory), value = team.wins.toString(), type = "success")
                                    StatItem(label = stringResource(R.string.draw), value = team.draws.toString(), type = "warning")
                                    StatItem(label = stringResource(R.string.lose), value = team.losses.toString(), type = "error")
                                    StatItem(label = stringResource(R.string.point), value = team.points.toString(), type = "primary")
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                            Text(stringResource(R.string.team_frame), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // JÁTÉKOSOK LISTÁJA
                        items(team.members) { member ->
                            PlayerCardRow(
                                name = member.name,
                                isCaptain = member.isCaptain,
                                onClick = { onNavigateToPlayerProfile(member.uid)}
                            )
                        }

                        if (state.pointsHistory.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(stringResource(R.string.team_improvement), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { showGraphInfoDialog = true }, modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        PerformanceGraph(data = state.pointsHistory)
                                    }
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }

                        // LEGUTÓBBI MECCSEK
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(stringResource(R.string.previous_matches), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Ha nincs egyetlen lejátszott meccsük sem
                            if (state.recentMatches.isEmpty()) {
                                Text("Ennek a csapatnak még nincsenek befejezett mérkőzései.", color = Color.Gray)
                            }
                        }

                        items(state.recentMatches) { match ->
                            MatchResultRow(match = match, onClick = { onNavigateToMatch(match.matchId) })
                        }
                    }
                }
            }
        }
    }

    // teljesítmény gráf info dialog
    if (showGraphInfoDialog) {
        hu.bme.aut.android.demo.ui.common.InfoDialog(
            title = stringResource(R.string.team_improvement),
            text = stringResource(R.string.team_improvement_dialog_text),
            onDismiss = { showGraphInfoDialog = false }
        )
    }
}

@Composable
fun MatchResultRow(
    match: MatchResult,
    onClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()

    val (bgColor, textColor) = if (match.isWin) {
        if(isDark) SuccessGreenDark.copy(0.25f) to SuccessGreenLight else SuccessGreenSolid to Color.White
    } else {
        if(isDark) ErrorRedSolid.copy(0.25f) to ErrorRedLight else ErrorRedSolid to Color.White
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = match.opponent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = match.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${match.homeScore} - ${match.awayScore}",
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun PlayerCardRow(
    name: String,
    isCaptain: Boolean,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

            if (isCaptain) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CaptainYellow.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(stringResource(R.string.captain), color = WarningOrangeDark, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}