package hu.bme.aut.android.demo.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.demo.R


@Composable
fun CommonFilterDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            // Itt biztosítjuk, hogy minden elem szépen egymás alatt, térközzel jelenjen meg
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                content()
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.done))
            }
        }
    )
}