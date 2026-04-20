package hu.bme.aut.android.demo.feature.tournament.teamMatch

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.ui.common.CommonFilterDialog
import hu.bme.aut.android.demo.ui.common.GenericFilterDropdown
import hu.bme.aut.android.demo.ui.common.LiveIndicator
import hu.bme.aut.android.demo.ui.common.UniversalMatchCard
import hu.bme.aut.android.demo.ui.theme.ProgressPink
import kotlinx.coroutines.isActive

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
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.championship)) },
                actions = {
                    IconButton(
                        onClick = { showFilterDialog = true },
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Szűrés")
                    }
                }
            )
        }
    ) { paddingValues ->

        // --- SZŰRŐ DIALÓGUS ---
        if (showFilterDialog) {
            CommonFilterDialog(
                title = stringResource(R.string.filter),
                onDismiss = { showFilterDialog = false }
            ) {
                GenericFilterDropdown(
                    label = stringResource(R.string.division_1),
                    defaultOptionText = stringResource(R.string.all),
                    options = state.availableDivisions,
                    selectedOption = state.selectedDivision,
                    optionLabeler = { it },
                    onOptionSelected = { onEvent(TeamMatchScreenEvent.OnDivisionSelected(it)) },
                    modifier = Modifier.fillMaxWidth()
                )

                val filteredTeams = if (state.selectedDivision != null) {
                    state.availableTeams.filter { state.teamDivisions[it.first] == state.selectedDivision }
                } else state.availableTeams

                GenericFilterDropdown(
                    label = stringResource(R.string.team),
                    defaultOptionText = stringResource(R.string.all_teams),
                    options = filteredTeams,
                    selectedOption = state.availableTeams.find { it.first == state.selectedTeamId },
                    optionLabeler = { it.second },
                    onOptionSelected = { onEvent(TeamMatchScreenEvent.OnTeamSelected(it?.first)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { onEvent(TeamMatchScreenEvent.LoadTeamMatches) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- VÉGTELEN ÉLŐ MECCSEK SÁVJA (TICKER) ---
                if (state.liveMatches.isNotEmpty()) {

                    // Csak akkor végtelenítjük és mozgatjuk, ha több mint 1 meccs megy épp élőben
                    val isTicker = state.liveMatches.size > 1

                    val listState = rememberLazyListState(
                        initialFirstVisibleItemIndex = if (isTicker) (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % state.liveMatches.size) else 0
                    )
                    val isDragged by listState.interactionSource.collectIsDraggedAsState()

                    // Automatikus görgető effekt
                    LaunchedEffect(isDragged, state.liveMatches.size) {
                        if (!isDragged && isTicker) {
                            while (isActive) {
                                // Szépen, lassan balra görget (20 másodperc alatt 1000 pixel)
                                listState.animateScrollBy(
                                    value = 1000f,
                                    animationSpec = tween(durationMillis = 20000, easing = LinearEasing)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.live_matches),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = ProgressPink,
                            modifier = Modifier.padding(start = 16.dp, top = 12.dp)
                        )

                        LazyRow(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // A lista darabszáma VÉGTELEN, ha több élő meccs van
                            items(
                                count = if (isTicker) Int.MAX_VALUE else state.liveMatches.size,
                                itemContent = { index ->
                                    val liveMatch = state.liveMatches[index % state.liveMatches.size]

                                    CompactLiveMatchCard(
                                        homeTeam = liveMatch.homeTeamName,
                                        guestTeam = liveMatch.guestTeamName,
                                        homeScore = liveMatch.homeTeamScore,
                                        guestScore = liveMatch.guestTeamScore,
                                        onClick = { onMatchClick(liveMatch.id) }
                                    )
                                }
                            )
                        }
                    }
                }

                // --- NORMÁL TARTALOM (Lista vagy Hiba) ---
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()) {
                    if (state.isLoading && state.teamMatchesByRound.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (state.teamMatchesByRound.isEmpty() && state.errorMessage == null) {
                        Text(
                            text = stringResource(R.string.no_available_matches),
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("match_list"),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            state.teamMatchesByRound.forEach { (roundNumber, teamMatches) ->
                                stickyHeader { RoundHeader(roundNumber) }

                                items(teamMatches) { teamMatch ->
                                    UniversalMatchCard(
                                        date = teamMatch.matchDate,
                                        homeTeam = teamMatch.homeTeamName,
                                        guestTeam = teamMatch.guestTeamName,
                                        homeScore = teamMatch.homeTeamScore,
                                        guestScore = teamMatch.guestTeamScore,
                                        isWin = null,
                                        status = teamMatch.status,
                                        location = teamMatch.location,
                                        onClick = { onMatchClick(teamMatch.id) }
                                    )
                                }
                            }
                        }
                    }

                    // Hibaüzenet
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
                                Text(text = state.errorMessage, color = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { onEvent(TeamMatchScreenEvent.LoadTeamMatches) }) {
                                    Text(stringResource(R.string.retry))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactLiveMatchCard(
    homeTeam: String,
    guestTeam: String,
    homeScore: Int?,
    guestScore: Int?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, ProgressPink),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Élő jelzés
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveIndicator(color = ProgressPink)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.status_in_progress), color = ProgressPink, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hazai Csapat
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = homeTeam,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = homeScore?.toString() ?: "-",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Vendég Csapat
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = guestTeam,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = guestScore?.toString() ?: "-",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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
            text = stringResource(R.string.f_round, roundNumber),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}