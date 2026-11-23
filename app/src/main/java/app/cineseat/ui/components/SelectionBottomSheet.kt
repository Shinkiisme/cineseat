package app.cineseat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.cineseat.data.model.Seat
import app.cineseat.data.model.SeatStatus
import app.cineseat.ui.canvas.color
import app.cineseat.ui.displayName
import app.cineseat.ui.formatVnd
import app.cineseat.ui.state.CinemaUiState
import app.cineseat.ui.theme.SeatColors

/** The small handle at the top of the sheet; shorter than Material's default so the peek leaves more map visible. */
@Composable
fun SheetHandle() {
    Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(width = 36.dp, height = 4.dp).background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)))
    }
}

/**
 * Sheet body. The top part ([peekContentHeight] tall) is what shows when collapsed: count, total and the seat
 * being looked at. Below it, when expanded: the scrollable list of picked seats, the total and the mock checkout.
 */
@Composable
fun SelectionSheetContent(
    state: CinemaUiState,
    expanded: Boolean,
    compact: Boolean,
    peekContentHeight: Dp,
    bottomInset: Dp,
    maxHeight: Dp,
    onToggleExpanded: () -> Unit,
    onLocate: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClearAll: () -> Unit,
    onConfirm: () -> Unit,
) {
    val selected = state.selectedSeats
    Column(Modifier.fillMaxWidth().heightIn(max = maxHeight)) {
        // ---- Collapsed summary ----
        Column(Modifier.height(peekContentHeight).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (selected.isEmpty()) "Chưa chọn ghế" else "${selected.size} ghế đã chọn",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        if (selected.isEmpty()) "Chạm vào ghế trống trên sơ đồ để chọn" else selected.joinToString(", ") { it.id },
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
                if (compact) InspectedSeatLine(state, Modifier.weight(1f).padding(horizontal = 12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatVnd(state.total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SeatColors.Vip)
                    Text("Tổng tạm tính", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                        contentDescription = if (expanded) "Thu gọn" else "Mở rộng danh sách ghế",
                    )
                }
            }
            if (!compact) InspectedSeatLine(state)
        }
        // The collapsed sheet sits above the navigation bar; this keeps the list from peeking out behind it.
        Spacer(Modifier.height(bottomInset))

        // ---- Expanded part ----
        HorizontalDivider()
        if (selected.isEmpty()) {
            Text(
                "Danh sách trống. Ghế bạn chọn trên sơ đồ sẽ hiện ở đây.",
                Modifier.padding(20.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val hidden = selected.count { !state.isVisible(it) }
            if (hidden > 0) {
                Text(
                    "$hidden ghế đang chọn bị làm mờ bởi bộ lọc (vẫn được giữ trong danh sách).",
                    Modifier.padding(horizontal = 20.dp, vertical = 8.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LazyColumn(Modifier.weight(1f, fill = false)) {
                items(selected, key = { it.id }) { seat ->
                    SelectedSeatRow(seat, dimmed = !state.isVisible(seat), focused = seat.id == state.inspectedId, onLocate = { onLocate(seat.id) }, onRemove = { onRemove(seat.id) })
                }
            }
        }
        HorizontalDivider()
        Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tổng cộng", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatVnd(state.total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onClearAll, enabled = selected.isNotEmpty()) { Text("Bỏ chọn hết") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onConfirm, enabled = selected.isNotEmpty()) { Text("Đặt vé") }
        }
    }
}

/** The seat last tapped, with its status and, for seats that cannot be booked, the reason. */
@Composable
private fun InspectedSeatLine(state: CinemaUiState, modifier: Modifier = Modifier) {
    val seat = state.inspectedSeat
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (seat == null) {
                Text("Chạm một ghế để xem chi tiết", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            } else {
                val status = state.statusOf(seat)
                SeatIcon(seat.type, status)
                Spacer(Modifier.width(8.dp))
                val detail = when (status) {
                    SeatStatus.BOOKED, SeatStatus.BLOCKED -> "${status.label} · ${if (seat.id in state.bookedIds) "bạn vừa đặt ghế này" else seat.note.orEmpty()}"
                    SeatStatus.SELECTED -> "Đang chọn · chạm lần nữa để bỏ"
                    SeatStatus.AVAILABLE -> if (state.isVisible(seat)) "Còn trống · chạm để chọn" else "Còn trống · đang bị bộ lọc làm mờ"
                }
                Text(
                    "${seat.displayName()} · ${seat.type.label} · ${formatVnd(seat.type.price)} — $detail",
                    style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SelectedSeatRow(seat: Seat, dimmed: Boolean, focused: Boolean, onLocate: () -> Unit, onRemove: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Định vị trên sơ đồ", onClick = onLocate)
            .background(if (focused) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface)
            .padding(start = 20.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
            .semantics { contentDescription = "${seat.displayName()}, ${seat.type.label}, ${formatVnd(seat.type.price)}" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SeatIcon(seat.type, SeatStatus.AVAILABLE)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(seat.displayName(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                seat.type.label + if (dimmed) " · ngoài bộ lọc" else "",
                style = MaterialTheme.typography.bodySmall, color = seat.type.color(),
            )
        }
        Text(formatVnd(seat.type.price), style = MaterialTheme.typography.bodyMedium)
        IconButton(onClick = onLocate) { Icon(Icons.Filled.MyLocation, "Định vị ${seat.id}", Modifier.size(20.dp)) }
        IconButton(onClick = onRemove) { Icon(Icons.Filled.Close, "Bỏ chọn ${seat.id}") }
    }
}
