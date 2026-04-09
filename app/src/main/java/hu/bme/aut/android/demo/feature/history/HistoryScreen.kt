package hu.bme.aut.android.demo.feature.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToMatchDetails: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.history)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- 1: MEGHÍVJUK A SZŰRŐDOBOZT ---
            if (!uiState.isLoading && uiState.error == null) {
                HistoryFilterSection(
                    state = uiState,
                    onEvent = viewModel::onEvent
                )
            }

            // --- 2: SZŰRT LISTA MEGJELENÍTÉSE ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.filteredMatches.isEmpty()) {
                    Text(
                        text = "Még nincsenek befejezett mérkőzések a szűrés alapján.",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.filteredMatches, key = { it.id }) { match ->
                            HistoryMatchCard(
                                match = match,
                                onClick = { onNavigateToMatchDetails(match.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFilterSection(
    state: HistoryUiState,
    onEvent: (HistoryScreenEvent) -> Unit
) {
    var seasonExpanded by remember { mutableStateOf(false) }
    var divisionExpanded by remember { mutableStateOf(false) }
    var teamExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                // 1. SZEZON SZŰRŐ (Kisebb szélesség)
                ExposedDropdownMenuBox(
                    expanded = seasonExpanded, onExpandedChange = { seasonExpanded = !seasonExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    // Megkeressük a kiválasztott ID-hoz tartozó szép nevet
                    val selectedSeasonName = state.availableSeasons.find { it.first == state.selectedSeasonId }?.second ?: "Minden"

                    OutlinedTextField(
                        value = selectedSeasonName, // Itt már a szép név jelenik meg!
                        onValueChange = {}, readOnly = true, label = { Text("Szezon") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seasonExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(expanded = seasonExpanded, onDismissRequest = { seasonExpanded = false }) {
                        DropdownMenuItem(text = { Text("Minden", fontWeight = FontWeight.Bold) }, onClick = { onEvent(HistoryScreenEvent.OnSeasonSelected(null)); seasonExpanded = false })

                        // Itt bontjuk ki a Párokat (ID és Szezonnév)
                        state.availableSeasons.forEach { (sId, sName) ->
                            DropdownMenuItem(
                                text = { Text(sName) }, // Itt a legördülőben is a szép név lesz!
                                onClick = { onEvent(HistoryScreenEvent.OnSeasonSelected(sId)); seasonExpanded = false }
                            )
                        }
                    }
                }

                // 2. DIVÍZIÓ SZŰRŐ
                ExposedDropdownMenuBox(
                    expanded = divisionExpanded, onExpandedChange = { divisionExpanded = !divisionExpanded },
                    modifier = Modifier.weight(1.5f)
                ) {
                    OutlinedTextField(
                        value = state.selectedDivision ?: "Összes",
                        onValueChange = {}, readOnly = true, label = { Text("Divízió") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = divisionExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(expanded = divisionExpanded, onDismissRequest = { divisionExpanded = false }) {
                        DropdownMenuItem(text = { Text("Összes", fontWeight = FontWeight.Bold) }, onClick = { onEvent(HistoryScreenEvent.OnDivisionSelected(null)); divisionExpanded = false })
                        state.availableDivisions.forEach { div ->
                            DropdownMenuItem(text = { Text(div) }, onClick = { onEvent(HistoryScreenEvent.OnDivisionSelected(div)); divisionExpanded = false })
                        }
                    }
                }
            }

            // 3. CSAPAT SZŰRŐ
            ExposedDropdownMenuBox(expanded = teamExpanded, onExpandedChange = { teamExpanded = !teamExpanded }) {
                val selectedTeamName = state.availableTeams.find { it.first == state.selectedTeamId }?.second ?: "Összes csapat"
                val filteredTeamsForDropdown = if (state.selectedDivision != null) {
                    state.availableTeams.filter { state.teamDivisions[it.first] == state.selectedDivision }
                } else state.availableTeams

                OutlinedTextField(
                    value = selectedTeamName, onValueChange = {}, readOnly = true, label = { Text("Csapat") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teamExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(expanded = teamExpanded, onDismissRequest = { teamExpanded = false }) {
                    DropdownMenuItem(text = { Text("Összes csapat", fontWeight = FontWeight.Bold) }, onClick = { onEvent(HistoryScreenEvent.OnTeamSelected(null)); teamExpanded = false })
                    filteredTeamsForDropdown.forEach { (teamId, teamName) ->
                        DropdownMenuItem(text = { Text(teamName) }, onClick = { onEvent(HistoryScreenEvent.OnTeamSelected(teamId)); teamExpanded = false })
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryMatchCard(match: TeamMatch, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val scoreBgColor = if(isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
    val scoreTextColor = if(isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary

    val displayDate = match.matchDate?.replace("T", " ") ?: ""

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${match.roundNumber}. Forduló",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.homeTeamName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (match.homeTeamScore > match.guestTeamScore) FontWeight.Black else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.guestTeamName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (match.guestTeamScore > match.homeTeamScore) FontWeight.Black else FontWeight.Normal
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scoreBgColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${match.homeTeamScore} - ${match.guestTeamScore}",
                        color = scoreTextColor,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}