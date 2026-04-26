package hu.bme.aut.android.demo.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.feature.profile.ColorCircle
import hu.bme.aut.android.demo.feature.profile.stringToColor
import hu.bme.aut.android.demo.ui.theme.*

// --- 1. PROFIL FEJLÉC (Kép, Név, Email, Csapat) ---
@Composable
fun ProfileHeader(
    firstName: String,
    lastName: String,
    email: String?,
    teamNames: List<String>,
    isLoading: Boolean = false
) {
    val isDark = isSystemInDarkTheme()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profil ikon",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$lastName $firstName",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = email ?: stringResource(R.string.no_email),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        val (teamBg, teamTextCol) = if (teamNames.isEmpty()) {
            if (isDark) ErrorRedSolid.copy(alpha = 0.2f) to ErrorRedLight else ErrorRedBg to ErrorRedSolid
        } else {
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(teamBg)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (teamNames.isEmpty()) stringResource(R.string.no_team) else stringResource(R.string.team_one_var, teamNames.joinToString(", ")),
                color = teamTextCol,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- 2. ALAP STATISZTIKA KÁRTYA ---
@Composable
fun AggregateStatsCard(
    matchesPlayed: Int,
    matchesWon: Int,
    winRate: Int,
    onInfoClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.aggregate_balance_sheet), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onInfoClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(label = stringResource(R.string.match), value = matchesPlayed.toString(), type = "neutral", circleSize = 56.dp, isLargeText = true)
                StatItem(label = stringResource(R.string.victory), value = matchesWon.toString(), type = "success", circleSize = 56.dp, isLargeText = true)
                StatItem(label = stringResource(R.string.ratio), value = "${winRate}%", type = "primary", circleSize = 56.dp, isLargeText = true)
            }
        }
    }
}

// --- 3. FORMA KÁRTYA ---
@Composable
fun RecentFormCard(recentForm: List<Boolean>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.from_last_5), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (recentForm.isEmpty()) {
                    Text("-", color = Color.Gray)
                } else {
                    recentForm.forEach { isWin ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isWin) SuccessGreenSolid else ErrorRedSolid),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (isWin) stringResource(R.string.victory_letter) else stringResource(R.string.lose_letter),
                                color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 4. EXTRA MUTATÓK KÁRTYA ---
@Composable
fun ExtraStatsCard(sweeps: Int, decidingSetWins: Int, flawlessDays: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.extra_stats), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$sweeps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = SuccessGreenSolid)
                    Text("3-0", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$decidingSetWins", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Text("3-2", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$flawlessDays", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = WarningOrangeSolid)
                    Text("4/4", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

// --- 5. H2H KÁRTYA ---
@Composable
fun H2HCard(favoriteOpponent: Pair<String, Int>?, nemesis: Pair<String, Int>?, onInfoClick: () -> Unit) {
    if (favoriteOpponent == null && nemesis == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.againts_each_other), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onInfoClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            favoriteOpponent?.let { (name, wins) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.favorite_opponent), style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.two_var_victory, name, wins), fontWeight = FontWeight.Bold)
                }
            }

            if (favoriteOpponent != null && nemesis != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }

            nemesis?.let { (name, losses) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.nemesis), style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.two_var_lose, name, losses), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 6. GRAFIKON KÁRTYA ---
@Composable
fun RatingGraphCard(ratingHistory: List<Float>, onInfoClick: () -> Unit) {
    if (ratingHistory.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.improvement_graph), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onInfoClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            PerformanceGraph(data = ratingHistory)
        }
    }
}

// --- DATA CLASS A UI-NAK ---
data class RacketUiModel(
    val bladeName: String,
    val fhName: String,
    val fhColorName: String,
    val bhName: String,
    val bhColorName: String
)

// --- 7. FELSZERELÉS KÁRTYA (FRISSÍTETT) ---
@Composable
fun EquipmentCard(
    rackets: List<RacketUiModel>,
    onAddEquipmentClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.equipment), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.SportsTennis, contentDescription = "Ütő", tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (rackets.isEmpty()) {
                // Ha még nincs ütője
                Text(
                    text = "Nincs még rögzített felszerelés.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                // Ha van ütője, végigiterálunk rajtuk
                rackets.forEachIndexed { index, racket ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.padding(bottom = if (index < rackets.lastIndex) 12.dp else 0.dp)
                    ) {
                        Column(modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()) {
                            Text(stringResource(R.string.blade), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(racket.bladeName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ColorCircle(color = stringToColor(racket.fhColorName))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(R.string.forehand), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Text(racket.fhName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ColorCircle(color = stringToColor(racket.bhColorName))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(R.string.backhand), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Text(racket.bhName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            if (onAddEquipmentClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddEquipmentClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_new_racket), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}