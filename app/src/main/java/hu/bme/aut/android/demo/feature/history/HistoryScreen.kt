package hu.bme.aut.android.demo.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// ----------------------- Adatmodellek ------------------------

data class SetResult(
    val homePoints: Int,
    val awayPoints: Int
)

data class MatchResultDetail(
    val homePlayer: String,
    val awayPlayer: String,
    val sets: List<SetResult>
)

data class RoundResult(
    val roundName: String,
    val location: String,
    val date: String,
    val matches: List<MatchResultDetail>
)

data class SeasonResult(
    val year: Int,
    val season: String,
    val winner: String,
    val rounds: List<RoundResult>
)

// ----------------------- Fő képernyő ------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    results: List<SeasonResult>
) {
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val years = results.map { it.year }.distinct().sortedDescending()

    val filteredResults = results.filter { season ->
        (selectedYear == null || season.year == selectedYear) &&
                (searchQuery.isBlank() || season.winner.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("History") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Korábbi bajnokságok", style = MaterialTheme.typography.headlineSmall)

            // Év választó
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {}
            ) {
                OutlinedTextField(
                    value = selectedYear?.toString() ?: "Összes év",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Év választása") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Csapat kereső
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Csapat keresése") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Keresés") },
                modifier = Modifier.fillMaxWidth()
            )

            // Szűrt szezonok listája
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredResults.size) { index ->
                    HistoryCard(season = filteredResults[index])
                }
            }
        }
    }
}

// ----------------------- UI komponensek ------------------------

@Composable
fun HistoryCard(season: SeasonResult) {
    // Alapértelmezetten lenyitva
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${season.year} ${season.season} szezon")
                Text("🏆 ${season.winner}")
            }

            Spacer(modifier = Modifier.height(8.dp))
            season.rounds.forEach { round ->
                RoundCard(round)
            }
        }
    }
}

@Composable
fun RoundCard(round: RoundResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(round.roundName)
                Text(round.date, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))
            round.matches.forEach { match ->
                MatchCard(match)
            }
        }
    }
}

@Composable
fun MatchCard(match: MatchResultDetail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${match.homePlayer} - ${match.awayPlayer}")
                val homeWins = match.sets.count { it.homePoints > it.awayPoints }
                val awayWins = match.sets.count { it.awayPoints > it.homePoints }
                Text("$homeWins - $awayWins")
            }

            Spacer(modifier = Modifier.height(4.dp))
            match.sets.forEachIndexed { index, set ->
                Text(
                    "Szett ${index + 1}: ${set.homePoints} - ${set.awayPoints}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ----------------------- Preview ------------------------

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    HistoryScreen(results = demoData)
}

val demoData = listOf(
    SeasonResult(
        2025, "Tavaszi", "Rapid Pong",
        rounds = listOf(
            RoundResult(
                roundName = "Rapid Pong - PingPong Heroes",
                location = "Sportcsarnok",
                date = "2025-03-10",
                matches = listOf(
                    MatchResultDetail(
                        "Kovács Péter", "Nagy Anna",
                        sets = listOf(
                            SetResult(11, 7),
                            SetResult(8, 11),
                            SetResult(11, 9)
                        )
                    ),
                    MatchResultDetail(
                        "Szabó Dániel", "Kiss Márk",
                        sets = listOf(
                            SetResult(11, 6),
                            SetResult(11, 4),
                            SetResult(11, 8)
                        )
                    )
                )
            )
        )
    )
)