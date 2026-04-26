package hu.bme.aut.android.demo.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.feature.auth.AuthViewModel
import hu.bme.aut.android.demo.ui.common.AggregateStatsCard
import hu.bme.aut.android.demo.ui.common.EquipmentCard
import hu.bme.aut.android.demo.ui.common.ExtraStatsCard
import hu.bme.aut.android.demo.ui.common.H2HCard
import hu.bme.aut.android.demo.ui.common.InfoDialog
import hu.bme.aut.android.demo.ui.common.ProfileHeader
import hu.bme.aut.android.demo.ui.common.RacketUiModel
import hu.bme.aut.android.demo.ui.common.RatingGraphCard
import hu.bme.aut.android.demo.ui.common.RecentFormCard
import hu.bme.aut.android.demo.ui.theme.ErrorRedSolid
import hu.bme.aut.android.demo.ui.theme.RacketBlack
import hu.bme.aut.android.demo.ui.theme.RacketBlue
import hu.bme.aut.android.demo.ui.theme.RacketYellow
import hu.bme.aut.android.demo.ui.theme.SuccessGreenSolid
import hu.bme.aut.android.demo.util.LanguageSelector

// --- SEGÉDFÜGGVÉNYEK ÉS GRAFIKON ---
@Composable
fun ColorCircle(color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .size(16.dp)
        .background(color, CircleShape))
}

fun stringToColor(colorName: String): Color {
    return when (colorName.lowercase()) {
        "red" -> ErrorRedSolid
        "black" -> RacketBlack
        "blue" -> RacketBlue
        "green" -> SuccessGreenSolid
        "yellow" -> RacketYellow
        else -> Color.Gray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit,
    onNavigateToRacketEditor: () -> Unit,
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val backendUser = authState.backendUser
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(backendUser) {
        profileViewModel.initUser(backendUser)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            profileViewModel.clearError()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                profileViewModel.refreshProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }
    var showGraphInfoDialog by remember { mutableStateOf(false) }
    var showH2HInfoDialog by remember { mutableStateOf(false) }
    var showOverallInfoDialog by remember { mutableStateOf(false) }

    val user = uiState.user
    val teamNames = uiState.userTeamNames

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_profile)) },
                actions = {
                    LanguageSelector()

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit_profile)) },
                            onClick = {
                                showMenu = false
                                editFirstName = user?.firstName ?: ""
                                editLastName = user?.lastName ?: ""
                                showEditDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onLogoutClick()
                            }
                        )
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

        // --- FELSZERELÉS MAPPELÉSE ---
        val equipmentList = user.equipment.map { racket ->
            RacketUiModel(
                bladeName = "${racket.bladeManufacturer} ${racket.bladeModel}",
                fhName = "${racket.fhRubberManufacturer} ${racket.fhRubberModel}",
                fhColorName = racket.fhRubberColor,
                bhName = "${racket.bhRubberManufacturer} ${racket.bhRubberModel}",
                bhColorName = racket.bhRubberColor
            )
        }

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

            // TODO stats to current season

            H2HCard(
                favoriteOpponent = uiState.favoriteOpponent,
                nemesis = uiState.nemesis,
                onInfoClick = { showH2HInfoDialog = true }
            )

            RatingGraphCard(
                ratingHistory = uiState.ratingHistory,
                onInfoClick = { showGraphInfoDialog = true }
            )

            EquipmentCard(
                rackets = equipmentList,
                onAddEquipmentClick = onNavigateToRacketEditor
            )
        }
    }

    // --- SZERKESZTŐ DIALOG ---
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.edit_profile), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = editLastName,
                        onValueChange = { editLastName = it },
                        label = { Text(stringResource(R.string.last_name)) },
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )
                    OutlinedTextField(
                        value = editFirstName,
                        onValueChange = { editFirstName = it },
                        label = { Text(stringResource(R.string.first_name)) },
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        profileViewModel.updateUser(editFirstName, editLastName)
                        showEditDialog = false
                    },
                    enabled = editFirstName.isNotBlank() && editLastName.isNotBlank() && !uiState.isLoading
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }, enabled = !uiState.isLoading) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        )
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