package app.cineseat

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import app.cineseat.data.model.SeatType
import app.cineseat.data.repository.SeatRepository
import app.cineseat.util.geometry.CinemaLayout
import app.cineseat.util.geometry.Viewport
import app.cineseat.util.geometry.ViewportLimits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometryTest {
    private val layout = CinemaLayout(SeatRepository.seats)
    private val seats = layout.seats
    private fun near(a: Offset, b: Offset, eps: Float = 1e-3f) =
        assertTrue("$a != $b", (a - b).getDistance() < eps)

    // ---- World <-> screen ----

    @Test fun toWorldUndoesToScreen() {
        val v = Viewport(2.37f, Offset(-412.5f, 88f))
        val p = Offset(123.4f, 567.8f)
        near(p, v.toWorld(v.toScreen(p)))
        near(Offset(123.4f * 2.37f - 412.5f, 567.8f * 2.37f + 88f), v.toScreen(p))
    }

    @Test fun zoomKeepsTheWorldPointUnderTheFingers() {
        var v = Viewport(1.5f, Offset(30f, -40f))
        for (centroid in listOf(Offset(100f, 900f), Offset(540f, 300f), Offset(1000f, 1500f))) {
            val before = v.toWorld(centroid)
            v = v.zoomAndPan(centroid, Offset.Zero, 1.8f, 0.1f, 100f)
            near(before, v.toWorld(centroid))
        }
    }

    @Test fun panAndZoomTogetherCarryThePointWithTheFingers() {
        val v = Viewport(2f, Offset(10f, 20f))
        val from = Offset(300f, 400f)
        val pan = Offset(-25f, 60f)
        val worldUnderFingers = v.toWorld(from)
        // `from` is the previous centroid, as detectTransformGestures reports it.
        val next = v.zoomAndPan(from, pan, 1.25f, 0.1f, 100f)
        assertEquals(2.5f, next.scale, 1e-5f)
        near(from + pan, next.toScreen(worldUnderFingers))
    }

    @Test fun zoomStopsAtTheLimitsAndStaysCentred() {
        val v = Viewport(1f, Offset.Zero)
        val c = Offset(200f, 200f)
        val maxed = v.zoomAndPan(c, Offset.Zero, 50f, 0.5f, 4f)
        assertEquals(4f, maxed.scale, 0f)
        near(v.toWorld(c), maxed.toWorld(c))
        assertEquals(0.5f, v.zoomAndPan(c, Offset.Zero, 0.01f, 0.5f, 4f).scale, 0f)
    }

    // ---- Pan limits ----

    private val portrait = ViewportLimits(layout.bounds, Rect(0f, 0f, 1080f, 1700f), edge = 80f)
    private val landscape = ViewportLimits(layout.bounds, Rect(0f, 0f, 2400f, 600f), edge = 80f)

    @Test fun defaultViewShowsTheWholeMapCentred() {
        for (limits in listOf(portrait, landscape)) {
            val v = limits.default()
            val onScreen = v.toScreen(layout.bounds)
            assertTrue(onScreen.left >= -0.5f && onScreen.right <= limits.view.right + 0.5f)
            assertTrue(onScreen.top >= -0.5f && onScreen.bottom <= limits.view.bottom + 0.5f)
            near(limits.view.center, onScreen.center, 0.5f)
        }
    }

    @Test fun panCannotThrowTheMapOffScreen() {
        for (limits in listOf(portrait, landscape)) {
            for (scale in listOf(limits.minScale, limits.fitScale, limits.fitScale * 3, limits.maxScale)) {
                for (far in listOf(Offset(1e6f, 1e6f), Offset(-1e6f, -1e6f), Offset(1e6f, -1e6f))) {
                    val map = limits.clamp(Viewport(scale, far)).toScreen(layout.bounds)
                    val view = limits.view
                    // Some of the map is always visible...
                    assertTrue(map.overlaps(view))
                    // ...and when it is bigger than the view, no more than `edge` of empty space shows.
                    if (map.width >= view.width) assertTrue(map.left <= view.left + 80.5f && map.right >= view.right - 80.5f)
                    else assertTrue(map.left >= view.left - 0.5f && map.right <= view.right + 0.5f)
                    if (map.height >= view.height) assertTrue(map.top <= view.top + 80.5f && map.bottom >= view.bottom - 80.5f)
                    else assertTrue(map.top >= view.top - 0.5f && map.bottom <= view.bottom + 0.5f)
                }
            }
        }
    }

    @Test fun clampLeavesALegalViewportAlone() {
        val v = portrait.default()
        assertEquals(v, portrait.clamp(v))
    }

    // ---- Layout ----

    @Test fun seatsDoNotOverlapAndCoupleSeatsAreDoubleWidth() {
        val rects = layout.seatRects
        for (i in rects.indices) for (j in i + 1 until rects.size) assertTrue("${seats[i].id}/${seats[j].id}", !rects[i].overlaps(rects[j]))
        val single = rects[seats.indexOfFirst { it.type == SeatType.STANDARD }]
        val couple = rects[seats.indexOfFirst { it.type == SeatType.COUPLE }]
        assertEquals(single.width * 2 + CinemaLayout.GAP, couple.width, 1e-4f)
        assertTrue(layout.seatRects.all { layout.bounds.contains(it.topLeft) && layout.bounds.contains(it.bottomRight) })
    }

    // ---- Hit-testing ----

    @Test fun everySeatIsHitAtItsCentreAndJustInsideEachEdge() {
        for ((i, r) in layout.seatRects.withIndex()) {
            val e = 0.01f
            for (p in listOf(r.center, Offset(r.left + e, r.center.y), Offset(r.right - e, r.center.y), Offset(r.center.x, r.top + e), Offset(r.center.x, r.bottom - e))) {
                assertEquals(seats[i].id, seats.getOrNull(layout.hitTest(p))?.id)
            }
        }
    }

    @Test fun tapsBetweenNeighboursPickTheNearerOneAndAisleTapsPickNothing() {
        val a = seats.indexOfFirst { it.id == "E5" }
        val b = seats.indexOfFirst { it.id == "E6" }
        val ra = layout.seatRects[a]
        val rb = layout.seatRects[b]
        val mid = (ra.right + rb.left) / 2
        assertEquals(a, layout.hitTest(Offset(mid - 0.1f, ra.center.y)))
        assertEquals(b, layout.hitTest(Offset(mid + 0.1f, ra.center.y)))
        // The middle of an aisle, the screen area and the gap between sections hit no seat.
        val aisle = layout.aisles[0]
        assertEquals(-1, layout.hitTest(Offset(aisle.center.x, ra.center.y)))
        assertEquals(-1, layout.hitTest(Offset(layout.seatAreaCenterX, layout.screenY)))
        assertEquals(-1, layout.hitTest(Offset(layout.seatAreaCenterX, layout.sectionHeaders[1].second)))
    }

    @Test fun hitTestIsRightAfterPanAndZoom() {
        // A tap on a seat's screen position must hit that seat at any pan/zoom.
        val views = listOf(
            portrait.default(),
            portrait.clamp(portrait.default().zoomAndPan(Offset(700f, 1200f), Offset(-150f, 90f), 3.2f, portrait.minScale, portrait.maxScale)),
            landscape.clamp(landscape.default().zoomAndPan(Offset(300f, 100f), Offset(400f, 0f), 2.5f, landscape.minScale, landscape.maxScale)),
            Viewport(portrait.minScale, Offset(55f, 70f)),
        )
        for (v in views) for ((i, r) in layout.seatRects.withIndex()) {
            for (worldPoint in listOf(r.center, Offset(r.left + 0.5f, r.top + 0.5f), Offset(r.right - 0.5f, r.bottom - 0.5f))) {
                val tap = v.toScreen(worldPoint)
                assertEquals("seat ${seats[i].id} at $v", i, layout.hitTest(v.toWorld(tap)))
            }
        }
    }

    @Test fun coupleSeatIsHitAcrossItsWholeWidth() {
        val i = seats.indexOfFirst { it.id == "M3" }
        val r = layout.seatRects[i]
        // Where the gap between two single seats would be, a couple seat is still solid.
        for (x in listOf(r.left + 1f, r.center.x, r.right - 1f)) assertEquals(i, layout.hitTest(Offset(x, r.center.y)))
    }
}
