package app.cineseat

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import app.cineseat.data.repository.SeatRepository
import app.cineseat.ui.canvas.CinemaCanvas
import app.cineseat.ui.canvas.ViewportController
import app.cineseat.ui.canvas.rememberViewportController
import app.cineseat.ui.state.CinemaUiState
import app.cineseat.util.geometry.CinemaLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Drives the real Canvas with injected touch events: two-finger pinch, one-finger drag, taps.
 * Checks that zoom stays centred on the fingers, pan is bounded, and taps hit the right seat after both.
 */
class CanvasGestureTest {
    @get:Rule val rule = createComposeRule()

    private val layout = CinemaLayout(SeatRepository.seats)
    private lateinit var controller: ViewportController
    private var tapped: String? = "none"

    @Before fun setUp() {
        rule.setContent {
            controller = rememberViewportController()
            CinemaCanvas(
                CinemaUiState(SeatRepository.seats), layout, controller, bottomInset = 0f,
                onSeatTap = { tapped = it }, modifier = Modifier.fillMaxSize().testTag("map"),
            )
        }
        rule.waitForIdle()
    }

    private fun screenOf(seatId: String): Offset = controller.viewport.toScreen(layout.rectOf(seatId)!!.center)

    private fun tapAt(p: Offset) {
        tapped = "none"
        rule.onNodeWithTag("map").performTouchInput { click(p) }
        rule.waitForIdle()
    }

    @Test fun tapHitsTheSeatAtDefaultView() {
        for (id in listOf("A1", "E8", "H16", "L9", "M1", "N8")) {
            tapAt(screenOf(id))
            assertEquals(id, tapped)
        }
        // The middle of an aisle is empty.
        val aisle = layout.aisles[0]
        tapAt(controller.viewport.toScreen(Offset(aisle.center.x, layout.rectOf("E1")!!.center.y)))
        assertEquals(null, tapped)
    }

    @Test fun pinchZoomsAroundTheFingersThenTapsStillHit() {
        val before = controller.viewport
        // Fingers on J6's column at the view's vertical centre: the map is centred there, so the pan limits
        // do not need to move it while it grows, and any drift would come from the zoom maths alone.
        val centre = Offset(screenOf("J6").x, controller.limits!!.view.center.y)
        val worldUnderFingers = before.toWorld(centre)
        rule.onNodeWithTag("map").performTouchInput {
            pinch(centre - Offset(40f, 0f), centre - Offset(260f, 0f), centre + Offset(40f, 0f), centre + Offset(260f, 0f), durationMillis = 600)
        }
        rule.waitForIdle()
        val after = controller.viewport
        assertTrue("scale should grow: ${before.scale} -> ${after.scale}", after.scale > before.scale * 2f)
        val drift = (after.toWorld(centre) - worldUnderFingers).getDistance()
        assertTrue("zoom drifted $drift world units off the fingers", drift < 1f)

        // Tap every seat whose centre is now on screen (zoomed in, that is a dozen or so neighbours).
        val view = controller.limits!!.view
        val onScreen = layout.seats.map { it.id }.filter { val p = screenOf(it); p.x in 0f..view.right && p.y in 0f..view.bottom }
        assertTrue("too few seats on screen: $onScreen", onScreen.size >= 3)
        for (id in onScreen) {
            val p = screenOf(id)
            tapAt(p)
            assertEquals("tap at $p, viewport ${controller.viewport}", id, tapped)
        }
    }

    @Test fun panIsBoundedAndTapsStillHitAfterPanAndZoom() {
        val l = controller.limits!!
        rule.onNodeWithTag("map").performTouchInput {
            pinch(center - Offset(30f, 0f), center - Offset(300f, 0f), center + Offset(30f, 0f), center + Offset(300f, 0f), durationMillis = 500)
        }
        rule.waitForIdle()
        assertTrue("pinch did not zoom: ${controller.viewport.scale}", controller.viewport.scale > l.fitScale * 2f)
        // Drag far past the edge, several times.
        repeat(4) { rule.onNodeWithTag("map").performTouchInput { swipe(centerLeft + Offset(50f, 0f), centerRight, 300) } }
        rule.onNodeWithTag("map").performTouchInput { swipe(topCenter + Offset(0f, 50f), bottomCenter, 300) }
        rule.waitForIdle()
        val v = controller.viewport
        val map = v.toScreen(layout.bounds)
        assertTrue("scale lost while panning: ${v.scale}", v.scale > l.fitScale * 2f)
        // Dragged right and down as far as possible: the map's top-left corner stops `edge` px inside the view.
        assertEquals(l.view.left + l.edge, map.left, 1f)
        assertEquals(l.view.top + l.edge, map.top, 1f)
        assertTrue(map.overlaps(l.view))
        // After pan+zoom, a top-left seat is in view; it and its neighbour must be told apart.
        for (id in listOf("A3", "A4", "B3")) {
            val p = screenOf(id)
            if (p.x in 0f..l.view.right && p.y in 0f..l.view.bottom) {
                tapAt(p)
                assertEquals(id, tapped)
            }
        }
    }

    @Test fun zoomStopsAtMaxAndResetGoesBack() {
        val l = controller.limits!!
        repeat(4) {
            rule.onNodeWithTag("map").performTouchInput {
                pinch(center - Offset(20f, 0f), center - Offset(400f, 0f), center + Offset(20f, 0f), center + Offset(400f, 0f), durationMillis = 300)
            }
        }
        rule.waitForIdle()
        assertEquals(l.maxScale, controller.viewport.scale, 1e-3f)
        rule.runOnIdle { controller.reset() }
        rule.mainClock.advanceTimeBy(1000)
        rule.waitForIdle()
        val back = controller.viewport
        assertEquals(l.default().scale, back.scale, 1e-4f)
        assertTrue((l.default().offset - back.offset).getDistance() < 0.5f)
    }
}
