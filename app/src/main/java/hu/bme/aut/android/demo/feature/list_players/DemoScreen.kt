package hu.bme.aut.android.demo.feature.list_players

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.demo.domain.model.PlayerDTO

@Composable
fun DemoScreen(viewModel: PlayersViewModel) {
    val players by viewModel.players
    val loading by viewModel.loading
    val error by viewModel.error

    var showAddDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var playerToDelete by remember { mutableStateOf<PlayerDTO?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
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
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(p.name, style = MaterialTheme.typography.titleMedium)
                            Text("Kor: ${p.age ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
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
                }) { Text("Mentés") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Mégse") }
            },
            title = { Text("Új játékos hozzáadása") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Név") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Kor") })
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
                }) { Text("Törlés") }
            },
            dismissButton = {
                TextButton(onClick = { playerToDelete = null }) { Text("Mégse") }
            },
            title = { Text("Biztosan törölni szeretnéd?") },
            text = { Text("A(z) \"${player.name}\" játékos törlésre kerül.") }
        )
    }
}
