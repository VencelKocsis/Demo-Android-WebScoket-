package hu.bme.aut.android.demo.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.ui.common.AggregateStatsCard
import hu.bme.aut.android.demo.ui.common.EquipmentCard
import hu.bme.aut.android.demo.ui.common.ExtraStatsCard
import hu.bme.aut.android.demo.ui.common.InfoDialog
import hu.bme.aut.android.demo.ui.common.ProfileHeader
import hu.bme.aut.android.demo.ui.common.RatingGraphCard
import hu.bme.aut.android.demo.ui.common.RecentFormCard

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

    // Ideális esetben ezeket a DTO-ból kapod, most mock-oljuk a példa kedvéért TODO
    val bladeName = "Butterfly Timo Boll ALC"
    val fhName = "DHS Hurricane 3"
    val fhColor = "Black"
    val bhName = "Yasaka Rakza 7"
    val bhColor = "Red"

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
            ProfileHeader(
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email,
                teamNames = uiState.userTeamNames
            )

            AggregateStatsCard(
                matchesPlayed = uiState.matchesPlayed,
                matchesWon = uiState.matchesWon,
                winRate = uiState.winRate,
                onInfoClick = { showOverallInfoDialog = true }
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                RecentFormCard(recentForm = uiState.recentForm)
                ExtraStatsCard(
                    sweeps = uiState.sweeps,
                    decidingSetWins = uiState.decidingSetWins,
                    flawlessDays = uiState.flawlessDays
                )
            }

            // H2HCard itt nincs, mert ez publikus profil, de a sajátnál hívhatod!

            RatingGraphCard(
                ratingHistory = uiState.ratingHistory,
                onInfoClick = { showGraphInfoDialog = true }
            )

            EquipmentCard(
                bladeName = bladeName,
                fhName = fhName, fhColorName = fhColor,
                bhName = bhName, bhColorName = bhColor,
                onAddEquipmentClick = null // Olvasási mód, nincs gomb!
            )
        }
    }

    // --- INFO DIALÓGUSOK ---
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