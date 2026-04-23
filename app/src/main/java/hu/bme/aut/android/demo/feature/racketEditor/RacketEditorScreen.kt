package hu.bme.aut.android.demo.feature.racketEditor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.ui.common.InfoDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacketEditorScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: RacketEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Új ütő összeállítása") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Vissza a profilhoz")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.saveRacket() }, // Mentés hívása
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                enabled = !state.isLoading
            ) {
                Text("Ütő mentése")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FA
                BladeConfigurationBlock(
                    blade = state.currentBlade,
                    manufacturers = state.bladeManufacturers,
                    models = state.availableBladeModels,
                    onManufacturerChange = { viewModel.updateBladeManufacturer(it) },
                    onModelChange = { viewModel.updateBladeModel(it) }
                )

                HorizontalDivider()

                // TENYERES
                RubberConfigurationBlock(
                    title = "Tenyeres borítás",
                    rubber = state.currentForehand,
                    manufacturers = state.rubberManufacturers,
                    models = state.availableFhModels,
                    colors = state.rubberColors,
                    onManufacturerChange = { viewModel.updateFhManufacturer(it) },
                    onModelChange = { viewModel.updateFhModel(it) },
                    onColorChange = { viewModel.updateFhColor(it) }
                )

                HorizontalDivider()

                // FONÁK
                RubberConfigurationBlock(
                    title = "Fonák borítás",
                    rubber = state.currentBackhand,
                    manufacturers = state.rubberManufacturers,
                    models = state.availableBhModels,
                    colors = state.rubberColors,
                    onManufacturerChange = { viewModel.updateBhManufacturer(it) },
                    onModelChange = { viewModel.updateBhModel(it) },
                    onColorChange = { viewModel.updateBhColor(it) }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isEnabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && isEnabled,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && isEnabled) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            enabled = isEnabled
        )
        ExposedDropdownMenu(
            expanded = expanded && isEnabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun BladeConfigurationBlock(
    blade: Blade,
    manufacturers: List<String>,
    models: List<String>,
    onManufacturerChange: (String) -> Unit,
    onModelChange: (String) -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }

    if (showInfo) {
        InfoDialog(
            title = "Ütőfa (Blade)",
            text = "Az ütőfa a felszerelésed 'lelke'. Ez határozza meg a sebességet, a rugalmasságot és a labdaérzékelést. A fák típusai általában a védekezőtől (DEF) a mindenoldalún (ALL) át az agresszív támadóig (OFF+) terjednek.",
            onDismiss = { showInfo = false }
        )
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Fa adatok", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = { showInfo = true }) {
                Icon(Icons.Default.Info, contentDescription = "Információ a fáról", tint = Color.Gray)
            }
            Icon(Icons.Default.SportsTennis, contentDescription = "Fa ikon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }

        DropdownSelector(
            label = "Gyártó",
            options = manufacturers,
            selectedOption = blade.manufacturer,
            onOptionSelected = onManufacturerChange
        )

        DropdownSelector(
            label = "Modell",
            options = models,
            selectedOption = blade.model,
            onOptionSelected = onModelChange,
            isEnabled = blade.manufacturer.isNotEmpty()
        )
    }
}

@Composable
fun RubberConfigurationBlock(
    title: String,
    rubber: Rubber,
    manufacturers: List<String>,
    models: List<String>,
    colors: List<String>,
    onManufacturerChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onColorChange: (String) -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }

    if (showInfo) {
        InfoDialog(
            title = title,
            text = "A borítás felel a labda megpörgetéséért (spin) és a sebességért. Fontos, hogy versenyeken kötelező egy piros és egy fekete (vagy más ITTF által engedélyezett színű) borítást használni!",
            onDismiss = { showInfo = false }
        )
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = { showInfo = true }) {
                Icon(Icons.Default.Info, contentDescription = "Információ a borításról", tint = Color.Gray)
            }
        }

        DropdownSelector(
            label = "Gyártó",
            options = manufacturers,
            selectedOption = rubber.manufacturer,
            onOptionSelected = onManufacturerChange
        )

        DropdownSelector(
            label = "Modell",
            options = models,
            selectedOption = rubber.model,
            onOptionSelected = onModelChange,
            isEnabled = rubber.manufacturer.isNotEmpty()
        )

        DropdownSelector(
            label = "Szín",
            options = colors,
            selectedOption = rubber.color,
            onOptionSelected = onColorChange
        )
    }
}