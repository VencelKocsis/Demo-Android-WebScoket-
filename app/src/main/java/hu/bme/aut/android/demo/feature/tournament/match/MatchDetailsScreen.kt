package hu.bme.aut.android.demo.feature.tournament.match

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.ui.common.MatchDateRow
import hu.bme.aut.android.demo.ui.common.MatchLocationButton
import hu.bme.aut.android.demo.ui.common.MatchStatusChip

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    matchId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToLiveMatch: () -> Unit = {},
    viewModel: MatchDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Frissítés Lifecycle alapon (Pl. ha visszajövünk a térképről)
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Visszalépés")
                    }
                }
            )
        }
    ) { paddingValues ->

        // --- PULL TO REFRESH DOBOZ A TELJES KÉPERNYŐRE ---
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.onEvent(MatchDetailsEvent.LoadMatch) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Box(modifier = Modifier.fillMaxSize()) {

                // Finom töltésjelző a képernyő tetején, ha épp adatot küldünk fel (mutating)
                if (state.isMutating) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (state.errorMessage != null && state.match == null) {
                    // Hiba képernyő, ha semmi sem töltött be
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Hiba: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.onEvent(MatchDetailsEvent.LoadMatch) }) {
                            Text("Újratöltés")
                        }
                    }
                } else {
                    // A let blokk garantálja, hogy ha van match, akkor ezen belül 100%-ig nem null.
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

                                // Ha vége van, kiírjuk nagyban az eredményt
                                if (match.status == "finished") {
                                    Text(
                                        text = "Végeredmény: ${match.homeTeamScore} - ${match.guestTeamScore}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                MatchStatusChip(status = match.status)

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // --- 2. IDŐPONT ÉS HELYSZÍN ---
                            item {
                                Text("Időpont és Helyszín", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                MatchDateRow(date = match.matchDate)

                                Spacer(modifier = Modifier.height(8.dp))

                                MatchLocationButton(location = match.location)

                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // --- 3. CSAPATKERETEK ---
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
                                                // Csak akkor lehessen szerkeszteni a keretet, ha még "scheduled" a meccs!
                                                showAction = state.isHomeCaptain && match.status == "scheduled",
                                                isLoading = state.isMutating,
                                                onToggle = { viewModel.onEvent(MatchDetailsEvent.OnToggleParticipantStatus(p.id, p.status)) }
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(100.dp)
                                            .background(Color.LightGray)
                                    )

                                    // Vendég Oszlop
                                    Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                                        Text("Vendég", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        match.participants.filter { it.teamSide == "GUEST" }.forEach { p ->
                                            ParticipantRow(
                                                name = p.playerName,
                                                status = p.status,
                                                // Csak akkor lehessen szerkeszteni a keretet, ha még "scheduled" a meccs!
                                                showAction = state.isGuestCaptain && match.status == "scheduled",
                                                isLoading = state.isMutating,
                                                onToggle = { viewModel.onEvent(MatchDetailsEvent.OnToggleParticipantStatus(p.id, p.status)) }
                                            )
                                        }
                                    }
                                }
                            }

                            // --- 4. AKCIÓ GOMBOK ---
                            if (state.isUserInvolved) {
                                item {
                                    Spacer(modifier = Modifier.height(32.dp))

                                    // HA A MECCS MÁR ELINDULT -> Bárki beléphet az Élő Mérkőzésbe
                                    if (match.status == "in_progress") {
                                        Button(
                                            onClick = onNavigateToLiveMatch,
                                            modifier = Modifier.fillMaxWidth().height(50.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                                        ) {
                                            Text("TOVÁBB AZ ÉLŐ MÉRKŐZÉSRE 🏓")
                                        }
                                        Text(
                                            text = "A mérkőzés már elindult. Lépj be a sorrend megadásához vagy az eredmények követéséhez!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                                        )
                                    }
                                    // HA A MECCS MÉG TERVEZETT -> Jöhet a jelentkezés és a kerethirdetés
                                    else if (match.status == "scheduled") {

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
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(statusColor.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
                                                        .padding(16.dp),
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

                                        // B) KAPITÁNYI NÉZET: Véglegesítés és Indítás
                                        val isCaptainOfCurrentMatch = state.isHomeCaptain || state.isGuestCaptain
                                        if (isCaptainOfCurrentMatch) {
                                            HorizontalDivider()
                                            Spacer(modifier = Modifier.height(16.dp))

                                            val isHomeReady = state.homeSelectedCount >= 4
                                            val isGuestReady = state.guestSelectedCount >= 4

                                            if (isHomeReady && isGuestReady) {
                                                Button(
                                                    onClick = {
                                                        viewModel.onEvent(MatchDetailsEvent.OnFinalizeRoster)
                                                        onNavigateToLiveMatch()
                                                    },
                                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                                    enabled = !state.isMutating,
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                                                ) {
                                                    Text("MECCS ELINDÍTÁSA 🏁")
                                                }
                                                Text(
                                                    text = "Mindkét csapat kerete megvan, a mérkőzés indítható!",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF4CAF50),
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                                                )
                                            } else {
                                                val statusText = if (state.isHomeCaptain) {
                                                    if (!isHomeReady) "Válaszd ki a hazai keretet! (Jelenleg: ${state.homeSelectedCount}/4)"
                                                    else "Hazai keret kész. Várakozás a vendég csapatra... (Jelenleg: ${state.guestSelectedCount}/4)"
                                                } else {
                                                    if (!isGuestReady) "Válaszd ki a vendég keretet! (Jelenleg: ${state.guestSelectedCount}/4)"
                                                    else "Vendég keret kész. Várakozás a hazai csapatra... (Jelenleg: ${state.homeSelectedCount}/4)"
                                                }

                                                Button(
                                                    onClick = { /* Semmi */ },
                                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                                    enabled = false
                                                ) {
                                                    Text("Meccs elindítása")
                                                }

                                                Text(
                                                    text = statusText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
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
}

@Composable
fun ParticipantRow(
    name: String,
    status: String,
    showAction: Boolean,
    isLoading: Boolean,
    onToggle: () -> Unit
) {
    val isSelected = status == "SELECTED" || status == "LOCKED"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth(),
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
                enabled = !isLoading
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