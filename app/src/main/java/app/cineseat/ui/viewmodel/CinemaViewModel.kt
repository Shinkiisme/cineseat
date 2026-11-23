package app.cineseat.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import app.cineseat.data.model.Seat
import app.cineseat.data.model.SeatStatus
import app.cineseat.data.model.SeatType
import app.cineseat.data.repository.SeatRepository
import app.cineseat.ui.state.CinemaUiState
import app.cineseat.ui.state.SeatFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * All seat logic lives here. Every change is written to [SavedStateHandle] too, so the selection, mock bookings
 * and filter survive rotation (the ViewModel itself survives) and process death (the handle is restored).
 */
class CinemaViewModel(private val saved: SavedStateHandle) : ViewModel() {
    private val seats: List<Seat> = SeatRepository.seats
    private val byId = SeatRepository.byId

    private val _state = MutableStateFlow(
        CinemaUiState(
            seats = seats,
            selectedIds = saved.get<ArrayList<String>>(KEY_SELECTED).orEmpty().filter { it in byId }.distinct(),
            bookedIds = saved.get<ArrayList<String>>(KEY_BOOKED).orEmpty().filter { it in byId }.toSet(),
            inspectedId = saved.get<String>(KEY_INSPECTED)?.takeIf { it in byId },
            filter = SeatFilter(
                types = saved.get<ArrayList<String>>(KEY_TYPES).orEmpty().mapNotNull { n -> SeatType.entries.find { it.name == n } }.toSet(),
                statuses = saved.get<ArrayList<String>>(KEY_STATUSES).orEmpty().mapNotNull { n -> SeatStatus.entries.find { it.name == n } }.toSet(),
            ),
        )
    )
    val state: StateFlow<CinemaUiState> = _state.asStateFlow()

    /** A tap on the map. [id] is null when the tap hit no seat, which closes the seat details. */
    fun onSeatTapped(id: String?) {
        val seat = id?.let(byId::get)
        if (seat == null) return update { it.copy(inspectedId = null) }
        update { s ->
            // A seat dimmed by the filter can be looked at but not picked, so the filter never changes the selection by surprise.
            val selected = when {
                !s.isVisible(seat) -> s.selectedIds
                s.statusOf(seat) == SeatStatus.AVAILABLE -> s.selectedIds + seat.id
                s.statusOf(seat) == SeatStatus.SELECTED -> s.selectedIds - seat.id
                else -> s.selectedIds // booked or blocked: details only
            }
            s.copy(selectedIds = selected, inspectedId = seat.id)
        }
    }

    /** Shows a seat's details without changing the selection (used by the bottom sheet list). */
    fun inspect(id: String) {
        if (id in byId) update { it.copy(inspectedId = id) }
    }

    fun removeSelection(id: String) = update { it.copy(selectedIds = it.selectedIds - id) }

    fun clearSelection() = update { it.copy(selectedIds = emptyList()) }

    fun toggleType(type: SeatType) = update { it.copy(filter = it.filter.copy(types = it.filter.types.toggle(type))) }

    fun toggleStatus(status: SeatStatus) = update { it.copy(filter = it.filter.copy(statuses = it.filter.statuses.toggle(status))) }

    fun clearFilter() = update { it.copy(filter = SeatFilter()) }

    /** Mock checkout: the picked seats become BOOKED for this session. Returns how many were booked. */
    fun confirmBooking(): Int {
        val count = _state.value.selectedIds.size
        if (count > 0) update { it.copy(bookedIds = it.bookedIds + it.selectedIds, selectedIds = emptyList()) }
        return count
    }

    private fun <T> Set<T>.toggle(item: T) = if (item in this) this - item else this + item

    private inline fun update(transform: (CinemaUiState) -> CinemaUiState) {
        val next = transform(_state.value)
        _state.value = next
        saved[KEY_SELECTED] = ArrayList(next.selectedIds)
        saved[KEY_BOOKED] = ArrayList(next.bookedIds)
        saved[KEY_INSPECTED] = next.inspectedId
        saved[KEY_TYPES] = ArrayList(next.filter.types.map { it.name })
        saved[KEY_STATUSES] = ArrayList(next.filter.statuses.map { it.name })
    }

    private companion object {
        const val KEY_SELECTED = "selected"
        const val KEY_BOOKED = "booked"
        const val KEY_INSPECTED = "inspected"
        const val KEY_TYPES = "filter_types"
        const val KEY_STATUSES = "filter_statuses"
    }
}
