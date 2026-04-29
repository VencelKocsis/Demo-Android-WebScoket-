package hu.bme.aut.android.demo.feature.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import hu.bme.aut.android.demo.domain.team.model.TeamStats
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.ui.common.GenericFilterDropdown
import hu.bme.aut.android.demo.ui.common.translateSeasonName
import hu.bme.aut.android.demo.ui.theme.Bronze
import hu.bme.aut.android.demo.ui.theme.Gold
import hu.bme.aut.android.demo.ui.theme.Silver

/**
 * A Ranglista (Leaderboard) képernyője.
 * * Csak vizuális logika: Itt már nem történik szűrés vagy statisztika-számítás,
 * azt a [LeaderboardViewModel] már elvégezte. A UI csak rajzol.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.leaderboard)) }) }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            // --- SZŰRŐK (Szezon és Divízió egymás mellett) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Szezon választó
                if (uiState.availableSeasons.isNotEmpty()) {
                    GenericFilterDropdown(
                        label = stringResource(R.string.season),
                        defaultOptionText = stringResource(R.string.all),
                        options = uiState.availableSeasons,
                        selectedOption = uiState.availableSeasons.find { it.first == uiState.selectedSeasonId },
                        optionLabeler = { translateSeasonName(it.second) },
                        // Esemény küldése az új MVI struktúrában
                        onOptionSelected = { viewModel.onEvent(LeaderboardEvent.OnSeasonSelected(it?.first)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 2. Divízió választó
                if (uiState.availableDivisions.isNotEmpty()) {
                    GenericFilterDropdown(
                        label = stringResource(R.string.division_1),
                        defaultOptionText = stringResource(R.string.all),
                        options = uiState.availableDivisions,
                        selectedOption = uiState.selectedDivision,
                        optionLabeler = { it },
                        // Esemény küldése az új MVI struktúrában
                        onOptionSelected = { viewModel.onEvent(LeaderboardEvent.OnDivisionSelected(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- TARTALOM ---
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.filteredTeams.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_teams_in_division), color = Color.Gray)
                }
            } else {
                val top3 = uiState.filteredTeams.take(3)
                val remainingTeams = uiState.filteredTeams.drop(3)

                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    // --- DOBOGÓ SZEKCIÓ ---
                    item {
                        if (top3.isNotEmpty()) {
                            Podium(topTeams = top3, selectedSeasonId = uiState.selectedSeasonId)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // --- TÁBLÁZAT FEJLÉC ---
                    if (remainingTeams.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("#", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(stringResource(R.string.team), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("M", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(stringResource(R.string.v_d_l), modifier = Modifier.width(60.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(stringResource(R.string.point), modifier = Modifier.width(45.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                            HorizontalDivider()
                        }

                        // --- TÁBLÁZAT TESTE (4. Helytől) ---
                        itemsIndexed(remainingTeams) { index, team ->
                            val actualRank = index + 4
                            val isEven = index % 2 == 0
                            val stats = team.getStats(uiState.selectedSeasonId)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$actualRank.", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(team.name, fontWeight = FontWeight.Bold)
                                    Text(team.clubName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }

                                Text("${stats.matchesPlayed}", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                                Text("${stats.wins}-${stats.draws}-${stats.losses}", modifier = Modifier.width(60.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                Text("${stats.points}", modifier = Modifier.width(45.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

// --- DOBOGÓ KOMPONENSEK ---

@Composable
fun Podium(topTeams: List<TeamDetails>, selectedSeasonId: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2. Helyezett (Bal oldalon)
        PodiumItem(
            team = topTeams.getOrNull(1),
            stats = topTeams.getOrNull(1)?.getStats(selectedSeasonId),
            rank = 2,
            color = Silver,
            pillarHeight = 110.dp,
            modifier = Modifier.weight(1f)
        )

        // 1. Helyezett (Középen, a legmagasabb)
        PodiumItem(
            team = topTeams.getOrNull(0),
            stats = topTeams.getOrNull(0)?.getStats(selectedSeasonId),
            rank = 1,
            color = Gold,
            pillarHeight = 150.dp,
            modifier = Modifier.weight(1.1f)
        )

        // 3. Helyezett (Jobb oldalon)
        PodiumItem(
            team = topTeams.getOrNull(2),
            stats = topTeams.getOrNull(2)?.getStats(selectedSeasonId),
            rank = 3,
            color = Bronze,
            pillarHeight = 85.dp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PodiumItem(
    team: TeamDetails?,
    stats: TeamStats?,
    rank: Int,
    color: Color,
    pillarHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        if (team != null && stats != null) {
            Text(
                text = team.name,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "${stats.points} pont",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )
        } else {
            Text("-", fontWeight = FontWeight.Bold, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Maga a pillér
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pillarHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Helyezés $rank",
                    tint = color,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$rank.",
                    fontWeight = FontWeight.Black,
                    color = color,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(color.copy(alpha = 0.4f))
                    .align(Alignment.BottomCenter)
            )
        }
    }
}