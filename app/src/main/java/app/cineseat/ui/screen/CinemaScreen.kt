package app.cineseat.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.cineseat.data.repository.SeatRepository
import app.cineseat.ui.canvas.CinemaCanvas
import app.cineseat.ui.canvas.rememberViewportController
import app.cineseat.ui.components.ActiveFilterLine
import app.cineseat.ui.components.FilterChips
import app.cineseat.ui.components.LegendCard
import app.cineseat.ui.components.SelectionSheetContent
import app.cineseat.ui.components.SheetHandle
import app.cineseat.ui.viewmodel.CinemaViewModel
import app.cineseat.util.geometry.CinemaLayout
import kotlinx.coroutines.launch

/**
 * Lays out header + filters, the map with its floating controls, and the bottom sheet.
 * All seat state comes from [CinemaViewModel]; this screen only owns view state (camera, sheet, legend open).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaScreen(vm: CinemaViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val layout = remember(state.seats) { CinemaLayout(state.seats) }
    val controller = rememberViewportController()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val sheet = scaffoldState.bottomSheetState
    var legendOpen by rememberSaveable { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val handleHeight = 24.dp
    val peekContent = if (landscape) 56.dp else 104.dp
    val peekHeight = handleHeight + peekContent + navBottom
    // The expanded sheet never takes more than this, so part of the map always stays visible.
    val sheetMax = configuration.screenHeightDp.dp * (if (landscape) 0.62f else 0.6f)
    val peekPx = with(LocalDensity.current) { peekHeight.toPx() }
    val expanded = sheet.currentValue == SheetValue.Expanded

    fun collapseSheet() = scope.launch { sheet.partialExpand() }
    fun locate(id: String) {
        vm.inspect(id)
        collapseSheet()
        layout.rectOf(id)?.let(controller::focusOn)
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = peekHeight,
        sheetDragHandle = { SheetHandle() },
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(it, Modifier.padding(bottom = peekHeight)) },
        sheetContent = {
            Box(Modifier.navigationBarsPadding()) {
                SelectionSheetContent(
                    state = state,
                    expanded = expanded,
                    compact = landscape,
                    peekContentHeight = peekContent,
                    bottomInset = navBottom,
                    maxHeight = sheetMax,
                    onToggleExpanded = { scope.launch { if (expanded) sheet.partialExpand() else sheet.expand() } },
                    onLocate = ::locate,
                    onRemove = vm::removeSelection,
                    onClearAll = vm::clearSelection,
                    onConfirm = {
                        val count = vm.confirmBooking()
                        collapseSheet()
                        scope.launch { scaffoldState.snackbarHostState.showSnackbar("Đặt vé thành công $count ghế (giả lập, không gửi đi đâu)") }
                    },
                )
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
        ) {
            if (landscape) {
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Header(Modifier.padding(start = 16.dp))
                    FilterChips(state.filter, vm::toggleType, vm::toggleStatus, Modifier.weight(1f), PaddingValues(horizontal = 12.dp))
                }
            } else {
                Header(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp))
                FilterChips(state.filter, vm::toggleType, vm::toggleStatus)
            }
            if (!landscape) ActiveFilterLine(state.filter, state.matchCount, vm::clearFilter)

            Box(Modifier.weight(1f).fillMaxWidth()) {
                CinemaCanvas(state, layout, controller, bottomInset = peekPx, onSeatTap = vm::onSeatTapped, modifier = Modifier.fillMaxSize())

                if (landscape && state.filter.isActive) {
                    // Landscape has little height to spare, so the filter summary floats over the map instead of taking a row.
                    Surface(
                        Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = peekHeight + 8.dp).widthIn(max = 360.dp),
                        shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 4.dp,
                    ) { ActiveFilterLine(state.filter, state.matchCount, vm::clearFilter) }
                }

                Column(Modifier.align(Alignment.TopEnd).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmallFloatingActionButton(onClick = { legendOpen = !legendOpen }, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Icon(Icons.Outlined.Info, contentDescription = if (legendOpen) "Ẩn chú thích" else "Hiện chú thích")
                    }
                    SmallFloatingActionButton(onClick = controller::reset, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Icon(Icons.Filled.CenterFocusStrong, contentDescription = "Đặt lại góc nhìn")
                    }
                }
                // Fully qualified: inside this Box the ColumnScope overload would otherwise be picked.
                androidx.compose.animation.AnimatedVisibility(
                    legendOpen,
                    Modifier.align(Alignment.TopStart).padding(12.dp),
                    enter = fadeIn() + scaleIn(transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)),
                    exit = fadeOut() + scaleOut(transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)),
                ) { LegendCard() }

                if (state.filter.isActive && state.matchCount == 0) {
                    Surface(
                        Modifier.align(Alignment.Center).padding(24.dp),
                        shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 8.dp,
                    ) {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Không có ghế nào khớp bộ lọc", style = MaterialTheme.typography.titleSmall)
                            Text("Thử bỏ bớt điều kiện lọc.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = vm::clearFilter) { Text("Xóa lọc") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(SeatRepository.MOVIE, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(
            "${SeatRepository.CINEMA} · ${SeatRepository.HALL} · ${SeatRepository.SHOWTIME}",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}
