package hu.bme.aut.android.demo.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.demo.R

/**
 * Univerzális kártya a szűrőknek.
 * Automatikusan beállítja a hátteret és a paddingokat.
 */
@Composable
fun FilterCard(
    modifier: Modifier = Modifier,
    topRowContent: @Composable RowScope.() -> Unit,
    bottomRowContent: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                content = topRowContent
            )
            bottomRowContent()
        }
    }
}

/**
 * Generikus Legördülő Menü.
 * @param T A lista elemeinek típusa (lehet String, Pair, egyedi adatmodell)
 * @param label A beviteli mező címkéje (pl. "Szezon")
 * @param defaultOptionText Az alapértelmezett, "üres" opció szövege (pl. "Összes")
 * @param options A választható elemek listája
 * @param selectedOption Az aktuálisan kiválasztott elem
 * @param optionLabeler Egy függvény, ami megmondja, hogyan kell a T típust szöveggé alakítani
 * @param onOptionSelected Esemény, ha a felhasználó választott valamit (null, ha az alapértelmezettet választja)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> GenericFilterDropdown(
    label: String,
    defaultOptionText: String,
    options: List<T>,
    selectedOption: T?,
    optionLabeler: @Composable (T) -> String,
    onOptionSelected: (T?) -> Unit,
    modifier: Modifier = Modifier
) {
    // A komponens SAJÁT MAGA kezeli, hogy nyitva van-e
    var expanded by remember { mutableStateOf(false) }

    // Kiszámoljuk az éppen megjelenítendő szöveget
    val currentLabel = selectedOption?.let { optionLabeler(it) } ?: defaultOptionText

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // "Összes" vagy alapértelmezett opció
            DropdownMenuItem(
                text = { Text(defaultOptionText, fontWeight = FontWeight.Bold) },
                onClick = {
                    onOptionSelected(null)
                    expanded = false
                }
            )

            // A dinamikus opciók
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabeler(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun translateSeasonName(rawName: String): String {
    val spring = stringResource(R.string.season_spring)
    val autumn = stringResource(R.string.season_autumn)
    val summer = stringResource(R.string.season_summer)
    val winter = stringResource(R.string.season_winter)

    return rawName
        .replace("Tavasz", spring, ignoreCase = true)
        .replace("Ősz", autumn, ignoreCase = true)
        .replace("Nyár", summer, ignoreCase = true)
        .replace("Tél", winter, ignoreCase = true)
}