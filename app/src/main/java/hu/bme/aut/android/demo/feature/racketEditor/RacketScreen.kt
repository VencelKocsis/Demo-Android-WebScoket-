package hu.bme.aut.android.demo.feature.racketEditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacketScreen(
    onNavigateBack: () -> Unit = {}
) {
    val defaultManufacturer = MANUFACTURERS.firstOrNull() ?: ""
    val defaultBladeModel = MODEL_DATA[defaultManufacturer]?.firstOrNull() ?: ""
    val defaultRubberModel = RUBBER_MODEL_DATA[defaultManufacturer]?.firstOrNull() ?: ""
    val defaultColor = RUBBER_COLORS.firstOrNull() ?: ""

    var currentBlade by remember {
        mutableStateOf(Blade(defaultManufacturer, model = defaultBladeModel))
    }
    var currentForehand by remember {
        mutableStateOf(
            Rubber(
                defaultManufacturer,
                model = defaultRubberModel,
                color = defaultColor
            )
        )
    }
    var currentBackhand by remember {
        mutableStateOf(Rubber(defaultManufacturer, model = defaultRubberModel, color = RUBBER_COLORS.getOrElse(1) { "" }))
    }

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
                onClick = { /* TODO: Mentési logika */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text("Ütő mentése")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BladeConfigurationBlock(
                blade = currentBlade,
                onBladeChange = { currentBlade = it }
            )

            Divider()

            RubberConfigurationBlock(
                title = "Tenyeres borítás",
                rubber = currentForehand,
                onRubberChange = { currentForehand = it }
            )

            Divider()

            RubberConfigurationBlock(
                title = "Fonák borítás",
                rubber = currentBackhand,
                onRubberChange = { currentBackhand = it }
            )

            Spacer(Modifier.height(16.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BladeConfigurationBlock(
    blade: Blade,
    onBladeChange: (Blade) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Fa adatok", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.SportsTennis, contentDescription = "Fa ikon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }

        DropdownSelector(
            label = "Gyártó",
            options = MANUFACTURERS,
            selectedOption = blade.manufacturer,
            onOptionSelected = { newManufacturer ->
                val newBlade = blade.copy(manufacturer = newManufacturer)

                if (MODEL_DATA[newManufacturer]?.contains(blade.model) != true) {
                    newBlade.model = MODEL_DATA[newManufacturer]?.firstOrNull() ?: ""
                }
                onBladeChange(newBlade)
            }
        )

        val availableModels = MODEL_DATA[blade.manufacturer] ?: emptyList()

        DropdownSelector(
            label = "Modell",
            options = availableModels,
            selectedOption = if (availableModels.contains(blade.model)) blade.model else "",
            onOptionSelected = { onBladeChange(blade.copy(model = it)) },
            isEnabled = blade.manufacturer.isNotEmpty()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RubberConfigurationBlock(
    title: String,
    rubber: Rubber,
    onRubberChange: (Rubber) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        }

        DropdownSelector(
            label = "Gyártó",
            options = MANUFACTURERS,
            selectedOption = rubber.manufacturer,
            onOptionSelected = { newManufacturer ->
                val newRubber = rubber.copy(manufacturer = newManufacturer)

                if (RUBBER_MODEL_DATA[newManufacturer]?.contains(rubber.model) != true) {
                    newRubber.model = RUBBER_MODEL_DATA[newManufacturer]?.firstOrNull() ?: ""
                }
                onRubberChange(newRubber)
            }
        )

        val availableModels = RUBBER_MODEL_DATA[rubber.manufacturer] ?: emptyList()

        DropdownSelector(
            label = "Modell",
            options = availableModels,
            selectedOption = if (availableModels.contains(rubber.model)) rubber.model else "",
            onOptionSelected = { onRubberChange(rubber.copy(model = it)) },
            isEnabled = rubber.manufacturer.isNotEmpty()
        )

        DropdownSelector(
            label = "Szín",
            options = RUBBER_COLORS,
            selectedOption = rubber.color,
            onOptionSelected = { onRubberChange(rubber.copy(color = it)) }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RacketScreenPreview() {
    RacketScreen(onNavigateBack = {})
}