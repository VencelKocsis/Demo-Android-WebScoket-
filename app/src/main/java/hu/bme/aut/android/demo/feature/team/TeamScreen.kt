package hu.bme.aut.android.demo.feature.team

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import hu.bme.aut.android.demo.ui.common.CommonFilterDialog
import hu.bme.aut.android.demo.ui.common.GenericFilterDropdown
import hu.bme.aut.android.demo.ui.common.InfoDialog
import hu.bme.aut.android.demo.ui.common.RatingGraphCard
import hu.bme.aut.android.demo.ui.common.StatItem
import hu.bme.aut.android.demo.ui.common.UniversalMatchCard
import hu.bme.aut.android.demo.ui.theme.CaptainYellow
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

@RequiresApi(Build.VERSION_CODES.O)
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
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onEvent(TeamScreenEvent.LoadInitialData)
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreenContent(
    state: TeamScreenState,
    onEvent: (TeamScreenEvent) -> Unit,
    onNavigateToEditor: (Int) -> Unit,
    onNavigateToMatch: (Int) -> Unit,
    onNavigateToPlayerProfile: (String) -> Unit
) {
    var showGraphInfoDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.club)) },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Szűrés")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.isCurrentUserCaptain && state.selectedTeam != null) {
                FloatingActionButton(onClick = { onNavigateToEditor(state.selectedTeam.id) }, containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Default.Edit, contentDescription = "Szerkesztés")
                }
            }
        }
    ) { paddingValues ->

        // --- KÖZÖS SZŰRŐ DIALÓGUS ---
        if (showFilterDialog) {
            CommonFilterDialog(
                title = stringResource(R.string.select_team),
                onDismiss = { showFilterDialog = false }
            ) {
                GenericFilterDropdown(
                    label = stringResource(R.string.club),
                    defaultOptionText = stringResource(R.string.all_clubs),
                    options = state.availableClubs,
                    selectedOption = state.selectedClub,
                    optionLabeler = { it },
                    onOptionSelected = { onEvent(TeamScreenEvent.OnClubSelected(it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                GenericFilterDropdown(
                    label = stringResource(R.string.division_1),
                    defaultOptionText = stringResource(R.string.all),
                    options = state.availableDivisions,
                    selectedOption = state.selectedDivision,
                    optionLabeler = { it },
                    onOptionSelected = { onEvent(TeamScreenEvent.OnDivisionSelected(it)) },
                    modifier = Modifier.fillMaxWidth()
                )

                val currentTeam = state.teamList.find { it.id == state.selectedTeam?.id }
                GenericFilterDropdown(
                    label = stringResource(R.string.selected_team),
                    defaultOptionText = stringResource(R.string.select_team),
                    options = state.teamList,
                    selectedOption = currentTeam,
                    optionLabeler = { it.name },
                    onOptionSelected = { if (it != null) onEvent(TeamScreenEvent.OnTeamSelected(it.id)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

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
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {

                    // 1. KIVÁLASZTOTT CSAPAT ADATAI
                    state.selectedTeam?.let { team ->
                        item {
                            Text(team.clubName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            if (!team.division.isNullOrEmpty()) {
                                Text(stringResource(R.string.division, team.division), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Row(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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

                        // Játékosok
                        items(team.members) { member ->
                            PlayerCardRow(name = member.name, isCaptain = member.isCaptain, onClick = { onNavigateToPlayerProfile(member.uid) })
                        }

                        // Grafikon
                        if (state.pointsHistory.isNotEmpty()) {
                            item { RatingGraphCard(ratingHistory = state.pointsHistory, onInfoClick = { showGraphInfoDialog = true }) }
                        }

                        // Legutóbbi meccsek
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(stringResource(R.string.previous_matches), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (state.recentMatches.isEmpty()) {
                                Text("Ennek a csapatnak még nincsenek befejezett mérkőzései.", color = Color.Gray)
                            }
                        }

                        // GENERIKUS MECCSKÁRTYA HASZNÁLATA
                        items(state.recentMatches) { match ->
                            Spacer(modifier = Modifier.height(8.dp))
                            UniversalMatchCard(
                                date = match.date,
                                homeTeam = match.opponent,
                                guestTeam = null,
                                homeScore = match.homeScore,
                                guestScore = match.awayScore,
                                isWin = match.isWin,
                                status = "finished",
                                onClick = { onNavigateToMatch(match.matchId) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showGraphInfoDialog) {
        InfoDialog(title = stringResource(R.string.team_improvement), text = stringResource(R.string.team_improvement_dialog_text), onDismiss = { showGraphInfoDialog = false })
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