package hu.bme.aut.android.demo.feature.team

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.domain.team.model.Team
import hu.bme.aut.android.demo.domain.team.model.TeamDetails
import kotlin.collections.isNotEmpty

// --- Adatmodellek ---
data class MatchResult(
    val opponent: String,
    val date: String,
    val homeScore: Int,
    val awayScore: Int,
    val isWin: Boolean
)

data class TeamScreenState(
    val isLoading: Boolean = false,
    val teamList: List<Team> = emptyList(),
    val selectedTeam: TeamDetails? = null,
    val isCurrentUserCaptain: Boolean = false,
    val errorMessage: String? = null
)

sealed class TeamScreenEvent {
    object LoadInitialData : TeamScreenEvent()
    data class OnTeamSelected(val teamId: Int) : TeamScreenEvent()
}

// --- 1. Állapotfüggő (Stateful) Composable ---
@Composable
fun TeamScreen(
    viewModel: TeamViewModel = hiltViewModel(),
    onNavigateToEditor: (Int) -> Unit = {}
) {
    // Állapot kinyerése a ViewModel-ből
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Ha a képernyő újra fókuszba kerül (ON_RESUME), frissítjük az adatokat!
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(TeamScreenEvent.LoadInitialData)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Továbbítjuk az állapotfüggetlen UI-nak
    TeamScreenContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToEditor = onNavigateToEditor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreenContent(
    state: TeamScreenState,
    onEvent: (TeamScreenEvent) -> Unit,
    onNavigateToEditor: (Int) -> Unit,
    playedMatches: List<MatchResult> = listOf(
        MatchResult("Asztal Királyai", "2025-09-01", 9, 7, true),
        MatchResult("PingPong Heroes", "2025-09-08", 8, 8, false),
        MatchResult("Ping Pong Kings", "2025-09-15", 12, 4, true)
    )
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Klubház") }) },
        floatingActionButton = {
            if (state.isCurrentUserCaptain && state.selectedTeam != null) {
                FloatingActionButton(
                    onClick = { onNavigateToEditor(state.selectedTeam.id) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Csapat szerkesztése")
                }
            }
        }
    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { onEvent(TeamScreenEvent.LoadInitialData) },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (state.errorMessage != null && state.teamList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // --- 1. CSAPATVÁLASZTÓ (DROPDOWN) ---
                    item {
                        if (state.teamList.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = state.selectedTeam?.name ?: "Válassz csapatot",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Kiválasztott Csapat") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    state.teamList.forEach { team ->
                                        DropdownMenuItem(
                                            text = { Text(team.name, fontWeight = FontWeight.Medium) },
                                            onClick = {
                                                onEvent(TeamScreenEvent.OnTeamSelected(team.id))
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // --- 2. KIVÁLASZTOTT CSAPAT ADATAI ---
                    state.selectedTeam?.let { team ->
                        item {
                            Text(team.clubName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            if (!team.division.isNullOrEmpty()) {
                                Text(
                                    text = "Divízió: ${team.division}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                            // SZÍNES STATISZTIKAI KÁRTYA
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatItem(label = "Meccs", value = team.matchesPlayed.toString(), type = "neutral")
                                    StatItem(label = "Gy", value = team.wins.toString(), type = "success")
                                    StatItem(label = "D", value = team.draws.toString(), type = "warning")
                                    StatItem(label = "V", value = team.losses.toString(), type = "error")
                                    StatItem(label = "Pont", value = team.points.toString(), type = "primary")
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                            Text("Csapatkeret", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // JÁTÉKOSOK LISTÁJA
                        items(team.members) { member ->
                            PlayerCardRow(name = member.name, isCaptain = member.isCaptain)
                        }

                        // LEGUTÓBBI MECCSEK
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text("Legutóbbi meccsek", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(playedMatches) { match ->
                            MatchResultRow(match)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, type: String) {
    val isDark = isSystemInDarkTheme()

    // Színek: Világos módban teli színek, fehér szöveg. Sötétben áttetsző pasztell.
    val (bgColor, textColor) = when (type) {
        "success" -> if(isDark) Color(0xFF2E7D32).copy(0.25f) to Color(0xFF81C784) else Color(0xFF388E3C) to Color.White
        "error" -> if(isDark) Color(0xFFD32F2F).copy(0.25f) to Color(0xFFFF8A80) else Color(0xFFD32F2F) to Color.White
        "warning" -> if(isDark) Color(0xFFF57F17).copy(0.25f) to Color(0xFFFFD54F) else Color(0xFFF57C00) to Color.White
        "primary" -> if(isDark) MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        else -> if(isDark) Color.Gray.copy(0.2f) to Color.LightGray else Color(0xFF757575) to Color.White
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MatchResultRow(match: MatchResult) {
    val isDark = isSystemInDarkTheme()

    val (bgColor, textColor) = if (match.isWin) {
        if(isDark) Color(0xFF2E7D32).copy(0.25f) to Color(0xFF81C784) else Color(0xFF388E3C) to Color.White
    } else {
        if(isDark) Color(0xFFD32F2F).copy(0.25f) to Color(0xFFFF8A80) else Color(0xFFD32F2F) to Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = match.opponent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "📅 ${match.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${match.homeScore} - ${match.awayScore}",
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun PlayerCardRow(name: String, isCaptain: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profil avatar helyettkeztető
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

            if (isCaptain) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFC107).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("KAPITÁNY", color = Color(0xFFF57F17), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}