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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
    teamName: String = "Rapid Pong",
    members: List<TeamMember> = listOf(
        TeamMember("Kovács Péter", true, PlayerStats(10, 2)),
        TeamMember("Nagy Anna", false, PlayerStats(6, 4)),
        TeamMember("Szabó Dániel", false, PlayerStats(4, 6)),
        TeamMember("Kiss Márk", false, PlayerStats(2, 8))
    ),
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
    useMembersStats: Boolean = false // <- Itt lehet váltani a számolás módját
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Csapat") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(teamName, style = MaterialTheme.typography.headlineSmall)

            // Csapattagok kártya
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Csapattagok", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(members) { member ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (member.isCaptain) {
                                        Icon(Icons.Default.Star, contentDescription = "Kapitány", tint = Color(0xFFFFD700))
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = "Játékos")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(member.name)
                                }
                                Text(
                                    "${member.stats.wins}W - ${member.stats.losses}L (${member.stats.winPercentage}%)",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Heti eredmények kártya
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("BEAC Heti eredmények", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    weeklyResults.forEach { result ->
                        Text(result, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Lejátszott meccsek kártya
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Lejátszott meccsek", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    playedMatches.forEach { match ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                        Divider()
                    }
                }
            }

            // Grafikon kártya
            val (totalWins, totalLosses) = if (useMembersStats) {
                val w = members.sumOf { it.stats.wins }
                val l = members.sumOf { it.stats.losses }
                w to l
            } else {
                val w = playedMatches.count { it.isWin }
                val l = playedMatches.size - w
                w to l
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (useMembersStats) "Összesített játékos statisztika"
                        else "Csapat teljesítmény",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val total = (totalWins + totalLosses).coerceAtLeast(1)
                            val sweepAngleWin = (totalWins.toFloat() / total) * 360f

                            drawArc(
                                color = Color(0xFF4CAF50),
                                startAngle = -90f,
                                sweepAngle = sweepAngleWin,
                                useCenter = true,
                                size = Size(size.width, size.height)
                            )
                            drawArc(
                                color = Color(0xFFD32F2F),
                                startAngle = -90f + sweepAngleWin,
                                sweepAngle = 360f - sweepAngleWin,
                                useCenter = true,
                                size = Size(size.width, size.height)
                            )
                        }
                        Text("${totalWins}W / ${totalLosses}L")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamScreenPreview() {
    TeamScreen(useMembersStats = false) // Csapat eredmény grafikon
}

@Preview(showBackground = true)
@Composable
fun TeamScreenMembersStatsPreview() {
    TeamScreen(useMembersStats = true) // Játékos statisztikákból számolt grafikon
}
