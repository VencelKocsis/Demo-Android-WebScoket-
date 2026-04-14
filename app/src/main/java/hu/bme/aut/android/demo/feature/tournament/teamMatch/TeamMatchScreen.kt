package hu.bme.aut.android.demo.feature.tournament.teamMatch

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.ui.common.CommonFilterDialog
import hu.bme.aut.android.demo.ui.common.GenericFilterDropdown
import hu.bme.aut.android.demo.ui.common.UniversalMatchCard

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TeamMatchScreen(
    viewModel: TeamMatchViewModel = hiltViewModel(),
    onNavigateToMatchDetails: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(TeamMatchScreenEvent.LoadTeamMatches)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    TeamMatchScreenContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        onMatchClick = onNavigateToMatchDetails
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TeamMatchScreenContent(
    state: TeamMatchUiState,
    onEvent: (TeamMatchScreenEvent) -> Unit,
    onMatchClick: (Int) -> Unit
) {
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.championship)) },
                actions = {
                    IconButton(
                        onClick = { showFilterDialog = true },
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Szűrés")
                    }
                }
            )
        }
    ) { paddingValues ->

        // --- SZŰRŐ DIALÓGUS ---
        if (showFilterDialog) {
            CommonFilterDialog(
                title = "Szűrés",
                onDismiss = { showFilterDialog = false }
            ) {
                GenericFilterDropdown(
                    label = stringResource(R.string.division_1),
                    defaultOptionText = stringResource(R.string.all),
                    options = state.availableDivisions,
                    selectedOption = state.selectedDivision,
                    optionLabeler = { it },
                    onOptionSelected = { onEvent(TeamMatchScreenEvent.OnDivisionSelected(it)) },
                    modifier = Modifier.fillMaxWidth()
                )

                val filteredTeams = if (state.selectedDivision != null) {
                    state.availableTeams.filter { state.teamDivisions[it.first] == state.selectedDivision }
                } else state.availableTeams

                GenericFilterDropdown(
                    label = stringResource(R.string.team),
                    defaultOptionText = stringResource(R.string.all_teams),
                    options = filteredTeams,
                    selectedOption = state.availableTeams.find { it.first == state.selectedTeamId },
                    optionLabeler = { it.second }, // Itt jött elő a hiba a te kódodban, de most már működik!
                    onOptionSelected = { onEvent(TeamMatchScreenEvent.OnTeamSelected(it?.first)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- TARTALOM ---
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { onEvent(TeamMatchScreenEvent.LoadTeamMatches) },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                if (state.isLoading && state.teamMatchesByRound.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.teamMatchesByRound.isEmpty() && state.errorMessage == null) {
                    Text(
                        text = stringResource(R.string.no_available_matches),
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("match_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.teamMatchesByRound.forEach { (roundNumber, teamMatches) ->
                            stickyHeader { RoundHeader(roundNumber) }

                            items(teamMatches) { teamMatch ->
                                UniversalMatchCard(
                                    date = teamMatch.matchDate,
                                    homeTeam = teamMatch.homeTeamName,
                                    guestTeam = teamMatch.guestTeamName,
                                    homeScore = teamMatch.homeTeamScore,
                                    guestScore = teamMatch.guestTeamScore,
                                    isWin = null, // Semleges szín
                                    status = teamMatch.status, // Ettől kap pilulát és háttérszínt
                                    location = teamMatch.location, // Ettől lesz térkép
                                    onClick = { onMatchClick(teamMatch.id) }
                                )
                            }
                        }
                    }
                }

                // Hibaüzenet megjelenítése lebegve az alján
                if (state.errorMessage != null && !state.isLoading) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = state.errorMessage, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { onEvent(TeamMatchScreenEvent.LoadTeamMatches) }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoundHeader(roundNumber: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.f_round, roundNumber),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}