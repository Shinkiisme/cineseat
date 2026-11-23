package app.cineseat.data.model

enum class SeatType(val label: String, val price: Long) {
    STANDARD("Standard", 75_000),
    VIP("VIP", 105_000),
    COUPLE("Couple", 190_000),
}

/**
 * What a seat shows. The data only ever stores AVAILABLE, BOOKED or BLOCKED; SELECTED is derived from the
 * selection set in the UI state, so deselecting a seat always brings back the status it had before.
 */
enum class SeatStatus(val label: String) {
    AVAILABLE("Còn trống"),
    SELECTED("Đang chọn"),
    BOOKED("Đã đặt"),
    BLOCKED("Không khả dụng"),
}

/**
 * One seat in the hall. [column] is the slot from the left (0..15) and [span] how many slots it covers
 * (a couple seat covers 2). Where it sits on screen is worked out by the layout, not stored here.
 */
data class Seat(
    val id: String,
    val row: Char,
    val number: Int,
    val type: SeatType,
    val baseStatus: SeatStatus,
    val column: Int,
    val span: Int = 1,
    val note: String? = null,
)
