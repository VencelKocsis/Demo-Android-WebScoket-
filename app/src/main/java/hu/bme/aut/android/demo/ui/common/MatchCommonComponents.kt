package hu.bme.aut.android.demo.ui.common

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.demo.util.toDisplayDate

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
            text = "Térkép: $location",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

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
            text = "📅 $formattedDate",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}