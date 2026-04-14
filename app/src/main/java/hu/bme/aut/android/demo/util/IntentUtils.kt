package hu.bme.aut.android.demo.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import androidx.annotation.RequiresApi
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun addMatchToCalendar(context: Context, match: TeamMatch) {
    try {
        // Backend dátum konvertálása (Feltételezzük: YYYY-MM-DDTHH:mm)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val localDateTime = LocalDateTime.parse(match.matchDate, formatter)
        val startMillis = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Egy átlagos meccs 2.5 óra
        val endMillis = startMillis + (2 * 60 * 60 * 1000) + (30 * 60 * 1000)

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "Bajnoki meccs: ${match.homeTeamName} vs ${match.guestTeamName}")
            putExtra(CalendarContract.Events.EVENT_LOCATION, match.location)
            putExtra(CalendarContract.Events.DESCRIPTION, "${match.roundNumber}. Forduló mérkőzés a Pilula Bajnokságban.")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            // Visszatérés az appba mentés után
            putExtra(CalendarContract.Events.HAS_ALARM, 1)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}