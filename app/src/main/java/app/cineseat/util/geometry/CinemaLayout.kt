package app.cineseat.util.geometry

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import app.cineseat.data.model.Seat
import app.cineseat.data.model.SeatType

/**
 * Where everything sits in world coordinates (1 unit = 1 px at scale 1). Built once from the seat data;
 * the Canvas draws these rects and the hit tester tests against the very same rects.
 *
 * Columns 0..15 form three blocks (4 | 8 | 4) split by two aisles. Each seat type is its own section with a
 * header gap above it. A couple seat spans two columns, so it is exactly as wide as two single seats plus the gap between them.
 */
class CinemaLayout(val seats: List<Seat>) {
    /** seatRects[i] is the rect of seats[i]. */
    val seatRects: List<Rect>
    val bounds: Rect
    val screenLeft = SEAT_LEFT
    val screenRight = SEAT_LEFT + SEAT_AREA_WIDTH
    val screenY = 56f
    val rowCenters: List<Pair<Char, Float>>
    val sectionHeaders: List<Pair<SeatType, Float>>
    val aisles: List<Rect>
    val leftLabelX = MARGIN + LABEL / 2
    val rightLabelX = SEAT_LEFT + SEAT_AREA_WIDTH + LABEL / 2
    val seatAreaCenterX = SEAT_LEFT + SEAT_AREA_WIDTH / 2

    init {
        val rows = seats.map { it.row }.distinct()
        val sectionOf = rows.associateWith { row -> seats.first { it.row == row }.type }
        val rowTop = HashMap<Char, Float>()
        var y = ROWS_TOP
        var lastType: SeatType? = null
        val headers = ArrayList<Pair<SeatType, Float>>()
        for (row in rows) {
            val type = sectionOf.getValue(row)
            if (type != lastType) {
                if (lastType != null) y += SECTION_GAP
                headers += type to (y - 22f)
                lastType = type
            }
            rowTop[row] = y
            y += ROW_PITCH
        }
        seatRects = seats.map { seat ->
            val left = columnX(seat.column)
            val right = columnX(seat.column + seat.span - 1) + SEAT
            val top = rowTop.getValue(seat.row)
            Rect(left, top, right, top + SEAT)
        }
        rowCenters = rows.map { it to rowTop.getValue(it) + SEAT / 2 }
        sectionHeaders = headers
        val seatsBottom = y - ROW_PITCH + SEAT
        bounds = Rect(0f, 0f, SEAT_LEFT + SEAT_AREA_WIDTH + LABEL + MARGIN, seatsBottom + MARGIN + 8f)
        aisles = listOf(3, 11).map { col ->
            Rect(columnX(col) + SEAT, ROWS_TOP - 6f, columnX(col + 1), seatsBottom + 6f)
        }
    }

    fun rectOf(seatId: String): Rect? = seats.indexOfFirst { it.id == seatId }.takeIf { it >= 0 }?.let(seatRects::get)

    /** Index of the seat under [world], or -1. See [SeatHitTester]. */
    fun hitTest(world: Offset): Int = SeatHitTester.hitTest(seatRects, world, TOUCH_TOLERANCE)

    companion object {
        const val SEAT = 28f
        const val GAP = 6f
        const val PITCH = SEAT + GAP
        const val ROW_PITCH = 38f
        const val AISLE = 30f
        const val LABEL = 36f
        const val MARGIN = 24f
        const val SECTION_GAP = 40f
        const val ROWS_TOP = 132f
        const val SEAT_LEFT = MARGIN + LABEL
        const val SEAT_AREA_WIDTH = 16 * PITCH - GAP + 2 * AISLE

        /** Half the gap between neighbours: hitboxes grow up to the midline and never overlap. */
        const val TOUCH_TOLERANCE = GAP / 2

        /** Left edge of column 0..15; blocks of 4 | 8 | 4 are pushed apart by an aisle. */
        fun columnX(column: Int): Float {
            val block = when {
                column < 4 -> 0
                column < 12 -> 1
                else -> 2
            }
            return SEAT_LEFT + column * PITCH + block * AISLE
        }
    }
}
