package hu.bme.aut.android.demo.feature.tournament.match

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.domain.teammatch.model.IndividualMatch
import hu.bme.aut.android.demo.domain.teammatch.model.MatchParticipant
import hu.bme.aut.android.demo.ui.common.LiveIndicator
import hu.bme.aut.android.demo.ui.common.MatchDateRow
import hu.bme.aut.android.demo.ui.common.MatchLocationButton
import hu.bme.aut.android.demo.ui.common.MatchStatusChip
import hu.bme.aut.android.demo.ui.theme.ErrorRed
import hu.bme.aut.android.demo.ui.theme.ProgressPink
import hu.bme.aut.android.demo.ui.theme.ProgressPinkDark
import hu.bme.aut.android.demo.ui.theme.SuccessGreen
import hu.bme.aut.android.demo.ui.theme.SuccessGreenSolid
import hu.bme.aut.android.demo.util.addMatchToCalendar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    matchId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToLiveMatch: () -> Unit = {},
    viewModel: MatchDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(MatchDetailsEvent.LoadMatch)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.actionError) {
        state.actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(MatchDetailsEvent.ClearActionError)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.match_details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Visszalépés")
                    }
                }
            )
        }
    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.onEvent(MatchDetailsEvent.LoadMatch) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Box(modifier = Modifier.fillMaxSize()) {

                if (state.isMutating) {
                    LinearProgressIndicator(modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter))
                }

                if (state.errorMessage != null && state.match == null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = stringResource(R.string.failure, state.errorMessage!!), color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.onEvent(MatchDetailsEvent.LoadMatch) }) {
                            Text(stringResource(R.string.reload))
                        }
                    }
                } else {
                    state.match?.let { match ->

                        // Kiszámoljuk az ÉLŐ vagy VÉGLEGES állást az egyéni meccsek alapján
                        val liveHomeScore = match.individualMatches.count { it.status == "finished" && it.homeScore > it.guestScore }
                        val liveGuestScore = match.individualMatches.count { it.status == "finished" && it.guestScore > it.homeScore }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            // --- 1. FEJLÉC ÉS EREDMÉNY ---
                            item {
                                Text(
                                    text = "${match.homeTeamName} vs ${match.guestTeamName}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Eredmény megjelenítése dinamikusan
                                if (match.status == "finished") {
                                    Text(
                                        text = stringResource(
                                            R.string.final_result,
                                            liveHomeScore,
                                            liveGuestScore
                                        ),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                } else if (match.status == "in_progress") {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LiveIndicator(color = ProgressPink)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(
                                                R.string.live_state,
                                                liveHomeScore,
                                                liveGuestScore
                                            ),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = ProgressPink,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                MatchStatusChip(status = match.status)

                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // --- 2. IDŐPONT ÉS HELYSZÍN ---
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = CardDefaults.outlinedCardBorder(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Infó",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.match_information),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Maguk az adatok
                                        MatchDateRow(date = match.matchDate)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        MatchLocationButton(location = match.location)
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                                Text(
                                    text = if (match.status == "scheduled") stringResource(R.string.applicants_and_frames) else stringResource(
                                        R.string.starting_team_and_statistics
                                    ),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // --- 3. CSAPATKERETEK / STATISZTIKÁK ---
                            item {
                                if (match.status == "scheduled") {
                                    RosterCard(
                                        teamName = stringResource(R.string.x_home, match.homeTeamName),
                                        roster = state.homeRoster,
                                        canEdit = state.isHomeCaptain,
                                        isLoading = state.isMutating,
                                        selectedCount = state.homeSelectedCount,
                                        onToggle = { rosterItem -> viewModel.onEvent(MatchDetailsEvent.OnCaptainTogglePlayer(rosterItem)) }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    RosterCard(
                                        teamName = stringResource(R.string.x_guest, match.guestTeamName),
                                        roster = state.guestRoster,
                                        canEdit = state.isGuestCaptain,
                                        isLoading = state.isMutating,
                                        selectedCount = state.guestSelectedCount,
                                        onToggle = { rosterItem -> viewModel.onEvent(MatchDetailsEvent.OnCaptainTogglePlayer(rosterItem)) }
                                    )
                                } else {
                                    // ELINDULT / BEFEJEZETT MECCS: Aktív kezdőcsapat és egyéni győzelmek!
                                    ActiveRosterCard(
                                        teamName = stringResource(
                                            R.string.x_home,
                                            match.homeTeamName),
                                        participants = match.participants.filter { it.teamSide == "HOME" },
                                        individualMatches = match.individualMatches
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    ActiveRosterCard(
                                        teamName = stringResource(
                                            R.string.x_guest,
                                            match.guestTeamName
                                        ),
                                        participants = match.participants.filter { it.teamSide == "GUEST" },
                                        individualMatches = match.individualMatches
                                    )
                                }
                            }

                            // --- 4. AKCIÓ GOMBOK ---
                            if (state.isUserInvolved) {
                                item {
                                    Spacer(modifier = Modifier.height(32.dp))

                                    if (match.status == "in_progress" || match.status == "scheduled") {
                                        OutlinedButton(
                                            onClick = { addMatchToCalendar(context, match) },
                                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.Event, contentDescription = "Naptár")
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Hozzáadás a Naptárhoz", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (match.status == "in_progress" || match.status == "finished") {
                                        Button(
                                            onClick = onNavigateToLiveMatch,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ProgressPinkDark)
                                        ) {
                                            Text(if (match.status == "finished") stringResource(R.string.detailed_results) else stringResource(
                                                R.string.to_live_match
                                            ), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    else if (match.status == "scheduled") {
                                        if (!state.hasApplied) {
                                            Button(
                                                onClick = { viewModel.onEvent(MatchDetailsEvent.OnApply) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp),
                                                enabled = !state.isMutating
                                            ) {
                                                Text(stringResource(R.string.apply_for_match), fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            val statusMsg = if (state.myStatus == "SELECTED") stringResource(
                                                R.string.captain_selected_you
                                            ) else stringResource(R.string.wait_for_captain_selection)
                                            val statusColor = if (state.myStatus == "SELECTED") SuccessGreen else MaterialTheme.colorScheme.primary

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            statusColor.copy(alpha = 0.1f),
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            statusColor.copy(alpha = 0.3f),
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .padding(16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = statusMsg, color = statusColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                TextButton(
                                                    onClick = { viewModel.onEvent(MatchDetailsEvent.OnWithdrawApplication) },
                                                    enabled = !state.isMutating
                                                ) {
                                                    Text(stringResource(R.string.cancel_apply), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        val isCaptainOfCurrentMatch = state.isHomeCaptain || state.isGuestCaptain
                                        if (isCaptainOfCurrentMatch) {
                                            HorizontalDivider()
                                            Spacer(modifier = Modifier.height(16.dp))

                                            val isHomeReady = state.homeSelectedCount >= 4
                                            val isGuestReady = state.guestSelectedCount >= 4

                                            if (isHomeReady && isGuestReady) {
                                                Button(
                                                    onClick = {
                                                        viewModel.onEvent(MatchDetailsEvent.OnFinalizeRoster)
                                                        addMatchToCalendar(context, match)
                                                        onNavigateToLiveMatch()
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(56.dp),
                                                    enabled = !state.isMutating,
                                                    colors = ButtonDefaults.buttonColors(containerColor = ProgressPinkDark)
                                                ) {
                                                    Text(stringResource(R.string.start_match), fontWeight = FontWeight.Black)
                                                }
                                            } else {
                                                Button(
                                                    onClick = { },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(56.dp),
                                                    enabled = false
                                                ) {
                                                    Text(stringResource(R.string.start_match))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 1. ÁLLAPOT: TERVEZETT MECCS (Jelentkezők és Kiválasztás)
// ---------------------------------------------------------
@Composable
fun RosterCard(
    teamName: String,
    roster: List<MatchRosterItem>,
    canEdit: Boolean,
    isLoading: Boolean,
    selectedCount: Int,
    onToggle: (MatchRosterItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = teamName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (selectedCount >= 4) SuccessGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$selectedCount / 4 Kiválasztva",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedCount >= 4) SuccessGreenSolid else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                roster.forEach { p ->
                    ParticipantRow(
                        name = p.playerName,
                        status = p.status,
                        showAction = canEdit,
                        isLoading = isLoading,
                        onToggle = { onToggle(p) }
                    )
                }
            }
        }
    }
}

@Composable
fun ParticipantRow(name: String, status: String, showAction: Boolean, isLoading: Boolean, onToggle: () -> Unit) {
    val isSelected = status == "SELECTED" || status == "LOCKED"
    val isNotApplied = status == "NOT_APPLIED"

    // Ha még nem jelentkezett, halványabban jelenítjük meg a nevét
    val rowAlpha = if (isNotApplied) 0.5f else 1f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = rowAlpha)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                else Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray.copy(alpha = rowAlpha), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name, style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = rowAlpha)
            )
        }
        if (showAction) {
            IconButton(
                onClick = onToggle,
                enabled = !isLoading,
                modifier = Modifier
                    .testTag("toggle_$name")
                    .size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(if (isSelected) Icons.Default.Remove else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ---------------------------------------------------------
// 2. ÁLLAPOT: ELINDULT MECCS (Kezdőcsapat és Statisztikák)
// ---------------------------------------------------------
@Composable
fun ActiveRosterCard(
    teamName: String,
    participants: List<MatchParticipant>,
    individualMatches: List<IndividualMatch>
) {
    // Csak azokat mutatjuk, akik ténylegesen be lettek osztva pozícióba (LOCKED)
    val playingMembers = participants.filter { it.status == "LOCKED" }.sortedBy { it.position ?: 99 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.SportsScore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = teamName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            Column(modifier = Modifier.padding(8.dp)) {
                playingMembers.forEach { player ->

                    // Kiszámoljuk a játékos napi statisztikáját az egyéni meccsekből a NÉV alapján
                    val wins = individualMatches.count { im ->
                        im.status == "finished" && (
                                (im.homePlayerName == player.playerName && im.homeScore > im.guestScore) ||
                                        (im.guestPlayerName == player.playerName && im.guestScore > im.homeScore)
                                )
                    }
                    val losses = individualMatches.count { im ->
                        im.status == "finished" && (
                                (im.homePlayerName == player.playerName && im.homeScore < im.guestScore) ||
                                        (im.guestPlayerName == player.playerName && im.guestScore < im.homeScore)
                                )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${player.position}.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.width(24.dp)
                            )
                            Text(
                                text = player.playerName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Statisztika "Pilula"
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gy: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("$wins", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("V: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("$losses", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ErrorRed)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}