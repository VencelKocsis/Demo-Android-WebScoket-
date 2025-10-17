package hu.bme.aut.android.demo.feature.list_players

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO

/**
 * Játékoslista képernyő, amely megjeleníti a játékosokat,
 * és lehetőséget biztosít hozzáadásra, illetve hosszú nyomásra törlésre.
 *
 * @param viewModel A PlayersViewModel, amely a játékosok listáját kezeli.
 * @param onLogout A kijelentkezési művelet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen(
    viewModel: PlayersViewModel,
    onLogout: () -> Unit
) {
    // ViewModel állapotok figyelése a 'by' kulcsszóval (mutableStateOf-hoz)
    val players by viewModel.players
    val loading by viewModel.loading
    val error by viewModel.error

    // Lokális UI állapotok
    var showAddDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var playerToDelete by remember { mutableStateOf<PlayerDTO?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Játékosok Listája") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Kijelentkezés")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Új játékos hozzáadása")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // A tartalom elhelyezése a TopBar alatt
        ) {
            // Betöltési indikátor (Ez okozta a hibát a verzió-ütközés miatt)
            if (loading) {
                // Ideiglenesen a progress indicator-t egy egyszerű Spacer-re cserélve kiküszöbölhető a hiba,
                // de a helyes megoldás a Compose verziók szinkronizálása a Gradle fájlban.
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Hibaüzenet
            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Játékoslista
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
            ) {
                items(players, key = { it.id }) { p ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .pointerInput(p.id) {
                                detectTapGestures(
                                    onLongPress = {
                                        playerToDelete = p
                                    }
                                )
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .heightIn(min = 48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column {
                                Text(p.name, style = MaterialTheme.typography.titleMedium)
                                Text("Kor: ${p.age ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Hely a FAB számára, ha a lista végéig görgetünk
                item { Spacer(modifier = Modifier.height(64.dp)) }
            }
        }
    }

    // --- Új játékos hozzáadó dialog ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addPlayer(name, age.toIntOrNull())
                        showAddDialog = false
                        name = ""
                        age = ""
                    }
                }, enabled = name.isNotBlank()) {
                    Text("Mentés")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Mégse") }
            },
            title = { Text("Új játékos hozzáadása") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Név") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Kor (opcionális)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    // --- Törlés megerősítő dialog ---
    playerToDelete?.let { player ->
        AlertDialog(
            onDismissRequest = { playerToDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePlayer(player.id)
                    playerToDelete = null
                }) { Text("Törlés", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { playerToDelete = null }) { Text("Mégse") }
            },
            title = { Text("Biztosan törölni szeretnéd?") },
            text = { Text("A(z) \"${player.name}\" játékos törlésre kerül.") }
        )
    }
}
