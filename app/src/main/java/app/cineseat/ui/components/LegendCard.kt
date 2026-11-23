package app.cineseat.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import app.cineseat.data.model.SeatStatus
import app.cineseat.data.model.SeatType
import app.cineseat.ui.canvas.SeatStrokes
import app.cineseat.ui.canvas.drawSeat
import app.cineseat.ui.formatVnd

/** Explains every colour and mark on the map. Icons are drawn by the same function as the map, so they always match. */
@Composable
fun LegendCard(modifier: Modifier = Modifier) {
    Surface(modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 3.dp, shadowElevation = 6.dp) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LegendTitle("Loại ghế")
                SeatType.entries.forEach { LegendRow(it, SeatStatus.AVAILABLE, "${it.label} · ${formatVnd(it.price)}") }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LegendTitle("Trạng thái")
                SeatStatus.entries.forEach { LegendRow(SeatType.STANDARD, it, it.label) }
            }
        }
    }
}

@Composable
private fun LegendTitle(text: String) =
    Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
private fun LegendRow(type: SeatType, status: SeatStatus, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SeatIcon(type, status)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SeatIcon(type: SeatType, status: SeatStatus, modifier: Modifier = Modifier) {
    val strokes = remember { SeatStrokes() }
    Canvas(modifier.size(20.dp)) {
        val s = size.width / 28f
        withTransform({ scale(s, s, pivot = Offset.Zero) }) { drawSeat(Rect(0f, 0f, 28f, 28f), type, status, 1f, strokes) }
    }
}
