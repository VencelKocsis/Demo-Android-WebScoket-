package hu.bme.aut.android.demo.feature.tournament.teamMatch

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import hu.bme.aut.android.demo.ui.common.MatchDateRow
import hu.bme.aut.android.demo.ui.common.MatchLocationButton
import hu.bme.aut.android.demo.util.toDisplayDate

@RequiresApi(Build.VERSION_CODES.O)
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

@Composable
fun getStatusTheme(status: String?): Pair<Color, Color> {
    return when (status) {
        "scheduled" -> Color(0xFF2E7D32).copy(alpha = 0.15f) to Color(0xFF81C784)
        "in_progress" -> Color(0xFFE91E63).copy(alpha = 0.15f) to Color(0xFFFF4081)
        "finished" -> Color(0xFF455A64).copy(alpha = 0.15f) to Color(0xFFCFD8DC)
        "cancelled" -> Color(0xFFD32F2F).copy(alpha = 0.15f) to Color(0xFFFF8A80)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@SuppressLint("RememberReturnType")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TeamMatchSimpleCard(
    teamMatch: TeamMatch,
    onClick: () -> Unit
) {
    val (bgColor, contentColor) = getStatusTheme(teamMatch.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = CardDefaults.outlinedCardBorder(enabled = teamMatch.status == "in_progress")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${teamMatch.homeTeamName} vs ${teamMatch.guestTeamName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // A nevek maradjanak fehérek/világosak
                )

                if (teamMatch.status == "in_progress") {
                    Spacer(modifier = Modifier.width(8.dp))
                    // Egy kis "LIVE" jelzés
                    LiveIndicator()
                }
            }

            // A státusz szöveg színe már az új, kontrasztos szín lesz
            Text(
                text = when(teamMatch.status) {
                    "scheduled" -> "Tervezve"
                    "in_progress" -> "FOLYAMATBAN"
                    "finished" -> "Befejezve"
                    else -> "Törölve"
                },
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            MatchLocationButton(
                location = teamMatch.location,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            MatchDateRow(date = teamMatch.matchDate)
        }
    }
}

@Composable
fun LiveIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFF4081).copy(alpha = alpha),
            modifier = Modifier.size(8.dp)
        ) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            "LIVE",
            color = Color(0xFFFF4081),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black
        )
    }
}