package hu.bme.aut.android.demo.feature.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.ui.common.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToMatchDetails: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history)) },
                actions = {
                    IconButton(
                        onClick = { showFilterDialog = true },
                        enabled = !uiState.isLoading && uiState.error == null
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Szűrés")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            // --- KÖZÖS SZŰRŐ DIALÓGUS ---
            if (showFilterDialog) {
                CommonFilterDialog(
                    title = stringResource(R.string.filter),
                    onDismiss = { showFilterDialog = false }
                ) {
                    GenericFilterDropdown(
                        label = stringResource(R.string.season),
                        defaultOptionText = stringResource(R.string.all),
                        options = uiState.availableSeasons,
                        selectedOption = uiState.availableSeasons.find { it.first == uiState.selectedSeasonId },
                        optionLabeler = { translateSeasonName(it.second) },
                        onOptionSelected = { viewModel.onEvent(HistoryScreenEvent.OnSeasonSelected(it?.first)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    GenericFilterDropdown(
                        label = stringResource(R.string.division_1),
                        defaultOptionText = stringResource(R.string.all),
                        options = uiState.availableDivisions,
                        selectedOption = uiState.selectedDivision,
                        optionLabeler = { it },
                        onOptionSelected = { viewModel.onEvent(HistoryScreenEvent.OnDivisionSelected(it)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val filteredTeams = if (uiState.selectedDivision != null) {
                        uiState.availableTeams.filter { uiState.teamDivisions[it.first] == uiState.selectedDivision }
                    } else uiState.availableTeams

                    GenericFilterDropdown(
                        label = stringResource(R.string.team),
                        defaultOptionText = stringResource(R.string.all_teams),
                        options = filteredTeams,
                        selectedOption = uiState.availableTeams.find { it.first == uiState.selectedTeamId },
                        optionLabeler = { it.second },
                        onOptionSelected = { viewModel.onEvent(HistoryScreenEvent.OnTeamSelected(it?.first)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // --- SZŰRT LISTA MEGJELENÍTÉSE ---
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            } else if (uiState.filteredMatches.isEmpty()) {
                Text(stringResource(R.string.no_completed_matches_based_on_filter), color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredMatches, key = { it.id }) { match ->
                        UniversalMatchCard(
                            date = match.matchDate,
                            homeTeam = match.homeTeamName,
                            guestTeam = match.guestTeamName,
                            homeScore = match.homeTeamScore,
                            guestScore = match.guestTeamScore,
                            isWin = null,
                            topLabel = stringResource(R.string.round, match.roundNumber),
                            status = null,
                            onClick = { onNavigateToMatchDetails(match.id) }
                        )
                    }
                }
            }
        }
    }
}