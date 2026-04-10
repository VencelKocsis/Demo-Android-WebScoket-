package hu.bme.aut.android.demo.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.demo.ui.theme.ErrorRedLight
import hu.bme.aut.android.demo.ui.theme.ErrorRedSolid
import hu.bme.aut.android.demo.ui.theme.NeutralGraySolid
import hu.bme.aut.android.demo.ui.theme.SuccessGreenDark
import hu.bme.aut.android.demo.ui.theme.SuccessGreenLight
import hu.bme.aut.android.demo.ui.theme.SuccessGreenSolid
import hu.bme.aut.android.demo.ui.theme.WarningOrangeDark
import hu.bme.aut.android.demo.ui.theme.WarningOrangeLight
import hu.bme.aut.android.demo.ui.theme.WarningOrangeSolid

/**
 * Univerzális statisztika megjelenítő kör (pl. Meccsek száma, Győzelmek, Pontok).
 *
 * @param label A statisztika neve (alatta jelenik meg)
 * @param value A statisztika értéke (a kör közepén)
 * @param type A szín típusa ("success", "error", "warning", "primary", vagy default semleges)
 * @param circleSize A kör átmérője (alapértelmezetten 48.dp, profiloknál érdemes 56.dp-t használni)
 * @param isLargeText Igaz esetén nagyobb betűméretet használ az értékhez (profilokhoz)
 */
@Composable
fun StatItem(
    label: String,
    value: String,
    type: String,
    circleSize: Dp = 48.dp, // Paraméterezhető méret
    isLargeText: Boolean = false // Paraméterezhető betűméret
) {
    val isDark = isSystemInDarkTheme()

    val (bgColor, textColor) = when (type) {
        "success" -> if(isDark) SuccessGreenDark.copy(0.25f) to SuccessGreenLight else SuccessGreenSolid to Color.White
        "error" -> if(isDark) ErrorRedSolid.copy(0.25f) to ErrorRedLight else ErrorRedSolid to Color.White
        "warning" -> if(isDark) WarningOrangeDark.copy(0.25f) to WarningOrangeLight else WarningOrangeSolid to Color.White
        "primary" -> if(isDark) MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        else -> if(isDark) Color.Gray.copy(0.2f) to Color.LightGray else NeutralGraySolid to Color.White
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = if (isLargeText) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(if (isLargeText) 8.dp else 6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}