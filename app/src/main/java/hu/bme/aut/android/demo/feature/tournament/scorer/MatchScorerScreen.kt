package hu.bme.aut.android.demo.feature.tournament.scorer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScorerScreen(
    individualMatchId: Int,
    matchId: Int,
    onNavigateBack: () -> Unit,
    viewModel: MatchScorerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eredmény rögzítése") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Vissza") }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                state.match?.let { match ->
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                        // --- FEJLÉC ÉS ÖSSZESÍTŐ ---
                        Text("Állás (Szettek)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(match.homePlayerName, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text("${state.homeSetsWon}", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                            }
                            Text(":", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(match.guestPlayerName, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text("${state.guestSetsWon}", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // --- SZETTEK PONTJAINAK BEVITELE ---
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Szettek részletei", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))

                                state.sets.forEachIndexed { index, setScore ->

                                    // Kiszámoljuk, hogy kiemeljük-e valamelyik dobozt (ha megnyerte a szettet)
                                    val hScore = setScore.home.toIntOrNull() ?: 0
                                    val gScore = setScore.guest.toIntOrNull() ?: 0
                                    val isHomeWinner = hScore >= 11 && (hScore - gScore) >= 2
                                    val isGuestWinner = gScore >= 11 && (gScore - hScore) >= 2

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        // Hazai Input
                                        ScoreInputBox(
                                            value = setScore.home,
                                            onValueChange = { viewModel.updateSetScore(index, it, setScore.guest) },
                                            isWinner = isHomeWinner
                                        )

                                        Text(" : ", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                        // Vendég Input
                                        ScoreInputBox(
                                            value = setScore.guest,
                                            onValueChange = { viewModel.updateSetScore(index, setScore.home, it) },
                                            isWinner = isGuestWinner
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // --- MENTÉS GOMBOK ---
                        val isMatchOver = state.homeSetsWon == 3 || state.guestSetsWon == 3

                        if (!state.isFinished) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.submitScore(isFinal = false) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    enabled = !state.isSaving
                                ) {
                                    Text("Mentés (Még tart)")
                                }

                                Button(
                                    onClick = { viewModel.submitScore(isFinal = true) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    enabled = !state.isSaving && isMatchOver,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("VÉGLEGESÍTÉS")
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
                            ) {
                                Text(
                                    "A mérkőzés lezárult.",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// KOMPONENS A PONTOK BEÍRÁSÁHOZ
@Composable
fun ScoreInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    isWinner: Boolean
) {
    // Ha nyertes, a doboz kitöltött (primary) lesz, különben szürke körvonalas
    val containerColor = if (isWinner) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val textColor = if (isWinner) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isWinner) Color.Transparent else MaterialTheme.colorScheme.outlineVariant

    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            // Csak számokat engedünk, és maximum 2 karaktert (pl. 99)
            if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
            ),
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = textColor
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .size(64.dp) // Szép, nagy, ujjbarát négyzet
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor)
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (value.isEmpty()) {
                    Text("-", color = Color.Gray.copy(alpha = 0.3f), style = MaterialTheme.typography.headlineMedium)
                }
                innerTextField()
            }
        }
    )
}