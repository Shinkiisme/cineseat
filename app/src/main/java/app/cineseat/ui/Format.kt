package app.cineseat.ui

import app.cineseat.data.model.Seat
import app.cineseat.data.model.SeatType

/** 190000 → "190.000đ" */
fun formatVnd(amount: Long): String = "%,d".format(amount).replace(',', '.') + "đ"

fun Seat.displayName(): String = if (type == SeatType.COUPLE) "Ghế đôi $id" else "Ghế $id"
