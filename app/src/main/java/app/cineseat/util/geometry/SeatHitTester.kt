package app.cineseat.util.geometry

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

/**
 * Hit-testing in world coordinates. The caller converts the screen tap with [Viewport.toWorld] first, so the
 * result does not depend on pan or zoom. Each rect is grown by [tolerance] on every side to make edge taps
 * easier; with tolerance at most half the gap the grown rects never overlap, and if they ever did the seat
 * whose centre is nearest wins.
 */
object SeatHitTester {
    fun hitTest(rects: List<Rect>, world: Offset, tolerance: Float): Int {
        var best = -1
        var bestDistance = Float.MAX_VALUE
        for (i in rects.indices) {
            val r = rects[i]
            if (world.x < r.left - tolerance || world.x > r.right + tolerance ||
                world.y < r.top - tolerance || world.y > r.bottom + tolerance
            ) continue
            val d = (world - r.center).getDistanceSquared()
            if (d < bestDistance) {
                best = i
                bestDistance = d
            }
        }
        return best
    }
}
