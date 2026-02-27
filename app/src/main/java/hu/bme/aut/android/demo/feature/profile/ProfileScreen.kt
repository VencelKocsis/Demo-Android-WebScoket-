package hu.bme.aut.android.demo.feature.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.feature.auth.AuthViewModel
import hu.bme.aut.android.demo.feature.racketEditor.Blade
import hu.bme.aut.android.demo.feature.racketEditor.Racket
import hu.bme.aut.android.demo.feature.racketEditor.Rubber

@Composable
fun ColorCircle(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(10.dp)
            .background(color, CircleShape)
    )
}

fun stringToColor(colorName: String): Color {
    return when (colorName.lowercase()) {
        "red" -> Color.Red
        "black" -> Color.Black
        "blue" -> Color.Blue
        "green" -> Color.Green
        "yellow" -> Color.Yellow
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
    // 1. Lekérjük a bejelentkezett felhasználót az AuthViewModel-ből
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val backendUser = authState.backendUser

    // 2. Amint megérkezik a backendUser (pl. betölt a hálózatról), átadjuk a ProfileViewModel-nek!
    LaunchedEffect(backendUser) {
        if (backendUser != null) {
            profileViewModel.setUser(backendUser)
        }
    }

    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user

    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Ideiglenes változók a dialoghoz
    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }

    // Dummy adatok, amiket később a backendről húzunk be
    val teamName = "Saját Csapat (TODO)"
    val seasonName = "2026 Tavasz"
    val matchesPlayed = 0
    val matchesWon = 0
    val winRate = if (matchesPlayed > 0) (matchesWon * 100) / matchesPlayed else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menü"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profil szerkesztése") },
                            onClick = {
                                showMenu = false
                                editFirstName = user?.firstName ?: ""
                                editLastName = user?.lastName ?: ""
                                showEditDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            // TODO add edit and delete rackets functionality
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Kijelentkezés") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), // Fix térköz a blokkok között
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- FELHASZNÁLÓI ALAPADATOK ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profil ikon",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                // Ha van betöltött user, az ő nevét mutatjuk, különben "Töltés..."
                Text(
                    text = if (user != null) "${user.lastName} ${user.firstName}" else "Töltés...",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = user?.email ?: "Nincs email",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Csapat: $teamName", style = MaterialTheme.typography.titleMedium)

                // --- STATISZTIKA (TODO) ---

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Saját statisztika", style = MaterialTheme.typography.titleMedium)
                        Text("Érvényes: $seasonName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        Divider()
                        Text("Lejátszott meccsek: $matchesPlayed")
                        Text("Megnyert meccsek: $matchesWon")
                        Text("Győzelmi arány: $winRate%")
                    }
                }
            }

            // --- ÜTŐK (Dummy maradt egyelőre) ---

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                val forehandRubberExample = Rubber("DHS", "Hurricane 3 NEO", "Inverted", 40, 2.1f, "Black")
                val backhandRubberExample =
                    Rubber("Yasaka", "Rakza 7 Soft", "Inverted", 35, 2.0f, "Red")
                val bladeExample =
                    Blade("Butterfly", "Regular", 5, 85f, "OFF", 6.0f, "Timo Boll ALC")
                val racketExample = Racket(
                    blade = "${bladeExample.manufacturer} ${bladeExample.model}",
                    forehandRubber = "${forehandRubberExample.manufacturer} ${forehandRubberExample.model}",
                    backhandRubber = "${backhandRubberExample.manufacturer} ${backhandRubberExample.model}"
                )

                Column (
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ütők",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.SportsTennis,
                            contentDescription = "Ütő ikon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Divider()

                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Fa: ${racketExample.blade}")

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Tenyeres: ${racketExample.forehandRubber}", modifier = Modifier.weight(1f)) // A szöveg kitölti a rendelkezésre álló helyet
                            Spacer(modifier = Modifier.width(8.dp))
                            ColorCircle(color = stringToColor(forehandRubberExample.color))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Fonák: ${racketExample.backhandRubber}", modifier = Modifier.weight(1f)) // A szöveg kitölti a rendelkezésre álló helyet
                            Spacer(modifier = Modifier.width(8.dp))
                            ColorCircle(color = stringToColor(backhandRubberExample.color))
                        }
                    }
                }
            }

            Button(
                onClick = { /* TODO: Új ütő hozzáadása */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Új ütő hozzáadása",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Új ütő hozzáadása")
            }
        }
    }

    // --- SZERKESZTŐ DIALOG ---
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Profil szerkesztése") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editLastName,
                        onValueChange = { editLastName = it },
                        label = { Text("Vezetéknév") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editFirstName,
                        onValueChange = { editFirstName = it },
                        label = { Text("Keresztnév") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        profileViewModel.updateUser(editFirstName, editLastName)
                        showEditDialog = false
                    }
                ) {
                    Text("Mentés")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Mégse")
                }
            }
        )
    }
}
