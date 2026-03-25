package hu.bme.aut.android.demo.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import hu.bme.aut.android.demo.feature.auth.AuthViewModel
import hu.bme.aut.android.demo.feature.racketEditor.Blade
import hu.bme.aut.android.demo.feature.racketEditor.Racket
import hu.bme.aut.android.demo.feature.racketEditor.Rubber
import hu.bme.aut.android.demo.util.LanguageSelector

@Composable
fun ColorCircle(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(16.dp)
            .background(color, CircleShape)
    )
}

fun stringToColor(colorName: String): Color {
    return when (colorName.lowercase()) {
        "red" -> Color(0xFFD32F2F)
        "black" -> Color(0xFF212121)
        "blue" -> Color(0xFF1976D2)
        "green" -> Color(0xFF388E3C)
        "yellow" -> Color(0xFFFBC02D)
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

    val user = profileState.user
    val teamNames = profileState.userTeamNames

    // Dummy adatok a statisztikához
    val seasonName = "2026 Tavasz"
    val matchesPlayed = 12
    val matchesWon = 9
    val winRate = if (matchesPlayed > 0) (matchesWon * 100) / matchesPlayed else 0

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
                .verticalScroll(rememberScrollState()) // GÖRGETHETŐSÉG!
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp), // Kicsit szellősebb térköz
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
                    if (isDark) Color(0xFFD32F2F).copy(alpha = 0.2f) to Color(0xFFFF8A80) else Color(0xFFFFEBEE) to Color(0xFFD32F2F)
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
                            R.string.team, teamNames.joinToString(", ")),
                        color = teamTextCol,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- 2. STATISZTIKA ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.personal_statistics), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(seasonName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem(label = stringResource(R.string.match), value = matchesPlayed.toString(), type = "neutral")
                        ProfileStatItem(label = stringResource(R.string.victory), value = matchesWon.toString(), type = "success")
                        ProfileStatItem(label = stringResource(R.string.ratio), value = "$winRate%", type = "primary")
                    }
                }
            }

            // --- 3. ÜTŐK (FELSZERELÉS) --- // TODO
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
}

// ÚJ KÖZÖS KOMPONENS A PROFIL STATISZTIKÁHOZ
@Composable
fun ProfileStatItem(label: String, value: String, type: String) {
    val isDark = isSystemInDarkTheme()

    val (bgColor, textColor) = when (type) {
        "success" -> if(isDark) Color(0xFF2E7D32).copy(0.25f) to Color(0xFF81C784) else Color(0xFF388E3C) to Color.White
        "primary" -> if(isDark) MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        else -> if(isDark) Color.Gray.copy(0.2f) to Color.LightGray else Color(0xFF757575) to Color.White
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}