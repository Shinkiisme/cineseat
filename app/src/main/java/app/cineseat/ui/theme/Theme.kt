package app.cineseat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Seat colours are shared by the Canvas and the legend, so they live here, in one place. */
object SeatColors {
    val Standard = Color(0xFF5B7BD5)
    val Vip = Color(0xFFE8B04B)
    val Couple = Color(0xFFB07CFF)
    val Selected = Color(0xFFFF4D5E)
    val Booked = Color(0xFF3A3F4B)
    val Blocked = Color(0xFF6B7080)
    val OnSelected = Color.White
}

val Background = Color(0xFF0E1016)
val Surface = Color(0xFF171A22)
val SurfaceHigh = Color(0xFF20242E)

private val CineColors = darkColorScheme(
    primary = SeatColors.Selected,
    onPrimary = Color.White,
    secondary = SeatColors.Vip,
    onSecondary = Color(0xFF231A05),
    background = Background,
    onBackground = Color(0xFFE9EAF0),
    surface = Surface,
    onSurface = Color(0xFFE9EAF0),
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = Color(0xFFA3A8B8),
    surfaceContainerLow = Surface,
    surfaceContainer = Surface,
    surfaceContainerHigh = SurfaceHigh,
    outline = Color(0xFF3A3F4B),
    outlineVariant = Color(0xFF2A2E38),
)

@Composable
fun CineSeatTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CineColors, content = content)
}
