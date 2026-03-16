package hu.bme.aut.android.demo.feature.tournament.liveMatch

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.ui.common.LiveIndicator
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMatchScreen(
    matchId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToScorer: (Int) -> Unit,
    viewModel: LiveMatchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Biztonsági HTTP frissítés, ha esetleg a WS kapcsolat megszakadt volna a háttérben
                viewModel.onEvent(LiveMatchEvent.LoadMatchData)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Élő Mérkőzés") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Vissza")
                    }
                }
            )
        }
    ) { paddingValues ->

        // Csak akkor engedjük a kézi "lehúzós" frissítést, ha Még NEM vagyunk a Match Grid fázisban!
        val isPullToRefreshEnabled = state.phase != LiveMatchPhase.MATCH_GRID

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Ha épp a szerverre küldünk adatot, egy finom csíkot mutatunk felül
            if (state.isMutating) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }

            // Ha engedélyezett a frissítés, beletesszük a dobozba
            if (isPullToRefreshEnabled) {
                PullToRefreshBox(
                    isRefreshing = state.isLoading && !state.isMutating,
                    onRefresh = { viewModel.onEvent(LiveMatchEvent.LoadMatchData) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    ScreenContent(state, viewModel, onNavigateToScorer)
                }
            } else {
                // Ha a Grid-en vagyunk (ahol a WebSocket amúgy is tolja az adatokat), nincs Pull-to-refresh
                ScreenContent(state, viewModel, onNavigateToScorer)
            }

            // Hibaüzenet kezelése
            state.errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

// A belső tartalom kiszervezése, hogy ne duplikáljuk a kódot az if-else ágban
@Composable
private fun BoxScope.ScreenContent(
    state: LiveMatchUiState,
    viewModel: LiveMatchViewModel,
    onNavigateToScorer: (Int) -> Unit
) {
    if (state.isLoading && state.phase == LiveMatchPhase.LOADING) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    } else {
        when (state.phase) {
            LiveMatchPhase.LOADING -> { }
            LiveMatchPhase.LINEUP_SETUP -> {
                LineupSetupContent(state = state, onEvent = viewModel::onEvent)
            }
            LiveMatchPhase.WAITING_FOR_OPPONENT -> {
                WaitingForOpponentContent(
                    state = state,
                    onRefresh = { viewModel.onEvent(LiveMatchEvent.LoadMatchData) }
                )
            }
            LiveMatchPhase.MATCH_GRID -> {
                MatchGridContent(
                    state = state,
                    onEvent = { event ->
                        if (event is LiveMatchEvent.OpenIndividualMatchScoring) {
                            onNavigateToScorer(event.individualMatchId)
                        } else {
                            viewModel.onEvent(event)
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun LineupSetupContent(
    state: LiveMatchUiState,
    onEvent: (LiveMatchEvent) -> Unit
) {
    val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
        onEvent(LiveMatchEvent.MovePlayer(from.index, to.index))
    })

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (state.isSpectator) "Csapatkeret" else "Állítsd be a sorrendet!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (state.isSpectator) {
                "Nézőként csak megtekintheted az eddigi felállást.\nHúzd le a frissítéshez!"
            } else {
                "Húzd a játékosokat a megfelelő helyre! Az első 4 pozíció lép pályára, a többiek a kispadon maradnak."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            state = reorderState.listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .reorderable(reorderState)
        ) {
            items(state.lineupList, key = { it.id }) { player ->
                ReorderableItem(reorderState, key = player.id) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                    val index = state.lineupList.indexOf(player)
                    val isStarter = index < 4

                    var cardModifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .shadow(elevation, MaterialTheme.shapes.medium)

                    if (!state.isSpectator) {
                        cardModifier = cardModifier.detectReorderAfterLongPress(reorderState)
                    }

                    Card(
                        modifier = cardModifier,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isStarter) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!state.isSpectator) {
                                Icon(Icons.Default.DragHandle, contentDescription = "Fogd és vidd")
                                Spacer(modifier = Modifier.width(16.dp))
                            }

                            Column {
                                Text(
                                    text = if (isStarter) "${index + 1}. Játékos" else "Kispad",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isStarter) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                                Text(
                                    text = player.playerName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isStarter) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!state.isSpectator) {
            val isReady = state.lineupList.size >= 4
            Button(
                onClick = { onEvent(LiveMatchEvent.SubmitLineup) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = isReady && !state.isMutating,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Sorrend beküldése")
            }
        }
    }
}

@Composable
fun WaitingForOpponentContent(
    state: LiveMatchUiState,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(24.dp))
        Text("Sorrend leadva! ✅", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Várakozás a(z) ${if (state.myTeamSide == "HOME") "vendég" else "hazai"} csapatra, hogy ők is beállítsák a sorrendet...\n\nHúzd lefelé a képernyőt a frissítéshez!",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MatchGridContent(
    state: LiveMatchUiState,
    onEvent: (LiveMatchEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                text = "Egyéni Mérkőzések",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Válaszd ki a meccset a pontozáshoz! Az eredmények automatikusan (élőben) frissülnek.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        val matchesByRound = state.individualMatches
            .sortedBy { it.orderNumber }
            .groupBy { ((it.orderNumber - 1) / 4) + 1 }

        for ((round, matches) in matchesByRound) {
            item(key = "round_$round") {
                Text(
                    text = "$round. Kör",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(matches, key = { it.id }) { game ->

                val isClickable = game.status != "pending"
                val cardAlpha = if (game.status == "pending") 0.5f else 1f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .alpha(cardAlpha)
                        .clickable(enabled = isClickable) {
                            onEvent(LiveMatchEvent.OpenIndividualMatchScoring(game.id))
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isClickable) 2.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(0.7f)) {
                            Text(
                                text = game.homePlayerName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "vs",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                            Text(
                                text = game.guestPlayerName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (game.status == "finished" && !game.setScores.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = game.setScores,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(0.3f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${game.homeScore} - ${game.guestScore}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (game.status == "finished") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            when (game.status) {
                                "in_progress" -> {
                                    LiveIndicator(color = Color(0xFFFF4081))
                                }
                                "finished" -> {
                                    Text(
                                        text = "Befejezve",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "Várakozik",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}