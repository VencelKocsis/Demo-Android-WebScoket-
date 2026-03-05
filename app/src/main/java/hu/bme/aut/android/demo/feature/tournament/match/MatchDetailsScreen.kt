package hu.bme.aut.android.demo.feature.tournament.match

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    matchId: Int,
    onNavigateBack: () -> Unit,
    viewModel: MatchDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Frissítés Lifecycle alapon
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(MatchDetailsEvent.LoadMatch)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Snackbar a gombnyomási hibákhoz
    LaunchedEffect(state.actionError) {
        state.actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(MatchDetailsEvent.ClearActionError)
        }
    }

    fun openMap(location: String) {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
        try { context.startActivity(intent) } catch (e: Exception) { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Meccs Részletek") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Vissza")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Finom töltésjelző a képernyő tetején, ha épp adatot küldünk fel (mutating)
            if (state.isMutating) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (state.isLoading && state.match == null) {
                // Csak akkor takarjuk el a képernyőt, ha a legelső letöltés zajlik
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.errorMessage != null && state.match == null) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Hiba: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.onEvent(MatchDetailsEvent.LoadMatch) }) { Text("Újratöltés") }
                }
            } else {
                // A let blokk garantálja, hogy ha van match, akkor ezen belül
                // a 'match' változó 100%-ig nem null.
                state.match?.let { match ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        // --- 1. FEJLÉC (Csapatok és Eredmény) ---
                        item {
                            Text(
                                text = "${match.homeTeamName} vs ${match.guestTeamName}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (match.status == "finished") {
                                Text(
                                    text = "Végeredmény: ${match.homeTeamScore} - ${match.guestTeamScore}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Státusz: ${if (match.status == "scheduled") "Tervezve" else "Folyamatban"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // --- 2. IDŐPONT ÉS HELYSZÍN ---
                        item {
                            Text("Időpont és Helyszín", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            match.matchDate?.let { date ->
                                Text(text = "📅 $date", style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (!match.location.isNullOrEmpty()) {
                                OutlinedButton(onClick = { openMap(match.location) }) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Térkép: ${match.location}")
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // --- 3. EREDMÉNYEK VAGY KERETEK ---
                        if (match.status == "finished") {
                            item {
                                Text("Részletes eredmények", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (match.individualMatches.isEmpty()) {
                                item {
                                    Text("Nincsenek feltöltve részletes eredmények.", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                }
                            } else {
                                items(match.individualMatches.size) { index ->
                                    val game = match.individualMatches[index]
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${game.homePlayerName} vs ${game.guestPlayerName}", style = MaterialTheme.typography.bodyLarge)
                                        Text("${game.homeScore} - ${game.guestScore}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    }
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                }
                            }
                        } else {
                            item {
                                Text("Csapatkeretek", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    // Hazai Oszlop
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Hazai", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        match.participants.filter { it.teamSide == "HOME" }.forEach { p ->
                                            ParticipantRow(
                                                name = p.playerName,
                                                status = p.status,
                                                showAction = state.isHomeCaptain,
                                                isLoading = state.isMutating, // UX: Gomb letiltása hálózat közben
                                                onToggle = { viewModel.onEvent(MatchDetailsEvent.OnToggleParticipantStatus(p.id, p.status)) }
                                            )
                                        }
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(100.dp).background(Color.LightGray))
                                    // Vendég Oszlop
                                    Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                                        Text("Vendég", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        match.participants.filter { it.teamSide == "GUEST" }.forEach { p ->
                                            ParticipantRow(
                                                name = p.playerName,
                                                status = p.status,
                                                showAction = state.isGuestCaptain,
                                                isLoading = state.isMutating, // UX
                                                onToggle = { viewModel.onEvent(MatchDetailsEvent.OnToggleParticipantStatus(p.id, p.status)) }
                                            )
                                        }
                                    }
                                }
                            }

                            // --- 4. JELENTKEZÉS ÉS KAPITÁNYI GOMBOK ---
                            if (state.isUserInvolved) {
                                item {
                                    Spacer(modifier = Modifier.height(32.dp))

                                    // A) JÁTÉKOS NÉZET: Jelentkezés és Visszavonás
                                    if (!state.hasApplied) {
                                        Button(
                                            onClick = { viewModel.onEvent(MatchDetailsEvent.OnApply) },
                                            modifier = Modifier.fillMaxWidth().height(50.dp),
                                            enabled = !state.isMutating
                                        ) {
                                            Text("Jelentkezem a meccsre")
                                        }
                                    } else {
                                        val statusMsg = if (state.myStatus == "SELECTED") "Be vagy válogatva a meccskeretbe!" else "Jelentkezésed rögzítve, várj a kapitány döntésére."
                                        val statusColor = if (state.myStatus == "SELECTED") Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().background(statusColor.copy(alpha = 0.1f), MaterialTheme.shapes.medium).padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = statusMsg, color = statusColor, fontWeight = FontWeight.Bold)
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            TextButton(
                                                onClick = { viewModel.onEvent(MatchDetailsEvent.OnWithdrawApplication) },
                                                enabled = !state.isMutating
                                            ) {
                                                Text("Jelentkezés visszavonása", color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // B) KAPITÁNYI NÉZET: Véglegesítés
                                    val isCaptainOfCurrentMatch = state.isHomeCaptain || state.isGuestCaptain
                                    val selectedCount = if (state.isHomeCaptain) state.homeSelectedCount else state.guestSelectedCount

                                    if (isCaptainOfCurrentMatch) {
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(16.dp))

                                        if (selectedCount >= 4) {
                                            Button(
                                                onClick = { viewModel.onEvent(MatchDetailsEvent.OnFinalizeRoster) },
                                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                                enabled = !state.isMutating,
                                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                                            ) {
                                                Text("Keret véglegesítése és Indítás ($selectedCount fő)")
                                            }
                                        } else {
                                            Text(
                                                text = "A meccs elindításához legalább 4 játékost be kell tenned a keretbe! (Jelenleg: $selectedCount/4)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(top = 8.dp)
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
    }
}

@Composable
fun ParticipantRow(
    name: String,
    status: String,
    showAction: Boolean,
    isLoading: Boolean, // ÚJ: a gomb tiltásához
    onToggle: () -> Unit
) {
    val isSelected = status == "SELECTED"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.width(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
        }

        if (showAction) {
            TextButton(
                onClick = onToggle,
                contentPadding = PaddingValues(0.dp),
                enabled = !isLoading // Gomb inaktiválása betöltés közben
            ) {
                Text(
                    text = if (isSelected) "Kivesz" else "Betesz",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLoading) Color.Gray else if (isSelected) Color.Red else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}