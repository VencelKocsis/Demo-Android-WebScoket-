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
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.demo.ui.theme.NeutralGraySolid
import hu.bme.aut.android.demo.ui.theme.SuccessGreenDark
import hu.bme.aut.android.demo.ui.theme.SuccessGreenLight
import hu.bme.aut.android.demo.ui.theme.SuccessGreenSolid

// KÖZÖS KOMPONENS A PROFIL STATISZTIKÁHOZ
@Composable
fun ProfileStatItem(label: String, value: String, type: String) {
    val isDark = isSystemInDarkTheme()

    val (bgColor, textColor) = when (type) {
        "success" -> if(isDark) SuccessGreenDark.copy(0.25f) to SuccessGreenLight else SuccessGreenSolid to Color.White
        "primary" -> if(isDark) MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        else -> if(isDark) Color.Gray.copy(0.2f) to Color.LightGray else NeutralGraySolid to Color.White
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