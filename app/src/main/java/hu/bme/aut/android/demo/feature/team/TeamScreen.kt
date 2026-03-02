package hu.bme.aut.android.demo.feature.team

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.android.demo.domain.team.model.Team
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import kotlin.collections.isNotEmpty

// --- Adatmodellek ---
data class MatchResult(
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
    val errorMessage: String? = null
)

sealed class TeamScreenEvent {
    object LoadInitialData : TeamScreenEvent()
    data class OnTeamSelected(val teamId: Int) : TeamScreenEvent()
}

// --- 1. Állapotfüggő (Stateful) Composable ---
@Composable
fun TeamScreen(
    viewModel: TeamViewModel = hiltViewModel(),
    onNavigateToEditor: (Int) -> Unit = {}
) {
    // Állapot kinyerése a ViewModel-ből
    val uiState by viewModel.uiState.collectAsState()

    // Továbbítjuk az állapotfüggetlen UI-nak
    TeamScreenContent(
        state = uiState,
        onEvent = { event -> viewModel.onEvent(event) },
        onNavigateToEditor = onNavigateToEditor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreenContent(
    state: TeamScreenState,
    onEvent: (TeamScreenEvent) -> Unit,
    onNavigateToEditor: (Int) -> Unit,
    playedMatches: List<MatchResult> = listOf(
        MatchResult("Asztal Királyai", "2025-09-01", 9, 7, true),
        MatchResult("PingPong Heroes", "2025-09-08", 8, 8, false),
        MatchResult("Ping Pong Kings", "2025-09-15", 12, 4, true)
    )
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Csapatok áttekintése") }
            )
        },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- 1. LEGÖRDÜLŐ MENÜ (DROPDOWN) ---
            if (state.teamList.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = state.selectedTeam?.name ?: "Válassz csapatot",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Csapatok") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        state.teamList.forEach { team ->
                            DropdownMenuItem(
                                text = { Text(text = team.name) },
                                onClick = {
                                    onEvent(TeamScreenEvent.OnTeamSelected(team.id))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            } else if (!state.isLoading) {
                Text("Nincsenek elérhető csapatok.", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. KIVÁLASZTOTT CSAPAT ADATAI ---
            state.selectedTeam?.let { team ->

                // Kiírjuk a klub nevét és a divíziót (amit a DTO-ból kaptunk)
                Text(
                    text = team.clubName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!team.division.isNullOrEmpty()) {
                    Text(
                        text = "Divízió: ${team.division}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "Meccsek", value = team.matchesPlayed.toString())
                        StatItem(label = "Győzelem", value = team.wins.toString(), color = Color.Green)
                        StatItem(label = "Döntetlen", value = team.draws.toString())
                        StatItem(label = "Vereség", value = team.losses.toString(), color = Color.Red)
                        StatItem(label = "Pontok", value = team.points.toString())
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Csapattagok",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    // Játékosok listázása a Domain modell alapján
                    items(team.members) { member ->
                        ListItem(
                            headlineContent = { Text(member.name) },
                            // Ha kapitány, kiírjuk, amúgy csak "Játékos"
                            supportingContent = {
                                Text(if (member.isCaptain) "Csapatkapitány" else "Játékos")
                            },
                            leadingContent = {
                                Icon(Icons.Default.Person, contentDescription = "Játékos ikon")
                            }
                        )
                        HorizontalDivider()
                    }

                    item {
                        Text(
                            text = "Legutóbbi meccsek",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(playedMatches) { match ->
                        val resultColor = if (match.isWin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        ListItem(
                            headlineContent = { Text("vs ${match.opponent}") },
                            supportingContent = { Text(match.date) },
                            trailingContent = {
                                Text(
                                    text = "${match.homeScore} - ${match.awayScore}",
                                    fontWeight = FontWeight.Bold,
                                    color = resultColor
                                )
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
            )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}