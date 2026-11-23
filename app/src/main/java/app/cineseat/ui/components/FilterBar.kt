package app.cineseat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.cineseat.data.model.SeatStatus
import app.cineseat.data.model.SeatType
import app.cineseat.ui.state.SeatFilter

/** Chips for seat type and status. Several chips in one group mean "any of these"; the two groups combine with AND. */
@Composable
fun FilterChips(
    filter: SeatFilter,
    onToggleType: (SeatType) -> Unit,
    onToggleStatus: (SeatStatus) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    LazyRow(modifier, contentPadding = contentPadding, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        items(SeatType.entries.size) { i ->
            val type = SeatType.entries[i]
            val on = type in filter.types
            FilterChip(
                selected = on,
                onClick = { onToggleType(type) },
                label = { Text(type.label) },
                leadingIcon = { if (on) Icon(Icons.Filled.Check, null, Modifier.width(18.dp)) else SeatIcon(type, SeatStatus.AVAILABLE) },
            )
        }
        item { VerticalDivider(Modifier.height(24.dp)) }
        items(SeatStatus.entries.size) { i ->
            val status = SeatStatus.entries[i]
            val on = status in filter.statuses
            FilterChip(
                selected = on,
                onClick = { onToggleStatus(status) },
                label = { Text(status.label) },
                leadingIcon = { if (on) Icon(Icons.Filled.Check, null, Modifier.width(18.dp)) else SeatIcon(SeatType.STANDARD, status) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}

/** Says which filter is on and how many seats match; offers to clear it. Hidden when no filter is on. */
@Composable
fun ActiveFilterLine(filter: SeatFilter, matchCount: Int, onClear: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedVisibility(filter.isActive, modifier) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.FilterList, null, Modifier.width(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                val parts = filter.types.map { it.label } + filter.statuses.map { it.label }
                Text(
                    "Đang lọc: ${parts.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (matchCount == 0) "Không có ghế phù hợp" else "$matchCount ghế phù hợp",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (matchCount == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onClear) { Text("Xóa lọc") }
        }
    }
}
