package hu.bme.aut.android.demo.feature.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.history)) }) }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            // --- 1: GENERIKUS SZŰRŐDOBOZ ---
            if (!uiState.isLoading && uiState.error == null) {
                FilterCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    topRowContent = {
                        GenericFilterDropdown(
                            label = stringResource(R.string.season), defaultOptionText = stringResource(R.string.all),
                            options = uiState.availableSeasons, selectedOption = uiState.availableSeasons.find { it.first == uiState.selectedSeasonId },
                            optionLabeler = { it.second }, onOptionSelected = { viewModel.onEvent(HistoryScreenEvent.OnSeasonSelected(it?.first)) },
                            modifier = Modifier.weight(1f)
                        )
                        GenericFilterDropdown(
                            label = stringResource(R.string.division_1), defaultOptionText = stringResource(R.string.all),
                            options = uiState.availableDivisions, selectedOption = uiState.selectedDivision,
                            optionLabeler = { it }, onOptionSelected = { viewModel.onEvent(HistoryScreenEvent.OnDivisionSelected(it)) },
                            modifier = Modifier.weight(1.5f)
                        )
                    },
                    bottomRowContent = {
                        val filteredTeams = if (uiState.selectedDivision != null) {
                            uiState.availableTeams.filter { uiState.teamDivisions[it.first] == uiState.selectedDivision }
                        } else uiState.availableTeams

                        GenericFilterDropdown(
                            label = stringResource(R.string.team), defaultOptionText = stringResource(R.string.all_teams),
                            options = filteredTeams, selectedOption = uiState.availableTeams.find { it.first == uiState.selectedTeamId },
                            optionLabeler = { it.second }, onOptionSelected = { viewModel.onEvent(HistoryScreenEvent.OnTeamSelected(it?.first)) }
                        )
                    }
                )
            }

            // --- 2: SZŰRT LISTA MEGJELENÍTÉSE ---
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)) {
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
                                isWin = null, // Semleges (primary) színt kap a pontszám doboz
                                topLabel = "${match.roundNumber}. Forduló",
                                status = null,
                                onClick = { onNavigateToMatchDetails(match.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}