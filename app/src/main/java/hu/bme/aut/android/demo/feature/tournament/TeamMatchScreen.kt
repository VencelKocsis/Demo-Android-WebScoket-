package hu.bme.aut.android.demo.feature.tournament

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

// --- 1. UI STATE (Állapot leíró) ---
data class TeamMatchUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // A meccsek fordulók szerint csoportosítva (Map<FordulóSzám, MeccsLista>)
    val teamMatchesByRound: Map<Int, List<TeamMatch>> = emptyMap()
)

// --- 2. EVENTS (Események) ---
sealed class TeamMatchScreenEvent {
    object LoadTeamMatches : TeamMatchScreenEvent()
    // Itt bővítheted később: pl. data class OnMatchClicked(val id: Int) : MatchScreenEvent()
}

// --- 3. STATEFUL COMPOSABLE (A ViewModel bekötése) ---
@Composable
fun TeamMatchScreen(
    viewModel: TeamMatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TeamMatchScreenContent(
        state = uiState,
        onEvent = { event -> viewModel.onEvent(event) }
    )
}

// --- 4. STATELESS COMPOSABLE (A megjelenítés) ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TeamMatchScreenContent(
    state: TeamMatchUiState,
    onEvent: (TeamMatchScreenEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bajnokság Mérkőzések") },
                actions = {
                    IconButton(onClick = { onEvent(TeamMatchScreenEvent.LoadTeamMatches) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Frissítés")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Töltésjelző
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Hibaüzenet
            else if (state.errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hiba: ${state.errorMessage}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { onEvent(TeamMatchScreenEvent.LoadTeamMatches) }) {
                        Text("Újrapóbálkozás")
                    }
                }
            }

            // Sikeres lista megjelenítése
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.teamMatchesByRound.forEach { (roundNumber, teamMatches) ->

                        // Sticky Header a fordulókhoz
                        stickyHeader {
                            RoundHeader(roundNumber)
                        }

                        // Csapatmeccsek listázása
                        items(teamMatches) { teamMatch ->
                            TeamMatchItemCard(teamMatch = teamMatch)
                        }
                    }
                }
            }
        }
    }
}

// --- SEGÉDKOMPONENSEK ---
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
            text = "Forduló $roundNumber",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TeamMatchItemCard(teamMatch: TeamMatch) {
    // Színkódolás státusz alapján
    val statusColor = when (teamMatch.status) {
        "scheduled" -> Color(0xFF4CAF50).copy(alpha = 0.2f) // Zöldes
        "in_progress" -> Color(0xFFFFC107).copy(alpha = 0.2f) // Sárgás
        "finished" -> Color(0xFF9E9E9E).copy(alpha = 0.2f)    // Szürke
        "cancelled" -> Color(0xFFFF5252).copy(alpha = 0.2f)   // Piros
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Fejléc: Csapatnevek és lenyitó nyíl
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { expanded = !expanded }
            ) {
                Text(
                    text = "${teamMatch.homeTeamName} vs ${teamMatch.guestTeamName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Részletek"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Információk
            if (!teamMatch.location.isNullOrEmpty()) {
                Text("Helyszín: ${teamMatch.location}", style = MaterialTheme.typography.bodyMedium)
            }
            if (!teamMatch.matchDate.isNullOrEmpty()) {
                Text("Dátum: ${teamMatch.matchDate}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gombok és Státusz
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Térkép gomb
                if (!teamMatch.location.isNullOrEmpty()) {
                    OutlinedButton(
                        onClick = { /* TODO: Térkép */ },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.height(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Térkép", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Státuszfüggő tartalom
                when (teamMatch.status) {
                    "scheduled" -> {
                        Button(
                            onClick = { /* TODO */ },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Jelentkezés", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    "finished" -> {
                        Text(
                            text = "${teamMatch.homeTeamScore} - ${teamMatch.guestTeamScore}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            // Lenyíló részletek
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Részletes eredmények hamarosan...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}