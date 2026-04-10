package hu.bme.aut.android.demo.ui.common

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.util.toDisplayDate
import androidx.compose.ui.res.stringResource
import hu.bme.aut.android.demo.ui.theme.ErrorRedLight
import hu.bme.aut.android.demo.ui.theme.ErrorRedSolid
import hu.bme.aut.android.demo.ui.theme.FinishedGrayDark
import hu.bme.aut.android.demo.ui.theme.FinishedGrayLight
import hu.bme.aut.android.demo.ui.theme.FinishedGraySolid
import hu.bme.aut.android.demo.ui.theme.NeutralGrayLight
import hu.bme.aut.android.demo.ui.theme.ProgressPink
import hu.bme.aut.android.demo.ui.theme.ProgressPinkDark
import hu.bme.aut.android.demo.ui.theme.ProgressPinkSolid
import hu.bme.aut.android.demo.ui.theme.SuccessGreenDark
import hu.bme.aut.android.demo.ui.theme.SuccessGreenLight
import hu.bme.aut.android.demo.ui.theme.SuccessGreenSolid

@Composable
fun MatchLocationButton(
    location: String?,
    modifier: Modifier = Modifier
) {
    if (location.isNullOrEmpty()) return
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            val uri = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        },
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Térkép megnyitása",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.map, location),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MatchDateRow(
    date: String?,
    modifier: Modifier = Modifier
) {
    if (date.isNullOrEmpty()) return
    val formattedDate = remember(date) { date.toDisplayDate() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

// --- DINAMIKUS SZÍNEK (Maximális kontraszt Világos módban is) ---
@Composable
fun getStatusTheme(status: String?): Pair<Color, Color> {
    val isDark = isSystemInDarkTheme()

    return when (status) {
        "scheduled" -> if (isDark) {
            SuccessGreenDark.copy(alpha = 0.2f) to SuccessGreenLight
        } else {
            SuccessGreenDark to Color.White // Teli sötétzöld, fehér szöveg
        }

        "in_progress" -> if (isDark) {
            ProgressPinkDark.copy(alpha = 0.2f) to ProgressPink
        } else {
            ProgressPinkSolid to Color.White // Teli magenta, fehér szöveg
        }

        "finished" -> if (isDark) {
            FinishedGrayDark.copy(alpha = 0.2f) to FinishedGrayLight
        } else {
            FinishedGraySolid to Color.White // Teli kékesszürke, fehér szöveg
        }

        "cancelled" -> if (isDark) {
            ErrorRedSolid.copy(alpha = 0.2f) to ErrorRedLight
        } else {
            ErrorRedSolid to Color.White // Teli piros, fehér szöveg
        }

        else -> if (isDark) {
            Color.Gray.copy(alpha = 0.2f) to Color.LightGray
        } else {
            NeutralGrayLight to Color.White // Teli szürke, fehér szöveg
        }
    }
}

@Composable
fun getStatusText(status: String?): String {
    return when (status) {
        "scheduled" -> stringResource(R.string.status_scheduled)
        "in_progress" -> stringResource(R.string.status_in_progress)
        "finished" -> stringResource(R.string.status_finished)
        "cancelled" -> stringResource(R.string.status_cancelled)
        else -> status ?: stringResource(R.string.status_unknown)
    }
}

// --- ANIMÁLT LIVE INDIKÁTOR (Kapott színnel dolgozik) ---
@Composable
fun LiveIndicator(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = alpha), // A kapott színt pulzáltatjuk
            modifier = Modifier.size(8.dp)
        ) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "LIVE",
            color = color, // A szöveg is az átadott színű lesz
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black
        )
    }
}

// --- A KÖZÖS STÁTUSZ PILULA ---
@Composable
fun MatchStatusChip(status: String?, modifier: Modifier = Modifier) {
    val (bgColor, contentColor) = getStatusTheme(status)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (status == "in_progress") {
            LiveIndicator(color = contentColor)
            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = getStatusText(status),
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// --- Pontszám háttér és szövegszín meghatározása ---
@Composable
fun getScoreColors(isWin: Boolean? = null): Pair<Color, Color> {
    val isDark = isSystemInDarkTheme()

    // Ha nem tudjuk, nyert-e (pl. History vagy Tournament nézet), használjuk az alap primary színt
    if (isWin == null) {
        return if(isDark) MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    }

    // Ha nyert a "fókuszban lévő" csapat (Team nézet)
    return if (isWin) {
        if(isDark) SuccessGreenDark.copy(0.25f) to SuccessGreenLight else SuccessGreenSolid to Color.White
    } else { // Ha vesztett
        if(isDark) ErrorRedSolid.copy(0.25f) to ErrorRedLight else ErrorRedSolid to Color.White
    }
}