package hu.bme.aut.android.demo.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import hu.bme.aut.android.demo.ui.common.StatItem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.ui.common.InfoDialog
import hu.bme.aut.android.demo.ui.common.PerformanceGraph
import hu.bme.aut.android.demo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    playerId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // Állapot kinyerése
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Amikor a képernyő betölt, azonnal lekérjük az adatokat a playerId alapján
    LaunchedEffect(playerId) {
        viewModel.loadPublicProfile(playerId)
    }

    // --- ÁLLAPOTOK A DIALÓGUSOKHOZ ---
    var showGraphInfoDialog by remember { mutableStateOf(false) }
    var showH2HInfoDialog by remember { mutableStateOf(false) }
    var showOverallInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_statistics)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Vissza")
                    }
                }
            )
        }
    ) { padding ->
        // Ha töltődik az adat, mutassunk egy kört
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // Ha hiba történt
        if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }

        // A letöltött játékos
        val user = uiState.user ?: return@Scaffold

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. JÁTÉKOS ALAPADATOK ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profil ikon", modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${user.lastName} ${user.firstName}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = user.email ?: "Nincs email", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }

                // Csapat "Pilula" (Csak az első csapatot írjuk ki, vagy egyet se ha nincs)
                if (uiState.userTeamNames.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = uiState.userTeamNames.first(),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- 2. ALAP STATISZTIKA ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.aggregate_balance_sheet), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showOverallInfoDialog = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatItem(label = stringResource(R.string.match), value = uiState.matchesPlayed.toString(), type = "neutral", circleSize = 56.dp, isLargeText = true)
                        StatItem(label = stringResource(R.string.victory), value = uiState.matchesWon.toString(), type = "success", circleSize = 56.dp, isLargeText = true)
                        StatItem(label = stringResource(R.string.ratio), value = "${uiState.winRate}%", type = "primary", circleSize = 56.dp, isLargeText = true)
                    }
                }
            }

            // --- 3. FORMA ÉS EXTRA MUTATÓK ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Aktuális Forma Kártya
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.from_last_5), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (uiState.recentForm.isEmpty()) {
                                Text("-", color = Color.Gray)
                            } else {
                                uiState.recentForm.forEach { isWin ->
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isWin) SuccessGreenSolid else ErrorRedSolid),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (isWin) stringResource(R.string.victory_letter) else stringResource(R.string.lose_letter),
                                            color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Extra Mutatók (Clutch / Söprések / Flawless) Kártya
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Extra Mutatók", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 1. Söprés (3-0)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${uiState.sweeps}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = SuccessGreenSolid)
                                Text("3-0", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }

                            // 2. Döntő szett (3-2)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${uiState.decidingSetWins}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Text("3-2", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }

                            // 3. Hibátlan bajnoki (4/4)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${uiState.flawlessDays}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = WarningOrangeSolid)
                                Text("4/4", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // --- 4. GRAFIKON ---
            if (uiState.ratingHistory.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.improvement_graph), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showGraphInfoDialog = true }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        PerformanceGraph(data = uiState.ratingHistory)
                    }
                }
            }

            // --- 5. ÜTŐK (Olvasási módban, gomb nélkül) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Felszerelés", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Icon(imageVector = Icons.Default.SportsTennis, contentDescription = "Ütő", tint = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Text(text = "Ütőfa", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "Butterfly Timo Boll ALC", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) // TODO: backend adat

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Tenyeres", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(text = "DHS Hurricane 3", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) // TODO: backend adat
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Fonák", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(text = "Yasaka Rakza 7", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) // TODO: backend adat
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- INFO DIALÓGUSOK ---
    // (A dialógusok kódja marad ugyanaz, amit megírtál)
    if (showGraphInfoDialog) {
        InfoDialog(
            title = stringResource(R.string.improvement_graph_dialog),
            text = stringResource(R.string.improvement_graph_dialog_text),
            onDismiss = { showGraphInfoDialog = false }
        )
    }

    if (showH2HInfoDialog) {
        InfoDialog(
            title = stringResource(R.string.against_each_other_dialog),
            text = stringResource(R.string.against_each_other_dialaog_text),
            onDismiss = { showH2HInfoDialog = false }
        )
    }

    if (showOverallInfoDialog) {
        InfoDialog(
            title = stringResource(R.string.aggregate_balance_sheet_dialog),
            text = stringResource(R.string.aggregate_balance_sheet_dialog_text),
            onDismiss = { showOverallInfoDialog = false }
        )
    }
}