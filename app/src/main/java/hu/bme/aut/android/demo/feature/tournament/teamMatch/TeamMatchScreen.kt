package hu.bme.aut.android.demo.feature.tournament.teamMatch

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.ui.common.MatchDateRow
import hu.bme.aut.android.demo.ui.common.MatchLocationButton
import hu.bme.aut.android.demo.ui.common.MatchStatusChip

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

@RequiresApi(Build.VERSION_CODES.O)
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

        // --- PULL TO REFRESH DOBOZ A TELJES KÉPERNYŐRE ---
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { onEvent(TeamMatchScreenEvent.LoadTeamMatches) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Box(modifier = Modifier.fillMaxSize()) {

                if (state.isLoading && state.teamMatchesByRound.isEmpty()) {
                    // Csak akkor mutatunk teljes képernyős töltést, ha még nincs egy adatunk sem
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.teamMatchesByRound.isEmpty() && state.errorMessage == null) {
                    // Ha nincs meccs
                    Text(
                        text = "Nincsenek elérhető mérkőzések a bajnokságban.",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                } else {
                    // A meccsek listája
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
                }

                // Hibaüzenet megjelenítése lebegve az alján
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

@SuppressLint("RememberReturnType")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TeamMatchSimpleCard(
    teamMatch: TeamMatch,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val statusColor = when (teamMatch.status) {
        "scheduled" -> if (isDark) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFE8F5E9)
        "in_progress" -> if (isDark) Color(0xFFE91E63).copy(alpha = 0.15f) else Color(0xFFFCE4EC)
        "finished" -> if (isDark) Color(0xFF455A64).copy(alpha = 0.15f) else Color(0xFFECEFF1)
        "cancelled" -> if (isDark) Color(0xFFD32F2F).copy(alpha = 0.15f) else Color(0xFFFFEBEE)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = when (teamMatch.status) {
        "scheduled" -> if (isDark) Color(0xFF81C784).copy(alpha = 0.5f) else Color(0xFFA5D6A7)
        "in_progress" -> if (isDark) Color(0xFFFF4081).copy(alpha = 0.5f) else Color(0xFFF48FB1)
        "finished" -> if (isDark) Color(0xFFCFD8DC).copy(alpha = 0.5f) else Color(0xFFCFD8DC)
        "cancelled" -> if (isDark) Color(0xFFFF8A80).copy(alpha = 0.5f) else Color(0xFFEF9A9A)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${teamMatch.homeTeamName} vs ${teamMatch.guestTeamName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            MatchStatusChip(status = teamMatch.status)

            if (teamMatch.status == "finished") {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Végeredmény: ${teamMatch.homeTeamScore} - ${teamMatch.guestTeamScore}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            MatchLocationButton(
                location = teamMatch.location,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            MatchDateRow(date = teamMatch.matchDate)
        }
    }
}