package app.cineseat.util.geometry

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.max
import kotlin.math.min

/**
 * World ↔ screen mapping: screen = world * scale + offset, world = (screen - offset) / scale.
 * The Canvas draws with translate(offset) then scale(scale) about (0, 0), which is the same formula.
 */
data class Viewport(val scale: Float = 1f, val offset: Offset = Offset.Zero) {
    fun toScreen(world: Offset): Offset = world * scale + offset
    fun toWorld(screen: Offset): Offset = (screen - offset) / scale
    fun toScreen(world: Rect): Rect = Rect(toScreen(world.topLeft), toScreen(world.bottomRight))

    /**
     * One gesture step. [centroid] is where the fingers are now, [pan] how far they moved since the last step,
     * [zoom] the change in spread. The world point that was under the fingers (centroid - pan) ends up under
     * them again at the new scale, so zooming stays centred on the fingers and pan and zoom can happen together.
     */
    fun zoomAndPan(centroid: Offset, pan: Offset, zoom: Float, minScale: Float, maxScale: Float): Viewport {
        val newScale = (scale * zoom).coerceIn(minScale, maxScale)
        val anchor = toWorld(centroid - pan)
        return Viewport(newScale, centroid - anchor * newScale)
    }

    companion object {
        fun lerp(a: Viewport, b: Viewport, t: Float) = Viewport(
            a.scale + (b.scale - a.scale) * t,
            a.offset + (b.offset - a.offset) * t,
        )
    }
}

/**
 * Limits for a [world] drawn inside the visible part of the canvas, [view] (screen px, the area not covered
 * by the collapsed bottom sheet). [edge] is how much empty space may show past the map's edge when zoomed in.
 */
data class ViewportLimits(val world: Rect, val view: Rect, val edge: Float) {
    /** The scale at which the whole map just fits: the default view. */
    val fitScale = min(view.width / world.width, view.height / world.height)
    val minScale = fitScale * 0.8f
    val maxScale = fitScale * 6f

    fun default(): Viewport = centredOn(world.center, fitScale)

    fun centredOn(worldPoint: Offset, scale: Float): Viewport {
        val s = scale.coerceIn(minScale, maxScale)
        return clamp(Viewport(s, view.center - worldPoint * s))
    }

    /**
     * Keeps the map on screen. Per axis: when the map is smaller than the view it must stay fully inside;
     * when larger, its edges may not come further in than [edge], so the map always covers the view.
     */
    fun clamp(v: Viewport): Viewport {
        val s = v.scale.coerceIn(minScale, maxScale)
        return Viewport(
            s,
            Offset(
                clampAxis(v.offset.x, world.left * s, world.right * s, view.left, view.right),
                clampAxis(v.offset.y, world.top * s, world.bottom * s, view.top, view.bottom),
            ),
        )
    }

    private fun clampAxis(offset: Float, start: Float, end: Float, viewStart: Float, viewEnd: Float): Float {
        val lo = min(viewEnd - edge - end, viewStart - start)
        val hi = max(viewStart + edge - start, viewEnd - end)
        return offset.coerceIn(lo, hi)
    }
}
