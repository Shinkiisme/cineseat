package app.cineseat

import androidx.lifecycle.SavedStateHandle
import app.cineseat.data.model.SeatStatus
import app.cineseat.data.model.SeatType
import app.cineseat.data.repository.SeatRepository
import app.cineseat.ui.viewmodel.CinemaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CinemaViewModelTest {
    private val seats = SeatRepository.seats
    private fun firstWith(status: SeatStatus, type: SeatType? = null) =
        seats.first { it.baseStatus == status && (type == null || it.type == type) }

    @Test fun mockDataHas208SeatsInTheRightMix() {
        assertEquals(208, seats.size)
        assertEquals(128, seats.count { it.type == SeatType.STANDARD })
        assertEquals(64, seats.count { it.type == SeatType.VIP })
        assertEquals(16, seats.count { it.type == SeatType.COUPLE })
        assertEquals(208, seats.map { it.id }.toSet().size)
        SeatStatus.entries.filter { it != SeatStatus.SELECTED }.forEach { s -> assertTrue("no $s seats", seats.any { it.baseStatus == s }) }
        assertTrue(seats.none { it.baseStatus == SeatStatus.SELECTED })
    }

    @Test fun mockDataIsTheSameEveryTime() {
        val again = SeatRepository.seats.map { it.baseStatus }
        assertEquals(seats.map { it.baseStatus }, again)
    }

    @Test fun tapSelectsThenDeselectsAndRestoresStatus() {
        val vm = CinemaViewModel(SavedStateHandle())
        val seat = firstWith(SeatStatus.AVAILABLE)
        vm.onSeatTapped(seat.id)
        assertEquals(listOf(seat.id), vm.state.value.selectedIds)
        assertEquals(SeatStatus.SELECTED, vm.state.value.statusOf(seat))
        vm.onSeatTapped(seat.id)
        assertTrue(vm.state.value.selectedIds.isEmpty())
        assertEquals(SeatStatus.AVAILABLE, vm.state.value.statusOf(seat))
    }

    @Test fun bookedAndBlockedSeatsCanBeInspectedButNotSelected() {
        val vm = CinemaViewModel(SavedStateHandle())
        for (status in listOf(SeatStatus.BOOKED, SeatStatus.BLOCKED)) {
            val seat = firstWith(status)
            vm.onSeatTapped(seat.id)
            assertTrue(vm.state.value.selectedIds.isEmpty())
            assertEquals(seat.id, vm.state.value.inspectedId)
        }
        vm.onSeatTapped(null)
        assertNull(vm.state.value.inspectedId)
    }

    @Test fun totalAndRemovalFromTheSheet() {
        val vm = CinemaViewModel(SavedStateHandle())
        val std = firstWith(SeatStatus.AVAILABLE, SeatType.STANDARD)
        val vip = firstWith(SeatStatus.AVAILABLE, SeatType.VIP)
        val couple = firstWith(SeatStatus.AVAILABLE, SeatType.COUPLE)
        listOf(std, vip, couple, vip).forEach { vm.onSeatTapped(it.id) } // the second VIP tap deselects it
        assertEquals(listOf(std.id, couple.id), vm.state.value.selectedIds)
        assertEquals(75_000L + 190_000L, vm.state.value.total)
        vm.removeSelection(couple.id)
        assertEquals(75_000L, vm.state.value.total)
        assertEquals(SeatStatus.AVAILABLE, vm.state.value.statusOf(couple))
    }

    @Test fun filterDimsWithoutTouchingTheSelection() {
        val vm = CinemaViewModel(SavedStateHandle())
        val std = firstWith(SeatStatus.AVAILABLE, SeatType.STANDARD)
        vm.onSeatTapped(std.id)
        vm.toggleType(SeatType.VIP)
        val s = vm.state.value
        assertEquals(listOf(std.id), s.selectedIds)
        assertTrue(!s.isVisible(std))
        assertEquals(64, s.matchCount)
        // A tap on a dimmed seat only shows details.
        vm.onSeatTapped(std.id)
        assertEquals(listOf(std.id), vm.state.value.selectedIds)
        // VIP + SELECTED has no match (the only pick is Standard): the count must say 0 so the UI can show "no results".
        vm.toggleStatus(SeatStatus.SELECTED)
        assertEquals(0, vm.state.value.matchCount)
        vm.clearFilter()
        assertEquals(208, vm.state.value.matchCount)
        assertEquals(listOf(std.id), vm.state.value.selectedIds)
    }

    @Test fun confirmBookingTurnsSelectedSeatsIntoBooked() {
        val vm = CinemaViewModel(SavedStateHandle())
        val seat = firstWith(SeatStatus.AVAILABLE)
        vm.onSeatTapped(seat.id)
        assertEquals(1, vm.confirmBooking())
        assertTrue(vm.state.value.selectedIds.isEmpty())
        assertEquals(SeatStatus.BOOKED, vm.state.value.statusOf(seat))
        vm.onSeatTapped(seat.id)
        assertTrue(vm.state.value.selectedIds.isEmpty())
        assertEquals(0, vm.confirmBooking())
    }

    @Test fun stateComesBackFromSavedStateHandle() {
        val handle = SavedStateHandle()
        val vm = CinemaViewModel(handle)
        val a = firstWith(SeatStatus.AVAILABLE, SeatType.STANDARD)
        val b = firstWith(SeatStatus.AVAILABLE, SeatType.VIP)
        vm.onSeatTapped(a.id)
        vm.onSeatTapped(b.id)
        vm.toggleStatus(SeatStatus.AVAILABLE)
        // A new ViewModel on the same handle is what the system builds after process death.
        val restored = CinemaViewModel(handle).state.value
        assertEquals(listOf(a.id, b.id), restored.selectedIds)
        assertEquals(b.id, restored.inspectedId)
        assertEquals(setOf(SeatStatus.AVAILABLE), restored.filter.statuses)
    }
}
