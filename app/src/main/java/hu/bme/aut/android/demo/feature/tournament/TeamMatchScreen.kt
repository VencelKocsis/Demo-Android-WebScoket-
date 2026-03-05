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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch

@Composable
fun TeamMatchScreen(
    viewModel: TeamMatchViewModel = hiltViewModel(),
    onNavigateToMatchDetails: (Int) -> Unit
) {
    // REFAKTOR: Lifecycle-aware állapofigyelés
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Meghúzzuk a frissítés ravaszt
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TeamMatchScreenContent(
    state: TeamMatchUiState,
    onEvent: (TeamMatchScreenEvent) -> Unit,
    onMatchClick: (Int) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Bajnokság Mérkőzések") }) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (state.isLoading && state.teamMatchesByRound.isEmpty()) {
                // Csak akkor mutatunk teljes képernyős töltést, ha még nincs egy adatunk sem
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.teamMatchesByRound.forEach { (roundNumber, teamMatches) ->
                        stickyHeader { RoundHeader(roundNumber) }

                        items(teamMatches) { teamMatch ->
                            TeamMatchSimpleCard(
                                teamMatch = teamMatch,
                                onClick = { onMatchClick(teamMatch.id) }
                            )
                        }
                    }
                }

                // Halvány töltésjelző a tetején, ha adat már van, de épp frissítünk (pl. gombnyomás után)
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    )
                }
            }

            // Hibaüzenet megjelenítése
            if (state.errorMessage != null && !state.isLoading) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onEvent(TeamMatchScreenEvent.LoadTeamMatches) }) {
                            Text("Újrapóbálkozás")
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
            .clickable { onClick() }, // KATTINTHATÓ A KÁRTYA
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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