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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import hu.bme.aut.android.demo.ui.theme.Bronze
import hu.bme.aut.android.demo.ui.theme.Gold
import hu.bme.aut.android.demo.ui.theme.Silver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var divisionExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ranglista") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // --- SZŰRŐ ---
            if (uiState.availableDivisions.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = divisionExpanded, onExpandedChange = { divisionExpanded = !divisionExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedDivision ?: "Összes",
                            onValueChange = {}, readOnly = true, label = { Text("Divízió") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = divisionExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(), colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(expanded = divisionExpanded, onDismissRequest = { divisionExpanded = false }) {
                            DropdownMenuItem(text = { Text("Összes") }, onClick = { viewModel.selectDivision(null); divisionExpanded = false })
                            uiState.availableDivisions.forEach { div ->
                                DropdownMenuItem(text = { Text(div) }, onClick = { viewModel.selectDivision(div); divisionExpanded = false })
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.filteredTeams.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nincsenek csapatok ebben a divízióban.", color = Color.Gray)
                }
            } else {
                // Szétválasztjuk az első hármat és a többit
                val top3 = uiState.filteredTeams.take(3)
                val remainingTeams = uiState.filteredTeams.drop(3)

                // Csak akkor kell görgethető LazyColumn, ha lejjebb a táblázatnak is görgethetőnek kell lennie
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    // --- DOBOGÓ SZEKCIÓ ---
                    item {
                        if (top3.isNotEmpty()) {
                            Podium(topTeams = top3)
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
                                Text("Csapat", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("M", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Gy-D-V", modifier = Modifier.width(60.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Pont", modifier = Modifier.width(45.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                            HorizontalDivider()
                        }

                        // --- TÁBLÁZAT TESTE (4. Helytől) ---
                        itemsIndexed(remainingTeams) { index, team ->
                            val actualRank = index + 4 // Mivel a 4. helytől kezdődik
                            val isEven = index % 2 == 0

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

                                Text("${team.matchesPlayed}", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                                Text("${team.wins}-${team.draws}-${team.losses}", modifier = Modifier.width(60.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                Text("${team.points}", modifier = Modifier.width(45.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
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
fun Podium(topTeams: List<TeamDetails>) {
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
            rank = 2,
            color = Silver,
            pillarHeight = 110.dp,
            modifier = Modifier.weight(1f)
        )

        // 1. Helyezett (Középen, a legmagasabb)
        PodiumItem(
            team = topTeams.getOrNull(0),
            rank = 1,
            color = Gold,
            pillarHeight = 150.dp,
            modifier = Modifier.weight(1.1f)
        )

        // 3. Helyezett (Jobb oldalon)
        PodiumItem(
            team = topTeams.getOrNull(2),
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
        if (team != null) {
            Text(
                text = team.name,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${team.points} pont",
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