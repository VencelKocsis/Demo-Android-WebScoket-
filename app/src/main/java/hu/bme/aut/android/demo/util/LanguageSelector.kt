package hu.bme.aut.android.demo.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import hu.bme.aut.android.demo.R

@Composable
fun LanguageSelector(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    // Lekérjük a jelenleg beállított nyelvet (ha nincs, akkor alapból 'hu')
    val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val isHungarian = currentLocale.isEmpty() || currentLocale.contains("hu")

    Box(modifier = modifier.wrapContentSize(Alignment.TopStart)) {
        // A gomb, ami megnyitja a menüt
        OutlinedButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = stringResource(id = R.string.language_selector)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isHungarian) "Magyar" else "English")
        }

        // A legördülő menü
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Magyar") },
                onClick = {
                    // Nyelv átállítása magyarra és mentés!
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("hu"))
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("English") },
                onClick = {
                    // Nyelv átállítása angolra és mentés!
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                    expanded = false
                }
            )
        }
    }
}