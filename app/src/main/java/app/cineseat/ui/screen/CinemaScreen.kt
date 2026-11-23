package app.cineseat.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.cineseat.ui.canvas.CinemaCanvas
import app.cineseat.ui.canvas.rememberViewportController
import app.cineseat.ui.viewmodel.CinemaViewModel
import app.cineseat.util.geometry.CinemaLayout

@Composable
fun CinemaScreen(vm: CinemaViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val layout = remember(state.seats) { CinemaLayout(state.seats) }
    val controller = rememberViewportController()
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        CinemaCanvas(state, layout, controller, bottomInset = 0f, onSeatTap = vm::onSeatTapped, modifier = Modifier.fillMaxSize())
        SmallFloatingActionButton(onClick = controller::reset, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            Icon(Icons.Filled.CenterFocusStrong, contentDescription = "Đặt lại góc nhìn")
        }
    }
}
