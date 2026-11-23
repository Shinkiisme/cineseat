package app.cineseat.data.repository

import app.cineseat.data.model.Seat
import app.cineseat.data.model.SeatStatus
import app.cineseat.data.model.SeatType
import kotlin.random.Random

/**
 * Mock data for one showing: 14 rows (A–H Standard, I–L VIP, M–N Couple), 208 seats in all.
 * Statuses come from a fixed seed, so the hall looks the same on every launch and after rotation.
 */
object SeatRepository {
    const val SEATS_PER_ROW = 16
    const val COUPLES_PER_ROW = 8
    val rows: List<Char> = ('A'..'N').toList()

    const val CINEMA = "CineSeat Landmark 81"
    const val HALL = "Phòng chiếu 05"
    const val MOVIE = "Dune: Messiah"
    const val SHOWTIME = "19:30 · Thứ Bảy 28/11"

    fun typeOf(row: Char): SeatType = when (row) {
        in 'A'..'H' -> SeatType.STANDARD
        in 'I'..'L' -> SeatType.VIP
        else -> SeatType.COUPLE
    }

    val seats: List<Seat> = buildSeats()
    val byId: Map<String, Seat> = seats.associateBy { it.id }

    private fun buildSeats(): List<Seat> {
        val random = Random(2024)
        val result = ArrayList<Seat>(208)
        for (row in rows) {
            val type = typeOf(row)
            val count = if (type == SeatType.COUPLE) COUPLES_PER_ROW else SEATS_PER_ROW
            val span = if (type == SeatType.COUPLE) 2 else 1
            for (i in 0 until count) {
                val number = i + 1
                val column = i * span
                // The middle block sells first, like a real hall.
                val central = column in 4..11
                val roll = random.nextFloat()
                val (status, note) = when {
                    row == 'A' && (number <= 2 || number >= 15) -> SeatStatus.BLOCKED to "khu vực kỹ thuật, không mở bán"
                    roll < 0.04f -> SeatStatus.BLOCKED to "ghế đang bảo trì"
                    roll < (if (central) 0.42f else 0.22f) -> SeatStatus.BOOKED to "khách khác đã đặt trước"
                    else -> SeatStatus.AVAILABLE to null
                }
                result += Seat("$row$number", row, number, type, status, column, span, note)
            }
        }
        return result
    }
}
