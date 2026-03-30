package hu.bme.aut.android.demo.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.feature.auth.AuthViewModel
import hu.bme.aut.android.demo.feature.racketEditor.Blade
import hu.bme.aut.android.demo.feature.racketEditor.Racket
import hu.bme.aut.android.demo.feature.racketEditor.Rubber
import hu.bme.aut.android.demo.util.LanguageSelector

// --- SEGÉDFÜGGVÉNYEK ÉS GRAFIKON ---
@Composable
fun ColorCircle(color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(16.dp).background(color, CircleShape))
}

fun stringToColor(colorName: String): Color {
    return when (colorName.lowercase()) {
        "red" -> Color(0xFFD32F2F)
        "black" -> Color(0xFF212121)
        "blue" -> Color(0xFF1976D2)
        "green" -> Color(0xFF388E3C)
        "yellow" -> Color(0xFFFBC02D)
        else -> Color.Gray
    }
}

// Saját, pehelysúlyú grafikon
@Composable
fun PerformanceGraph(data: List<Float>, color: Color = MaterialTheme.colorScheme.primary) {
    if (data.size < 2) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text("Nincs elég adat a grafikonhoz", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
        }
        return
    }

    val max = data.maxOrNull() ?: 1000f
    val min = data.minOrNull() ?: 1000f
    val range = (max - min).coerceAtLeast(10f)
    val padding = range * 0.1f
    val yMax = max + padding
    val yMin = min - padding
    val yRange = yMax - yMin

    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp)) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1)

        val path = Path()
        val fillPath = Path()
        val points = mutableListOf<Offset>()

        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = height - ((value - yMin) / yRange) * height
            points.add(Offset(x, y))

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        // Átmenetes kitöltés a vonal alatt
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.4f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Vastag vonal rajzolása
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Pöttyök (mérkőzések)
        points.forEach { point ->
            drawCircle(color = color, radius = 6f, center = point)
            drawCircle(color = Color.White, radius = 3f, center = point)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val backendUser = authState.backendUser

    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(backendUser) {
        profileViewModel.initUser(backendUser)
    }

    LaunchedEffect(profileState.error) {
        profileState.error?.let {
            snackbarHostState.showSnackbar(it)
            profileViewModel.clearError()
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }
    var showGraphInfoDialog by remember { mutableStateOf(false) }
    var showH2HInfoDialog by remember { mutableStateOf(false) }

    val user = profileState.user
    val teamNames = profileState.userTeamNames

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_profile)) },
                actions = {
                    LanguageSelector()

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit_profile)) },
                            onClick = {
                                showMenu = false
                                editFirstName = user?.firstName ?: ""
                                editLastName = user?.lastName ?: ""
                                showEditDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onLogoutClick()
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. FELHASZNÁLÓI ALAPADATOK (AVATAR ÉS NÉV) ---
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
                    if (profileState.isLoading) {
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
                        text = if (user != null) "${user.lastName} ${user.firstName}" else "Töltés...",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user?.email ?: stringResource(R.string.no_email),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // Csapat "Pilula"
                val (teamBg, teamTextCol) = if (teamNames.isEmpty()) {
                    if (isDark) Color(0xFFD32F2F).copy(alpha = 0.2f) to Color(0xFFFF8A80) else Color(0xFFFFEBEE) to Color(0xFFD32F2F)
                } else {
                    if (isDark) MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(teamBg)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (teamNames.isEmpty()) stringResource(R.string.no_team) else stringResource(
                            R.string.team, teamNames.joinToString(", ")),
                        color = teamTextCol,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- 2. ALAP STATISZTIKA (Kártya) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Összesített Mérleg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ProfileStatItem(label = stringResource(R.string.match), value = profileState.matchesPlayed.toString(), type = "neutral")
                        ProfileStatItem(label = stringResource(R.string.victory), value = profileState.matchesWon.toString(), type = "success")
                        ProfileStatItem(label = stringResource(R.string.ratio), value = "${profileState.winRate}%", type = "primary")
                    }
                }
            }

            // --- 3. FORMA & SZETT MUTATÓK ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Aktuális Forma
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Forma (Utolsó 5)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (profileState.recentForm.isEmpty()) Text("-", color = Color.Gray)
                            profileState.recentForm.forEach { isWin ->
                                Box(
                                    modifier = Modifier.size(24.dp).clip(CircleShape).background(if (isWin) Color(0xFF4CAF50) else Color(0xFFF44336)),
                                    contentAlignment = Alignment.Center
                                ) { Text(if (isWin) "Gy" else "V", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }

                // Clutch / Söprések
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Szett Mutatók", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${profileState.sweeps}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
                                Text("Söprés (3-0)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${profileState.decidingSetWins}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Text("Döntő szett", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // --- 4. H2H (Nemezis és Kedvenc) ---
            if (profileState.favoriteOpponent != null || profileState.nemesis != null) {
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
                            Text("Egymás Elleni (H2H)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showH2HInfoDialog = true }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        profileState.favoriteOpponent?.let { (name, wins) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kedvenc ellenfél:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.weight(1f))
                                Text("$name ($wins győzelem)", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (profileState.favoriteOpponent != null && profileState.nemesis != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        }

                        profileState.nemesis?.let { (name, losses) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Nemezis:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.weight(1f))
                                Text("$name ($losses vereség)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- 5. GRAFIKON (Élő-pont változás) ---
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
                        Text("Fejlődési Görbe (Forma)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showGraphInfoDialog = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    PerformanceGraph(data = profileState.ratingHistory)
                }
            }

            // --- 6. ÜTŐK (FELSZERELÉS) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                val forehandRubberExample = Rubber("DHS", "Hurricane 3 NEO", "Inverted", 40, 2.1f, "Black")
                val backhandRubberExample = Rubber("Yasaka", "Rakza 7 Soft", "Inverted", 35, 2.0f, "Red")
                val bladeExample = Blade("Butterfly", "Regular", 5, 85f, "OFF", 6.0f, "Timo Boll ALC")
                val racketExample = Racket(
                    blade = "${bladeExample.manufacturer} ${bladeExample.model}",
                    forehandRubber = "${forehandRubberExample.manufacturer} ${forehandRubberExample.model}",
                    backhandRubber = "${backhandRubberExample.manufacturer} ${backhandRubberExample.model}"
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Felszerelés",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.SportsTennis,
                            contentDescription = "Ütő",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()) {
                            Text(text = "Ütőfa", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = racketExample.blade, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ColorCircle(color = stringToColor(forehandRubberExample.color))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Tenyeres", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Text(text = racketExample.forehandRubber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ColorCircle(color = stringToColor(backhandRubberExample.color))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Fonák", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Text(text = racketExample.backhandRubber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* TODO: Új ütő hozzáadása */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ÚJ ÜTŐ HOZZÁADÁSA", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- SZERKESZTŐ DIALOG ---
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.edit_profile), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = editLastName,
                        onValueChange = { editLastName = it },
                        label = { Text(stringResource(R.string.last_name)) },
                        singleLine = true,
                        enabled = !profileState.isLoading
                    )
                    OutlinedTextField(
                        value = editFirstName,
                        onValueChange = { editFirstName = it },
                        label = { Text(stringResource(R.string.first_name)) },
                        singleLine = true,
                        enabled = !profileState.isLoading
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        profileViewModel.updateUser(editFirstName, editLastName)
                        showEditDialog = false
                    },
                    enabled = editFirstName.isNotBlank() && editLastName.isNotBlank() && !profileState.isLoading
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }, enabled = !profileState.isLoading) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        )
    }

    // --- INFO DIALÓGUSOK ---
    if (showGraphInfoDialog) {
        AlertDialog(
            onDismissRequest = { showGraphInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fejlődési Görbe", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Ez a grafikon a becsült Élő-pontszámod (Rating) változását mutatja az idő előrehaladtával.\n\nMinden győzelem növeli, míg minden vereség csökkenti a pontszámodat. A grafikon segít átlátni a hosszútávú fejlődésedet és formádat a lejátszott mérkőzéseid alapján.")
            },
            confirmButton = { TextButton(onClick = { showGraphInfoDialog = false }) { Text("Értem") } }
        )
    }

    if (showH2HInfoDialog) {
        AlertDialog(
            onDismissRequest = { showH2HInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Egymás Elleni (H2H)", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Kedvenc ellenfél: Az a játékos, akit a legtöbbször győztél le a pályafutásod során.\n\nNemezis: Az a játékos, akitől a legtöbbször kaptál ki.")
            },
            confirmButton = { TextButton(onClick = { showH2HInfoDialog = false }) { Text("Értem") } }
        )
    }
}

// KÖZÖS KOMPONENS A PROFIL STATISZTIKÁHOZ
@Composable
fun ProfileStatItem(label: String, value: String, type: String) {
    val isDark = isSystemInDarkTheme()

    val (bgColor, textColor) = when (type) {
        "success" -> if(isDark) Color(0xFF2E7D32).copy(0.25f) to Color(0xFF81C784) else Color(0xFF388E3C) to Color.White
        "primary" -> if(isDark) MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        else -> if(isDark) Color.Gray.copy(0.2f) to Color.LightGray else Color(0xFF757575) to Color.White
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