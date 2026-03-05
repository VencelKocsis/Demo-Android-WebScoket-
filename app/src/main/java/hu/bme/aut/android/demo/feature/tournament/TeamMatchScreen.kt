package hu.bme.aut.android.demo.feature.tournament

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

// --- 1. UI STATE (Állapot leíró) --- // TODO refactor to separate files data class, ui state, events
data class TeamMatchUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // A meccsek fordulók szerint csoportosítva (Map<FordulóSzám, MeccsLista>)
    val teamMatchesByRound: Map<Int, List<TeamMatch>> = emptyMap(),

    // Jogosultságok
    val currentUserName: String = "",
    val userTeamIds: List<Int> = emptyList(),
    val userCaptainTeamIds: List<Int> = emptyList()
)

// --- 2. EVENTS (Események) ---
sealed class TeamMatchScreenEvent {
    object LoadTeamMatches : TeamMatchScreenEvent()
    data class OnApplyForMatch(val matchId: Int) : TeamMatchScreenEvent()
    data class OnToggleParticipantStatus(val participantId: Int, val currentStatus: String) : TeamMatchScreenEvent()
}

// --- 3. STATEFUL COMPOSABLE (A ViewModel bekötése) ---
@Composable
fun TeamMatchScreen(
    viewModel: TeamMatchViewModel = hiltViewModel(),
    onNavigateToMatchDetails: (Int) -> Unit // ÚJ: Navigáció a részletekhez
) {
    val uiState by viewModel.uiState.collectAsState()
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
        onEvent = { event -> viewModel.onEvent(event) },
        onMatchClick = onNavigateToMatchDetails // Átadjuk a kattintást
    )
}

// --- 4. STATELESS COMPOSABLE (A megjelenítés) ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TeamMatchScreenContent(
    state: TeamMatchUiState,
    onEvent: (TeamMatchScreenEvent) -> Unit,
    onMatchClick: (Int) -> Unit // ÚJ
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Bajnokság Mérkőzések") }) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.errorMessage != null) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Hiba: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { onEvent(TeamMatchScreenEvent.LoadTeamMatches) }) { Text("Újrapóbálkozás") }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.teamMatchesByRound.forEach { (roundNumber, teamMatches) ->
                        stickyHeader { RoundHeader(roundNumber) }
                        items(teamMatches) { teamMatch ->
                            // Az új, letisztult kártyát hívjuk meg
                            TeamMatchSimpleCard(
                                teamMatch = teamMatch,
                                onClick = { onMatchClick(teamMatch.id) }
                            )
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
fun TeamMatchSimpleCard(
    teamMatch: TeamMatch,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    fun openMap(location: String) {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try { context.startActivity(intent) }
        catch (e: Exception) { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    }

    val statusColor = when (teamMatch.status) {
        "scheduled" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        "in_progress" -> Color(0xFFFFC107).copy(alpha = 0.1f)
        "finished" -> Color(0xFF9E9E9E).copy(alpha = 0.1f)
        "cancelled" -> Color(0xFFFF5252).copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // A TELJES KÁRTYA KATTINTHATÓ (Részletek megnyitása)
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. Csapatok és Végeredmény (ha van)
            Text(
                text = "${teamMatch.homeTeamName} vs ${teamMatch.guestTeamName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (teamMatch.status == "finished") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Végeredmény: ${teamMatch.homeTeamScore} - ${teamMatch.guestTeamScore}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Lokáció (Ugyanolyan OutlinedButton-nel, mint a MatchDetailsScreen-en)
            if (!teamMatch.location.isNullOrEmpty()) {
                OutlinedButton(
                    onClick = { openMap(teamMatch.location) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Térkép",
                        modifier = Modifier.height(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Térkép: ${teamMatch.location}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // 3. Dátum
            teamMatch.matchDate?.let {
                Text(
                    text = "📅 $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}