package hu.bme.aut.android.demo.feature.team

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

data class PlayerStats(val wins: Int, val losses: Int) {
    val winPercentage: Int
        get() = if (wins + losses == 0) 0 else (wins * 100) / (wins + losses)
}

data class TeamMember(
    val name: String,
    val isCaptain: Boolean = false,
    val stats: PlayerStats = PlayerStats(0, 0)
)

data class MatchResult(
    val opponent: String,
    val date: String,
    val homeScore: Int,
    val awayScore: Int,
    val isWin: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    viewModel: TeamViewModel = hiltViewModel(),
    weeklyResults: List<String> = listOf(
        "Rapid Pong 7 - 5 Asztal Királyai",
        "PingPong Heroes 6 - 6 Rapid Pong",
        "Rapid Pong 8 - 4 Ping Pong Kings"
    ),
    playedMatches: List<MatchResult> = listOf(
        MatchResult("Asztal Királyai", "2025-09-01", 9, 7, true),
        MatchResult("PingPong Heroes", "2025-09-08", 8, 8, false),
        MatchResult("Ping Pong Kings", "2025-09-15", 12, 4, true)
    ),
    useMembersStats: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Csapat") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                // 1. ÁLLAPOT: Töltés
                uiState.isLoading -> {
                    Spacer(modifier = Modifier.height(100.dp))
                    CircularProgressIndicator()
                    Text("Adatok betöltése...")
                }

                // 2. ÁLLAPOT: Hiba
                uiState.error != null -> {
                    Spacer(modifier = Modifier.height(100.dp))
                    Text(
                        text = "Hiba történt:\n${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // 3. ÁLLAPOT: Sikeres betöltés (Adatok mutatása)
                else -> {
                    // Címsor: A csapat neve a Backendről
                    Text(
                        text = uiState.teamName,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    // 1. KÁRTYA: Játékosok (Backendről)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Csapattagok", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Mivel Scrollable Column-ban vagyunk, nem használhatunk LazyColumnt fix magasság nélkül.
                            // Helyette forEach-et használunk:
                            uiState.members.forEach { member ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = "Játékos")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = member.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (member.isCaptain) androidx.compose.ui.text.font.FontWeight.Bold else null
                                        )
                                        if (member.isCaptain) {
                                            Text(" (C)", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }

                    // 2. KÁRTYA: Heti eredmények (Mock)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Heti eredmények", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            weeklyResults.forEach { result ->
                                Text(result, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // 3. KÁRTYA: Lejátszott meccsek (Mock)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Lejátszott meccsek", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            playedMatches.forEach { match ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(match.opponent, style = MaterialTheme.typography.bodyLarge)
                                        Text(match.date, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(
                                        "${match.homeScore} - ${match.awayScore}",
                                        color = if (match.isWin) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                HorizontalDivider()
                            }
                        }
                    }

                    // 4. KÁRTYA: Grafikon
                    val (totalWins, totalLosses) = if (useMembersStats) {
                        val w = uiState.members.sumOf { it.stats.wins }
                        val l = uiState.members.sumOf { it.stats.losses }
                        w to l
                    } else {
                        val w = playedMatches.count { it.isWin }
                        val l = playedMatches.size - w
                        w to l
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (useMembersStats) "Összesített játékos statisztika" else "Csapat teljesítmény",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val total = (totalWins + totalLosses).coerceAtLeast(1)
                                    val sweepAngleWin = (totalWins.toFloat() / total) * 360f

                                    drawArc(color = Color(0xFF4CAF50), startAngle = -90f, sweepAngle = sweepAngleWin, useCenter = true, size = Size(size.width, size.height))
                                    drawArc(color = Color(0xFFD32F2F), startAngle = -90f + sweepAngleWin, sweepAngle = 360f - sweepAngleWin, useCenter = true, size = Size(size.width, size.height))
                                }
                                Text("${totalWins}W / ${totalLosses}L")
                            }
                        }
                    }

                    // Extra távolság az alsó menü miatt
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}