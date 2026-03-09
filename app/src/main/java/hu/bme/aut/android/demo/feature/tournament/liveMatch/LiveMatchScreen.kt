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

    // --- ÚJ: Lifecycle Figyelő a Visszanavigáláshoz ---
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Amikor visszatérünk a Scorer-ből, újratöltjük az eredményeket!
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

        // --- ÚJ: Pull to Refresh Doboz ---
        PullToRefreshBox(
            isRefreshing = state.isLoading && !state.isMutating, // Ne pörögjön duplán
            onRefresh = { viewModel.onEvent(LiveMatchEvent.LoadMatchData) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // Finom vonal, ha épp adatot küldünk fel a szervernek (Sorrend beküldése)
                if (state.isMutating) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
                }

                // Csak akkor mutatunk középső, nagy töltőt, ha teljesen üres a képernyő és épp töltünk
                if (state.isLoading && state.phase == LiveMatchPhase.LOADING) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    // A fázisok megjelenítése
                    when (state.phase) {
                        LiveMatchPhase.LOADING -> { /* Már lekezeltük fent */ }
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
}

@Composable
fun LineupSetupContent(
    state: LiveMatchUiState,
    onEvent: (LiveMatchEvent) -> Unit
) {
    // Reorderable Állapot Inicializálása
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
                "Nézőként csak megtekintheted az eddigi felállást."
            } else {
                "Húzd a játékosokat a megfelelő helyre! Az első 4 pozíció lép pályára, a többiek a kispadon maradnak."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        // A Drag & Drop Lista
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
                    val isStarter = index < 4 // Az első 4 ember a kezdő

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
        // A PullToRefresh miatt már nem kell feltétlenül ez a töltő is, de vizuálisan maradhat.
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
                text = "Válaszd ki a meccset a pontozáshoz! Húzd le a képernyőt a legfrissebb eredményekért.",
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
                // Ha várakozik, halványabb a kártya
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
                        // Játékosok Nevei
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
                        }

                        // Szettarány és Státusz
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

                            // A státusz szövegének meghatározása
                            val statusText = when (game.status) {
                                "pending" -> "Várakozik"
                                "in_progress" -> "JÁTÉKBAN" // Ezt nagybetűsítjük, hogy feltűnőbb legyen
                                "finished" -> "Befejezve"
                                else -> game.status
                            }

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (game.status == "in_progress") FontWeight.Bold else FontWeight.Normal,
                                color = when (game.status) {
                                    "in_progress" -> Color(0xFFE91E63) // Rózsaszín/Piros
                                    "finished" -> Color(0xFF4CAF50) // Zöld
                                    else -> Color.Gray
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}