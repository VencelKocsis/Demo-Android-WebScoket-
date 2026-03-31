package hu.bme.aut.android.demo.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.feature.auth.AuthViewModel
import hu.bme.aut.android.demo.feature.racketEditor.Blade
import hu.bme.aut.android.demo.feature.racketEditor.Racket
import hu.bme.aut.android.demo.feature.racketEditor.Rubber
import hu.bme.aut.android.demo.ui.common.InfoDialog
import hu.bme.aut.android.demo.ui.common.PerformanceGraph
import hu.bme.aut.android.demo.ui.common.ProfileStatItem
import hu.bme.aut.android.demo.ui.theme.ErrorRed
import hu.bme.aut.android.demo.ui.theme.ErrorRedBg
import hu.bme.aut.android.demo.ui.theme.ErrorRedLight
import hu.bme.aut.android.demo.ui.theme.ErrorRedSolid
import hu.bme.aut.android.demo.ui.theme.RacketBlack
import hu.bme.aut.android.demo.ui.theme.RacketBlue
import hu.bme.aut.android.demo.ui.theme.RacketYellow
import hu.bme.aut.android.demo.ui.theme.SuccessGreen
import hu.bme.aut.android.demo.ui.theme.SuccessGreenDark
import hu.bme.aut.android.demo.ui.theme.SuccessGreenLight
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
    onLogoutClick: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val backendUser = authState.backendUser

    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(backendUser) {
        profileViewModel.initUser(backendUser)
    }

    LaunchedEffect(profileState.error) {
        profileState.error?.let {
            snackbarHostState.showSnackbar(it)
            profileViewModel.clearError()
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }
    var showGraphInfoDialog by remember { mutableStateOf(false) }
    var showH2HInfoDialog by remember { mutableStateOf(false) }
    var showOverallInfoDialog by remember { mutableStateOf(false) }

    val user = profileState.user
    val teamNames = profileState.userTeamNames

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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. FELHASZNÁLÓI ALAPADATOK (AVATAR ÉS NÉV) ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    if (profileState.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil ikon",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (user != null) "${user.lastName} ${user.firstName}" else "Töltés...",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user?.email ?: stringResource(R.string.no_email),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // Csapat "Pilula"
                val (teamBg, teamTextCol) = if (teamNames.isEmpty()) {
                    if (isDark) ErrorRedSolid.copy(alpha = 0.2f) to ErrorRedLight else ErrorRedBg to ErrorRedSolid
                } else {
                    if (isDark) MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(teamBg)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (teamNames.isEmpty()) stringResource(R.string.no_team) else stringResource(
                            R.string.team_one_var, teamNames.joinToString(", ")),
                        color = teamTextCol,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
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
                        ProfileStatItem(label = stringResource(R.string.match), value = profileState.matchesPlayed.toString(), type = "neutral")
                        ProfileStatItem(label = stringResource(R.string.victory), value = profileState.matchesWon.toString(), type = "success")
                        ProfileStatItem(label = stringResource(R.string.ratio), value = "${profileState.winRate}%", type = "primary")
                    }
                }
            }

            // --- 3. FORMA & SZETT MUTATÓK ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Aktuális Forma
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.from_last_5), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (profileState.recentForm.isEmpty()) Text("-", color = Color.Gray)
                            profileState.recentForm.forEach { isWin ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isWin) SuccessGreen else ErrorRed),
                                    contentAlignment = Alignment.Center
                                ) { Text(
                                    if (isWin) stringResource(R.string.victory_letter)
                                    else stringResource(R.string.lose_letter),
                                    color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
                                ) }
                            }
                        }
                    }
                }

                // Clutch / Söprések
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.set_of_indicators), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${profileState.sweeps}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = SuccessGreen)
                                Text(stringResource(R.string.clutch), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${profileState.decidingSetWins}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Text(stringResource(R.string.final_set), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // --- 4. H2H (Nemezis és Kedvenc) ---
            if (profileState.favoriteOpponent != null || profileState.nemesis != null) {
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
                            Text(stringResource(R.string.againts_each_other), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showH2HInfoDialog = true }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        profileState.favoriteOpponent?.let { (name, wins) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ThumbUp, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.favorite_opponent), style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.weight(1f))
                                Text(stringResource(R.string.two_var_victory, name, wins), fontWeight = FontWeight.Bold)
                            }
                        }

                        if (profileState.favoriteOpponent != null && profileState.nemesis != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        }

                        profileState.nemesis?.let { (name, losses) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.nemesis), style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.weight(1f))
                                Text(stringResource(R.string.two_var_lose, name, losses), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- 5. GRAFIKON (Élő-pont változás) ---
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
                    PerformanceGraph(data = profileState.ratingHistory)
                }
            }

            // --- 6. ÜTŐK (FELSZERELÉS) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                val forehandRubberExample = Rubber("DHS", "Hurricane 3 NEO", "Inverted", 40, 2.1f, "Black")
                val backhandRubberExample = Rubber("Yasaka", "Rakza 7 Soft", "Inverted", 35, 2.0f, "Red")
                val bladeExample = Blade("Butterfly", "Regular", 5, 85f, "OFF", 6.0f, "Timo Boll ALC")
                val racketExample = Racket(
                    blade = "${bladeExample.manufacturer} ${bladeExample.model}",
                    forehandRubber = "${forehandRubberExample.manufacturer} ${forehandRubberExample.model}",
                    backhandRubber = "${backhandRubberExample.manufacturer} ${backhandRubberExample.model}"
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Felszerelés",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.SportsTennis,
                            contentDescription = "Ütő",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()) {
                            Text(text = "Ütőfa", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = racketExample.blade, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ColorCircle(color = stringToColor(forehandRubberExample.color))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Tenyeres", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Text(text = racketExample.forehandRubber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ColorCircle(color = stringToColor(backhandRubberExample.color))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Fonák", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Text(text = racketExample.backhandRubber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* TODO: Új ütő hozzáadása */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ÚJ ÜTŐ HOZZÁADÁSA", fontWeight = FontWeight.Bold)
                    }
                }
            }
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
                        enabled = !profileState.isLoading
                    )
                    OutlinedTextField(
                        value = editFirstName,
                        onValueChange = { editFirstName = it },
                        label = { Text(stringResource(R.string.first_name)) },
                        singleLine = true,
                        enabled = !profileState.isLoading
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        profileViewModel.updateUser(editFirstName, editLastName)
                        showEditDialog = false
                    },
                    enabled = editFirstName.isNotBlank() && editLastName.isNotBlank() && !profileState.isLoading
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }, enabled = !profileState.isLoading) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        )
    }

    // --- INFO DIALÓGUSOK ---

    // 1. Grafikon Info Dialógus
    if (showGraphInfoDialog) {
        InfoDialog(
            title = stringResource(R.string.improvement_graph_dialog),
            text = stringResource(R.string.improvement_graph_dialog_text),
            onDismiss = { showGraphInfoDialog = false }
        )
    }

    // 2. H2H Info Dialógus
    if (showH2HInfoDialog) {
        InfoDialog(
            title = stringResource(R.string.against_each_other_dialog),
            text = stringResource(R.string.against_each_other_dialaog_text),
            onDismiss = { showH2HInfoDialog = false }
        )
    }

    // 3. Összesített Mérleg Info Dialógus
    if (showOverallInfoDialog) {
        InfoDialog(
            title = stringResource(R.string.aggregate_balance_sheet_dialog),
            text = stringResource(R.string.aggregate_balance_sheet_dialog_text),
            onDismiss = { showOverallInfoDialog = false }
        )
    }
}