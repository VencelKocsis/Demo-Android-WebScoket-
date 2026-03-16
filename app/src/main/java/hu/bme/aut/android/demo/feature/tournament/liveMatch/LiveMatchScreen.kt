package hu.bme.aut.android.demo.feature.tournament.liveMatch

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                val cardAlpha = if (game.status == "pending") 0.6f else 1f

                // Kiszámoljuk, ki nyert (ha már vége a meccsnek), a vizuális kiemeléshez
                val isHomeWinner = game.status == "finished" && game.homeScore > game.guestScore
                val isGuestWinner = game.status == "finished" && game.guestScore > game.homeScore

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .alpha(cardAlpha)
                        .clickable(enabled = isClickable) {
                            onEvent(LiveMatchEvent.OpenIndividualMatchScoring(game.id))
                        },
                    // ÚJ DESIGN: Tiszta háttér, finom körvonal
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isClickable) 2.dp else 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // --- BAL OLDAL: Játékosok Nevei ---
                            Column(modifier = Modifier.weight(0.7f)) {
                                Text(
                                    text = game.homePlayerName,
                                    // Ha vége a meccsnek és NEM ő nyert, elhalványítjuk!
                                    fontWeight = if (isHomeWinner) FontWeight.Black else FontWeight.Bold,
                                    color = if (game.status == "finished" && !isHomeWinner) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "vs",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Text(
                                    text = game.guestPlayerName,
                                    fontWeight = if (isGuestWinner) FontWeight.Black else FontWeight.Bold,
                                    color = if (game.status == "finished" && !isGuestWinner) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            // --- JOBB OLDAL: Szettarány és Státusz ---
                            Column(
                                modifier = Modifier.weight(0.3f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${game.homeScore} - ${game.guestScore}",
                                    style = MaterialTheme.typography.headlineMedium, // Kicsit nagyobbra vettük
                                    fontWeight = FontWeight.Black,
                                    color = if (game.status == "finished") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // ÚJ DESIGN: Státusz "Jelvények" (Badges)
                                when (game.status) {
                                    "in_progress" -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            LiveIndicator(color = Color(0xFFFF4081))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "ÉLŐ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFF4081)
                                            )
                                        }
                                    }
                                    "finished" -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF4CAF50).copy(alpha = 0.1f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Befejezve",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF388E3C)
                                            )
                                        }
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
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

        // --- ALÁÍRÁS / LEZÁRÁS KÁRTYA ---
        val allFinished = state.individualMatches.size == 16 && state.individualMatches.all { it.status == "finished" }

        if (allFinished && state.match != null) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Jegyzőkönyv Hitelesítése",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Minden mérkőzés véget ért. Kérjük a csapatok képviselőit, hogy hagyják jóvá az eredményt!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // HAZAI CSAPAT ÁLLAPOTA
                            SignatureStatusColumn(
                                teamName = "Hazai",
                                isSigned = state.match.homeTeamSigned,
                                isMyTeam = state.myTeamSide == "HOME" && !state.isSpectator,
                                onSignClick = { onEvent(LiveMatchEvent.SignMatch) }
                            )

                            // VENDÉG CSAPAT ÁLLAPOTA
                            SignatureStatusColumn(
                                teamName = "Vendég",
                                isSigned = state.match.guestTeamSigned,
                                isMyTeam = state.myTeamSide == "GUEST" && !state.isSpectator,
                                onSignClick = { onEvent(LiveMatchEvent.SignMatch) }
                            )
                        }

                        if (state.match.homeTeamSigned && state.match.guestTeamSigned) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))) {
                                Text(
                                    text = "A MÉRKŐZÉS HIVATALOSAN LEZÁRULT!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SignatureStatusColumn(
    teamName: String,
    isSigned: Boolean,
    isMyTeam: Boolean,
    onSignClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = teamName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (isSigned) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Aláírva", tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
            Text("Jóváhagyva", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        } else {
            if (isMyTeam) {
                Button(onClick = onSignClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Aláírom")
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Várakozás...", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}