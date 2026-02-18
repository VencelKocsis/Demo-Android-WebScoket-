package hu.bme.aut.android.demo.feature.tournament

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class Round(
    val homeTeam: String,
    val awayTeam: String,
    val location: String,
    val date: String,
    val status: RoundStatus,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val userParticipates: Boolean = false,
    val matchDetails: List<MatchDetail> = emptyList() // részletes meccsek
)

data class MatchDetail(
    val homePlayer: String,
    val awayPlayer: String,
    val homePoints: Int,
    val awayPoints: Int
)

enum class RoundStatus { OPEN, IN_PROGRESS, CLOSED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentScreen(
    seasonName: String = "2025 tavaszi szezon",
    rounds: List<Round> = listOf(
        Round(
            "Rapid Pong", "Asztal Királyai",
            "Sportcsarnok", "2025-09-20 18:00",
            RoundStatus.OPEN
        ),
        Round(
            "Rapid Pong", "PingPong Heroes",
            "Csarnok 2", "2025-09-27 18:00",
            RoundStatus.IN_PROGRESS, userParticipates = true
        ),
        Round(
            "Rapid Pong", "Ping Pong Kings",
            "Csarnok 3", "2025-08-15 18:00",
            RoundStatus.CLOSED,
            homeScore = 9, awayScore = 7,
            matchDetails = listOf(
                MatchDetail("Hazai 1", "Vendég 1", 3, 1),
                MatchDetail("Hazai 2", "Vendég 2", 2, 3),
                MatchDetail("Hazai 3", "Vendég 3", 4, 0)
            )
        )
    )
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Bajnokság - $seasonName") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rounds) { round ->
                val statusColor = when (round.status) {
                    RoundStatus.OPEN -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                    RoundStatus.IN_PROGRESS -> Color(0xFFFFC107).copy(alpha = 0.3f)
                    RoundStatus.CLOSED -> Color(0xFF9E9E9E).copy(alpha = 0.5f)
                }

                var expanded by remember { mutableStateOf(true) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(containerColor = statusColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = if (round.status == RoundStatus.CLOSED) {
                                Modifier.clickable { expanded = !expanded }
                            } else Modifier
                        ) {
                            Text(
                                "${round.homeTeam} - ${round.awayTeam}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (round.status == RoundStatus.CLOSED) {
                                Icon(
                                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle Details"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Helyszín: ${round.location}")
                        Text("Dátum: ${round.date}")
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Csak OPEN státuszban jelenik meg a Térkép gomb
                            if (round.status == RoundStatus.OPEN) {
                                OutlinedButton(onClick = { /* TODO: Google Maps megnyitása */ }) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Térkép")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Térkép")
                                }
                            }

                            when (round.status) {
                                RoundStatus.OPEN -> Button(onClick = { /* TODO: Jelentkezés */ }) {
                                    Text("Jelentkezés")
                                }
                                RoundStatus.IN_PROGRESS -> if (round.userParticipates) {
                                    Button(onClick = { /* TODO: Meccs vezetése */ }) {
                                        Text("Meccs kezelése")
                                    }
                                } else {
                                    Text("Folyamatban", style = MaterialTheme.typography.bodyMedium)
                                }
                                RoundStatus.CLOSED -> Text(
                                    "Eredmények: ${round.homeTeam} ${round.homeScore} - ${round.awayScore} ${round.awayTeam}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Részletek lenyitva
                        if (round.status == RoundStatus.CLOSED && expanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                round.matchDetails.forEach { match ->
                                    Text(
                                        "${match.homePlayer} - ${match.awayPlayer} : ${match.homePoints} - ${match.awayPoints}",
                                        style = MaterialTheme.typography.bodyMedium
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

@Preview(showBackground = true)
@Composable
fun TournamentScreenPreview() {
    TournamentScreen()
}
