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
    if (data.size < 2) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_data_for_graph), color = Color.Gray, style = MaterialTheme.typography.labelMedium)
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

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
        .padding(vertical = 8.dp)) {
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