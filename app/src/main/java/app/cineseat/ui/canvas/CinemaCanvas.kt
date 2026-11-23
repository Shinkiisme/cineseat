package app.cineseat.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cineseat.data.model.SeatStatus
import app.cineseat.data.model.SeatType
import app.cineseat.ui.formatVnd
import app.cineseat.ui.state.CinemaUiState
import app.cineseat.ui.theme.SeatColors
import app.cineseat.util.geometry.CinemaLayout
import app.cineseat.util.geometry.ViewportLimits

fun SeatType.color(): Color = when (this) {
    SeatType.STANDARD -> SeatColors.Standard
    SeatType.VIP -> SeatColors.Vip
    SeatType.COUPLE -> SeatColors.Couple
}

/**
 * The whole hall on one Canvas: no composable per seat. Geometry is drawn in world units inside a single
 * translate+scale transform; text is drawn afterwards at screen positions so it stays readable at any zoom.
 * Taps are converted to world coordinates and hit-tested against the same rects that are drawn.
 *
 * @param bottomInset screen px at the bottom covered by the collapsed bottom sheet; the default view fits above it.
 */
@Composable
fun CinemaCanvas(
    state: CinemaUiState,
    layout: CinemaLayout,
    controller: ViewportController,
    bottomInset: Float,
    onSeatTap: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnSeatTap by rememberUpdatedState(onSeatTap)
    val textMeasurer = rememberTextMeasurer()
    // Everything that does not change per frame is built once here, never inside the draw block.
    val labels = remember(textMeasurer) { CanvasLabels(textMeasurer) }
    val strokes = remember { SeatStrokes() }
    val screenPath = remember(layout) {
        Path().apply {
            moveTo(layout.screenLeft, layout.screenY)
            quadraticTo(layout.seatAreaCenterX, layout.screenY - 34f, layout.screenRight, layout.screenY)
        }
    }
    // A beam that widens from the screen towards the first rows.
    val glowPath = remember(layout) {
        val inset = 40f
        Path().apply {
            moveTo(layout.screenLeft + inset, layout.screenY - 8f)
            quadraticTo(layout.seatAreaCenterX, layout.screenY - 40f, layout.screenRight - inset, layout.screenY - 8f)
            lineTo(layout.screenRight, CinemaLayout.ROWS_TOP - 30f)
            lineTo(layout.screenLeft, CinemaLayout.ROWS_TOP - 30f)
            close()
        }
    }
    val screenGlow = remember(layout) {
        Brush.verticalGradient(
            listOf(Color(0x40A9C4FF), Color.Transparent),
            startY = layout.screenY - 30f,
            endY = CinemaLayout.ROWS_TOP - 30f,
        )
    }

    val selectedRows = remember(state.selectedIds) { state.selectedSeats.mapTo(HashSet()) { it.row } }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val edge = with(LocalDensity.current) { 48.dp.toPx() }
    LaunchedEffect(canvasSize, bottomInset, layout) {
        if (canvasSize != IntSize.Zero) {
            val view = Rect(0f, 0f, canvasSize.width.toFloat(), (canvasSize.height - bottomInset).coerceAtLeast(canvasSize.height * 0.3f))
            controller.onLimits(ViewportLimits(layout.bounds, view, edge))
        }
    }

    Canvas(
        modifier
            .onSizeChanged { canvasSize = it }
            .semantics { contentDescription = "Sơ đồ phòng chiếu với ${layout.seats.size} ghế" }
            .pointerInput(layout) {
                detectTapGestures { tap ->
                    val world = controller.viewport.toWorld(tap)
                    currentOnSeatTap(layout.seats.getOrNull(layout.hitTest(world))?.id)
                }
            }
            // Inner modifier: sees moves first and consumes them once past touch slop, which cancels the tap above.
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ -> controller.onGesture(centroid, pan, zoom) }
            }
    ) {
        if (controller.limits == null) return@Canvas // first frame, before the size is known
        val v = controller.viewport
        val visibleWorld = Rect(v.toWorld(Offset.Zero), v.toWorld(Offset(size.width, size.height)))
        val inspected = state.inspectedId

        withTransform({
            translate(v.offset.x, v.offset.y)
            scale(v.scale, v.scale, pivot = Offset.Zero)
        }) {
            // Screen and the light it throws on the first rows.
            drawPath(glowPath, screenGlow)
            drawPath(screenPath, Color(0xFFDDE6FF), style = strokes.screen)
            // Aisles.
            for (aisle in layout.aisles) drawRoundRect(Color(0x0FFFFFFF), aisle.topLeft, aisle.size, CornerRadius(6f))

            for (i in layout.seats.indices) {
                val r = layout.seatRects[i]
                if (!r.overlaps(visibleWorld)) continue
                val seat = layout.seats[i]
                val status = state.statusOf(seat)
                val alpha = if (state.isVisible(seat)) 1f else 0.16f
                drawSeat(r, seat.type, status, alpha, strokes)
                if (seat.id == inspected) {
                    drawRoundRect(Color.White, r.topLeft - Offset(3.5f, 3.5f), Size(r.width + 7f, r.height + 7f), CornerRadius(9f), style = strokes.ring)
                    val t = controller.pulse.value
                    if (t < 1f) {
                        val grow = 4f + 14f * t
                        drawRoundRect(Color.White.copy(alpha = 1f - t), r.topLeft - Offset(grow, grow), Size(r.width + 2 * grow, r.height + 2 * grow), CornerRadius(8f + grow), style = strokes.ring)
                    }
                }
            }
        }

        // ---- Text, at screen positions ----
        val screenLabelPos = v.toScreen(Offset(layout.seatAreaCenterX, layout.screenY + 14f))
        drawCentered(labels.screen, screenLabelPos, Color(0xFF8F98B3))
        for ((type, y) in layout.sectionHeaders) {
            drawCentered(labels.sections.getValue(type), v.toScreen(Offset(layout.seatAreaCenterX, y)), type.color())
        }
        for ((index, rowAndY) in layout.rowCenters.withIndex()) {
            val (row, y) = rowAndY
            val label = labels.rows[index]
            val color = if (row in selectedRows) SeatColors.Selected else Color(0xFF8F98B3)
            drawCentered(label, v.toScreen(Offset(layout.leftLabelX, y)), color)
            drawCentered(label, v.toScreen(Offset(layout.rightLabelX, y)), color)
        }
        // Seat numbers only once seats are big enough to hold them.
        if (CinemaLayout.SEAT * v.scale >= 22.dp.toPx()) {
            for (i in layout.seats.indices) {
                val r = layout.seatRects[i]
                if (!r.overlaps(visibleWorld)) continue
                val seat = layout.seats[i]
                val status = state.statusOf(seat)
                if (status == SeatStatus.BOOKED || status == SeatStatus.BLOCKED) continue
                val color = if (status == SeatStatus.SELECTED) SeatColors.OnSelected else seat.type.color()
                val alpha = if (state.isVisible(seat)) 1f else 0.2f
                drawCentered(labels.numbers[seat.number - 1], v.toScreen(r.center + Offset(0f, 1.5f)), color, alpha)
            }
        }
    }
}

