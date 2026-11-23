package app.cineseat.ui.state

import app.cineseat.data.model.Seat
import app.cineseat.data.model.SeatStatus
import app.cineseat.data.model.SeatType

/** An empty set means "any". The filter only dims seats on the map; it never changes seat data or the selection. */
data class SeatFilter(
    val types: Set<SeatType> = emptySet(),
    val statuses: Set<SeatStatus> = emptySet(),
) {
    val isActive get() = types.isNotEmpty() || statuses.isNotEmpty()
    fun matches(type: SeatType, status: SeatStatus) =
        (types.isEmpty() || type in types) && (statuses.isEmpty() || status in statuses)
}

/**
 * The single source of truth that the Canvas, the filter bar and the bottom sheet all read.
 * [selectedIds] keeps the order seats were picked in; [bookedIds] are seats this user confirmed.
 */
data class CinemaUiState(
    val seats: List<Seat>,
    val selectedIds: List<String> = emptyList(),
    val bookedIds: Set<String> = emptySet(),
    val inspectedId: String? = null,
    val filter: SeatFilter = SeatFilter(),
) {
    private val selectedSet = selectedIds.toHashSet()

    fun statusOf(seat: Seat): SeatStatus = when {
        seat.id in selectedSet -> SeatStatus.SELECTED
        seat.id in bookedIds -> SeatStatus.BOOKED
        else -> seat.baseStatus
    }

    fun isVisible(seat: Seat) = filter.matches(seat.type, statusOf(seat))

    val selectedSeats: List<Seat> get() = selectedIds.mapNotNull { id -> seats.firstOrNull { it.id == id } }
    val total: Long get() = selectedSeats.sumOf { it.type.price }
    val inspectedSeat: Seat? get() = inspectedId?.let { id -> seats.firstOrNull { it.id == id } }
    val matchCount: Int get() = seats.count(::isVisible)
}
