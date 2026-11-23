package app.cineseat.ui.canvas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import app.cineseat.util.geometry.Viewport
import app.cineseat.util.geometry.ViewportLimits
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Holds the camera (pan/zoom) of the map. It is view state only: seat data and selection live in the ViewModel.
 * The Canvas reads [viewport] inside its draw block, so a gesture only redraws, it does not recompose the screen.
 */
@Stable
class ViewportController(private val scope: CoroutineScope) {
    var viewport by mutableStateOf(Viewport())
        private set
    var limits: ViewportLimits? by mutableStateOf(null)
        private set

    /** 0 → 1 while the "here it is" ring pulses around a located seat. */
    val pulse = Animatable(1f)
    private var animation: Job? = null

    /** Called when the canvas gets a (new) size: start from the default view for that size, e.g. after rotation. */
    fun onLimits(newLimits: ViewportLimits) {
        if (newLimits == limits) return
        limits = newLimits
        animation?.cancel()
        viewport = newLimits.default()
    }

    fun onGesture(centroid: Offset, pan: Offset, zoom: Float) {
        val l = limits ?: return
        animation?.cancel() // a finger on the map wins over a running animation
        viewport = l.clamp(viewport.zoomAndPan(centroid, pan, zoom, l.minScale, l.maxScale))
    }

    /** Back to the default position and scale. Selection and filter are not touched. */
    fun reset() {
        limits?.let { animateTo(it.default()) }
    }

    /** Moves and zooms so [seat] (world rect) is in the middle of the view, then pulses a ring around it. */
    fun focusOn(seat: Rect) {
        val l = limits ?: return
        animateTo(l.centredOn(seat.center, max(viewport.scale, l.fitScale * 2.5f)))
        scope.launch {
            repeat(2) {
                pulse.snapTo(0f)
                pulse.animateTo(1f, tween(700))
            }
        }
    }

    private fun animateTo(target: Viewport) {
        animation?.cancel()
        val start = viewport
        animation = scope.launch {
            animate(0f, 1f, animationSpec = tween(450, easing = FastOutSlowInEasing)) { t, _ ->
                viewport = Viewport.lerp(start, target, t)
            }
        }
    }
}

@Composable
fun rememberViewportController(): ViewportController {
    val scope = rememberCoroutineScope()
    return remember { ViewportController(scope) }
}
