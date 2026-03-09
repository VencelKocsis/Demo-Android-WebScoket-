package hu.bme.aut.android.demo.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ISO-8601 formátumú Stringet (pl. 2026-03-13T18:30) alakít át
 * olvasható magyar formátumra (2026.03.13. 18:30).
 */
@RequiresApi(Build.VERSION_CODES.O)
fun String?.toDisplayDate(): String {
    if (this.isNullOrBlank()) return ""

    return try {
        // A LocalDateTime.parse alapból kezeli az ISO formátumot (T betűvel)
        val dateTime = LocalDateTime.parse(this)
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm", Locale.getDefault())
        dateTime.format(formatter)
    } catch (e: Exception) {
        // Ha valamiért nem tudná parzolni, adjuk vissza az eredeti szöveget,
        // hogy ne legyen adatvesztés a UI-on
        this
    }
}