/** Text measured once and reused every frame. */
private class CanvasLabels(measurer: androidx.compose.ui.text.TextMeasurer) {
    private val small = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp)
    val screen: TextLayoutResult = measurer.measure("MÀN HÌNH", small)
    val rows: List<TextLayoutResult> = ('A'..'N').map { measurer.measure(it.toString(), small.copy(fontSize = 12.sp, letterSpacing = 0.sp)) }
    val numbers: List<TextLayoutResult> = (1..16).map { measurer.measure(it.toString(), TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold)) }
    val sections: Map<SeatType, TextLayoutResult> = SeatType.entries.associateWith {
        measurer.measure("${it.label.uppercase()}  ·  ${formatVnd(it.price)}", small)
    }
}

private class SeatStrokes {
    val outline = Stroke(width = 1.6f)
    val blocked = Stroke(width = 1.4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 2.5f)))
    val ring = Stroke(width = 2.2f)
    val screen = Stroke(width = 5f, cap = StrokeCap.Round)
}

private fun DrawScope.drawCentered(text: TextLayoutResult, center: Offset, color: Color, alpha: Float = 1f) {
    drawText(text, color = color, topLeft = Offset(center.x - text.size.width / 2f, center.y - text.size.height / 2f), alpha = alpha)
}

/**
 * One seat, in world units. Status is told apart by more than colour: selected is solid with a tick,
 * booked is grey with a cross, blocked is a dashed outline with a slash, available is an outline in its type colour.
 */
private fun DrawScope.drawSeat(r: Rect, type: SeatType, status: SeatStatus, alpha: Float, strokes: SeatStrokes) {
    val corner = CornerRadius(6f)
    val typeColor = type.color()
    when (status) {
        SeatStatus.AVAILABLE -> {
            drawRoundRect(typeColor, r.topLeft, r.size, corner, alpha = 0.22f * alpha)
            drawRoundRect(typeColor, r.topLeft, r.size, corner, style = strokes.outline, alpha = alpha)
        }
        SeatStatus.SELECTED -> {
            drawRoundRect(SeatColors.Selected, r.topLeft, r.size, corner, alpha = alpha)
        }
        SeatStatus.BOOKED -> {
            drawRoundRect(SeatColors.Booked, r.topLeft, r.size, corner, alpha = alpha)
            val c = r.center
            val color = Color(0xFF6A7080)
            drawLine(color, c + Offset(-5f, -5f), c + Offset(5f, 5f), 2f, StrokeCap.Round, alpha = alpha)
            drawLine(color, c + Offset(5f, -5f), c + Offset(-5f, 5f), 2f, StrokeCap.Round, alpha = alpha)
        }
        SeatStatus.BLOCKED -> {
            drawRoundRect(SeatColors.Blocked, r.topLeft, r.size, corner, alpha = 0.08f * alpha)
            drawRoundRect(SeatColors.Blocked, r.topLeft, r.size, corner, style = strokes.blocked, alpha = alpha)
            drawLine(SeatColors.Blocked, Offset(r.left + 5f, r.bottom - 5f), Offset(r.right - 5f, r.top + 5f), 1.6f, StrokeCap.Round, alpha = alpha)
        }
    }
    // Armrest bar: makes the shape read as a seat. Couple seats get a divider too.
    val barColor = if (status == SeatStatus.SELECTED) Color.White else typeColor
    if (status != SeatStatus.BLOCKED) {
        drawRoundRect(barColor, Offset(r.left + 5f, r.bottom - 5f), Size(r.width - 10f, 2.5f), CornerRadius(1.25f), alpha = (if (status == SeatStatus.BOOKED) 0.25f else 0.7f) * alpha)
    }
    if (type == SeatType.COUPLE && status != SeatStatus.BLOCKED) {
        drawLine(barColor, Offset(r.center.x, r.top + 5f), Offset(r.center.x, r.bottom - 9f), 1.2f, alpha = 0.35f * alpha)
    }
}
