package hu.bme.aut.android.demo.ui.common

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.demo.ui.theme.ErrorRedBg
import hu.bme.aut.android.demo.ui.theme.ErrorRedBorder
import hu.bme.aut.android.demo.ui.theme.ErrorRedLight
import hu.bme.aut.android.demo.ui.theme.ErrorRedSolid
import hu.bme.aut.android.demo.ui.theme.FinishedGrayBg
import hu.bme.aut.android.demo.ui.theme.FinishedGrayDark
import hu.bme.aut.android.demo.ui.theme.FinishedGrayLight
import hu.bme.aut.android.demo.ui.theme.ProgressPink
import hu.bme.aut.android.demo.ui.theme.ProgressPinkBg
import hu.bme.aut.android.demo.ui.theme.ProgressPinkBorder
import hu.bme.aut.android.demo.ui.theme.ProgressPinkDark
import hu.bme.aut.android.demo.ui.theme.SuccessGreenBg
import hu.bme.aut.android.demo.ui.theme.SuccessGreenBorder
import hu.bme.aut.android.demo.ui.theme.SuccessGreenDark
import hu.bme.aut.android.demo.ui.theme.SuccessGreenLight

/**
 * Univerzális meccskártya, amely a bemenő paraméterek alapján alakítja a kinézetét.
 * * @param date Nyers dátum string (a komponens belül tisztítja és formázza)
 * @param homeTeam Hazai csapat neve
 * @param guestTeam Vendég csapat neve (ha null, a "Team" nézet dizájnját kapjuk)
 * @param homeScore Hazai pontszám
 * @param guestScore Vendég pontszám
 * @param isWin A fókuszban lévő csapat nyert-e? (csak Team nézetnél releváns a színkódoláshoz)
 * @param topLabel Bal felső sarok szövege (pl. "5. Forduló")
 * @param status Mérkőzés státusza (pl. "finished", "scheduled"). Ha null, nem mutat státuszt.
 * @param location Helyszín string. Ha nem null, megjelenít egy térkép gombot.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UniversalMatchCard(
    date: String?,
    homeTeam: String,
    guestTeam: String? = null,
    homeScore: Int? = null,
    guestScore: Int? = null,
    isWin: Boolean? = null,
    topLabel: String? = null,
    status: String? = null,
    location: String? = null,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // 1. Dátum tisztítása: Csak a "YYYY-MM-DD" részt mutatjuk, az időt elhagyva
    val displayDate = date?.substringBefore("T") ?: ""

    // 2. Kártya háttér és keret (Bajnokság nézethez)
    val cardBgColor = when (status) {
        "scheduled" -> if (isDark) SuccessGreenDark.copy(alpha = 0.15f) else SuccessGreenBg
        "in_progress" -> if (isDark) ProgressPinkDark.copy(alpha = 0.15f) else ProgressPinkBg
        "finished" -> if (isDark) FinishedGrayDark.copy(alpha = 0.15f) else FinishedGrayBg
        "cancelled" -> if (isDark) ErrorRedSolid.copy(alpha = 0.15f) else ErrorRedBg
        else -> MaterialTheme.colorScheme.surface
    }

    val cardBorderColor = when (status) {
        "scheduled" -> if (isDark) SuccessGreenLight.copy(alpha = 0.5f) else SuccessGreenBorder
        "in_progress" -> if (isDark) ProgressPink.copy(alpha = 0.5f) else ProgressPinkBorder
        "finished" -> if (isDark) FinishedGrayLight.copy(alpha = 0.5f) else FinishedGrayLight
        "cancelled" -> if (isDark) ErrorRedLight.copy(alpha = 0.5f) else ErrorRedBorder
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    // 3. Pontszám doboz színei
    val (scoreBgColor, scoreTextColor) = getScoreColors(isWin)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // --- FEJLÉC (Okos elrendezéssel) ---
            if (topLabel != null || guestTeam != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (topLabel != null) {
                        // HISTORY NÉZET: Forduló balra, Dátum jobbra
                        Text(
                            text = topLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = displayDate, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    } else {
                        // BAJNOKSÁG (TeamMatch) NÉZET: Nincs forduló a kártyán, így a Dátum kerül BALRA
                        Text(text = displayDate, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // --- FŐ TARTALOM (Csapatok és Eredmény/VS) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = homeTeam,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (homeScore != null && guestScore != null && homeScore > guestScore) FontWeight.Black else if (guestTeam == null) FontWeight.Bold else FontWeight.Normal
                    )

                    if (guestTeam != null) {
                        // History / Bajnokság elrendezés (Egymás alatt)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = guestTeam,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (homeScore != null && guestScore != null && guestScore > homeScore) FontWeight.Black else FontWeight.Normal
                        )
                    } else {
                        // Sima TeamScreen elrendezés (Dátum a név alatt)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = displayDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                // --- EREDMÉNY DOBOZ VAGY "VS" JELZÉS ---
                if (homeScore != null && guestScore != null && status != "scheduled" && status != "cancelled") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(scoreBgColor)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$homeScore - $guestScore",
                            color = scoreTextColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (guestTeam != null) {
                    // Ha még nincs pontszám (pl. jövőbeli meccs), írjunk ki egy VS-t, hogy kitöltse a teret
                    Text(
                        text = "VS",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            // --- LÁBLÉC (Státusz Pilula és Térkép) ---
            if (status != null || location != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (status != null) {
                        MatchStatusChip(status = status)
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (location != null) {
                        MatchLocationButton(location = location)
                    }
                }
            }
        }
    }
}