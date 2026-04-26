package hu.bme.aut.android.demo.feature.racketEditor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.ui.common.InfoDialog

// --- IMPORTÁLUK A TÉMA SZÍNEIT ---
import hu.bme.aut.android.demo.ui.theme.ErrorRedSolid
import hu.bme.aut.android.demo.ui.theme.RacketBlack
import hu.bme.aut.android.demo.ui.theme.RacketBlue
import hu.bme.aut.android.demo.ui.theme.RacketYellow
import hu.bme.aut.android.demo.ui.theme.SuccessGreenSolid
import hu.bme.aut.android.demo.ui.theme.ProgressPink
import hu.bme.aut.android.demo.ui.theme.Purple40

// --- KOMPONENS A SZÍNES PÖTTYHÖZ ---
@Composable
fun ColorDot(colorName: String, modifier: Modifier = Modifier) {
    val color = when (colorName.lowercase()) {
        stringResource(R.string.red).lowercase() -> ErrorRedSolid
        stringResource(R.string.black).lowercase() -> RacketBlack
        stringResource(R.string.blue).lowercase() -> RacketBlue
        stringResource(R.string.green).lowercase() -> SuccessGreenSolid
        stringResource(R.string.yellow).lowercase() -> RacketYellow
        stringResource(R.string.pink).lowercase() -> ProgressPink
        stringResource(R.string.purple).lowercase() -> Purple40
        else -> Color.Transparent
    }

    if (color != Color.Transparent) {
        Box(
            modifier = modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacketEditorScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: RacketEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.racketId == null) stringResource(R.string.new_racket_assembly) else "Ütő szerkesztése") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Vissza a profilhoz")
                    }
                },
                actions = {
                    // Csak akkor jelenik meg a törlés gomb, ha már létező ütőt szerkesztünk!
                    if (state.racketId != null) {
                        IconButton(onClick = { viewModel.deleteRacket() }) {
                            Icon(
                                Icons.Default.Close, // X ikon
                                contentDescription = "Ütő törlése",
                                tint = MaterialTheme.colorScheme.error // Piros színű lesz
                            )
                        }
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
                Text(stringResource(R.string.save_racket))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // TENYERES
                    RubberConfigurationBlock(
                        title = stringResource(R.string.forehand_rubber),
                        iconResId = R.drawable.forehand_rubber_icon,
                        rubber = state.currentForehand,
                        manufacturers = state.rubberManufacturers,
                        models = state.availableFhModels,
                        colors = state.rubberColors,
                        onManufacturerChange = { viewModel.updateFhManufacturer(it) },
                        onModelChange = { viewModel.updateFhModel(it) },
                        onColorChange = { viewModel.updateFhColor(it) },
                        rotationAngle = -45f
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // FONÁK
                    RubberConfigurationBlock(
                        title = stringResource(R.string.backhand_rubber),
                        iconResId = R.drawable.backhand_rubber_icon,
                        rubber = state.currentBackhand,
                        manufacturers = state.rubberManufacturers,
                        models = state.availableBhModels,
                        colors = state.rubberColors,
                        onManufacturerChange = { viewModel.updateBhManufacturer(it) },
                        onModelChange = { viewModel.updateBhModel(it) },
                        onColorChange = { viewModel.updateBhColor(it) },
                        rotationAngle = 45f
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }

            state.errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
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
    isEnabled: Boolean = true,
    isColorSelector: Boolean = false
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
            leadingIcon = if (isColorSelector && selectedOption.isNotEmpty()) {
                { ColorDot(colorName = selectedOption) }
            } else null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && isEnabled) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = isEnabled
        )
        ExposedDropdownMenu(
            expanded = expanded && isEnabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isColorSelector) {
                                ColorDot(colorName = selectionOption)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(selectionOption)
                        }
                    },
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
            title = stringResource(R.string.racket),
            text = stringResource(R.string.raacket_description),
            onDismiss = { showInfo = false }
        )
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.blade_icon),
                contentDescription = "Fa ikon",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.racket_details), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showInfo = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Információ a fáról", tint = Color.Gray)
                }
            }
        }

        DropdownSelector(
            label = stringResource(R.string.manufacturer),
            options = manufacturers,
            selectedOption = blade.manufacturer,
            onOptionSelected = onManufacturerChange
        )

        DropdownSelector(
            label = stringResource(R.string.model),
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
    iconResId: Int,
    rubber: Rubber,
    manufacturers: List<String>,
    models: List<String>,
    colors: List<String>,
    onManufacturerChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    rotationAngle: Float = 0f
) {
    var showInfo by remember { mutableStateOf(false) }

    if (showInfo) {
        InfoDialog(
            title = title,
            text = stringResource(R.string.rubber_description),
            onDismiss = { showInfo = false }
        )
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "Borítás ikon",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .rotate(rotationAngle)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showInfo = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Információ a borításról", tint = Color.Gray)
                }
            }
        }

        DropdownSelector(
            label = stringResource(R.string.manufacturer),
            options = manufacturers,
            selectedOption = rubber.manufacturer,
            onOptionSelected = onManufacturerChange
        )

        DropdownSelector(
            label = stringResource(R.string.model),
            options = models,
            selectedOption = rubber.model,
            onOptionSelected = onModelChange,
            isEnabled = rubber.manufacturer.isNotEmpty()
        )

        DropdownSelector(
            label = stringResource(R.string.color),
            options = colors,
            selectedOption = rubber.color,
            onOptionSelected = onColorChange,
            isColorSelector = true
        )
    }
}