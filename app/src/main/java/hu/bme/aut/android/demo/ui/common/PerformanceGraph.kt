package hu.bme.aut.android.demo.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.demo.R

@Composable
fun PerformanceGraph(data: List<Float>, color: Color = MaterialTheme.colorScheme.primary) {

    // --- 1. VÉDŐVONAL (Guard Clause) ---
    // Ha nincs elég adat (minimum 2 pont kell egy vonalhoz), kilépünk és egy szöveget mutatunk.
    if (data.size < 2) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_data_for_graph), color = Color.Gray, style = MaterialTheme.typography.labelMedium)
        }
        return
    }

    // --- 2. SKÁLÁZÁS ÉS MATEMATIKA ---
    // Megkeressük a legkisebb és legnagyobb értéket a listában (alapértelmezettként 1000f, ha valamiért null lenne).
    val max = data.maxOrNull() ?: 1000f
    val min = data.minOrNull() ?: 1000f

    // Kiszámoljuk a különbséget (range). A coerceAtLeast(10f) biztosítja, hogy ha
    // minden adat megegyezik (pl. végig 1200 pontja van), a grafikon akkor se legyen egy lapos vonal középen,
    // és elkerüljük a nullával való osztást a későbbi matekban.
    val range = (max - min).coerceAtLeast(10f)

    // Adunk neki 10% "levegőt" (padding) alul-felül, hogy a vonal és a pöttyök
    // soha ne lógjanak ki a Canvas kereteiből, és ne vágja le őket a képernyő széle.
    val padding = range * 0.1f
    val yMax = max + padding
    val yMin = min - padding
    val yRange = yMax - yMin // Ez a végleges, kiterjesztett értéktartományunk

    // --- 3. A VÁSZON (Canvas) ---
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
        .padding(vertical = 8.dp)) {

        // A Canvas ad nekünk egy 'size' objektumot, ebből tudjuk a pontos pixel méreteket.
        val width = size.width
        val height = size.height

        // Kiszámoljuk, mekkora távolság (pixel) kell két pont közé az X tengelyen.
        val stepX = width / (data.size - 1)

        // Két útvonalat építünk fel:
        // 'path': ez lesz maga a vastag, színes vonal.
        val path = Path()
        // 'fillPath': ez egy zárt alakzat lesz, amit átmenettel (gradienssel) színezünk ki alul.
        val fillPath = Path()

        // Eltároljuk a koordinátákat, hogy a végén tudjunk rájuk pöttyöket rajzolni.
        val points = mutableListOf<Offset>()

        // --- 4. KOORDINÁTÁK KISZÁMÍTÁSA ---
        data.forEachIndexed { i, value ->
            // X koordináta: az index szorozva a lépésközzel (balról jobbra haladunk).
            val x = i * stepX

            // Y koordináta: A legtrükkösebb rész!
            // A Canvas-on az Y=0 a képernyő TETEJÉT jelenti, lefelé nő az érték.
            // Ezért meg kell fordítanunk az arányt (1 - arány), hogy a magasabb pontszám feljebb kerüljön.
            // Képlet: Magasság - (aktuális érték pozíciója a tartományon belül %-ban) * Magasság
            val y = height - ((value - yMin) / yRange) * height

            points.add(Offset(x, y))

            if (i == 0) {
                // Az első pontnál az ecsetet oda kell "mozgatni" (moveTo)
                path.moveTo(x, y)
                // A kitöltésnél a képernyő aljáról indulunk fel a legelső ponthoz
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                // A többi pontnál csak vonalat húzunk (lineTo) az előző ponttól
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // A kitöltő alakzatot lehúzzuk a jobb alsó sarokba, majd lezárjuk (close),
        // így egy tökéletes, lezárt poligont kapunk a vonal alatt.
        fillPath.lineTo(width, height)
        fillPath.close()

        // --- 5. RAJZOLÁS (A rétegek egymásra pakolása) ---

        // 1. réteg (Legalul): Az átmenetes kitöltés (fent a szín 40%-os átlátszósággal, lent teljesen átlátszó).
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.4f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // 2. réteg (Középen): A vastag grafikon vonal.
        // A StrokeCap.Round és StrokeJoin.Round teszi széppé, lekerekítetté a csatlakozásokat és a végeket.
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 3. réteg (Legfelül): A mérkőzéseket jelző pöttyök.
        points.forEach { point ->
            // Rajzolunk egy nagyobb, színes kört...
            drawCircle(color = color, radius = 6f, center = point)
            // ...és a közepére egy kisebb fehéret, így "lyukas" vagy "célkereszt" hatást érünk el.
            drawCircle(color = Color.White, radius = 3f, center = point)
        }
    }
}