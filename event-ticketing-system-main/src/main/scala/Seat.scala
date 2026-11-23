class Seat(val seatId: String) {
  private var booked: Boolean = false
  def isBooked: Boolean = booked

  def book(): Unit = {
    if (booked) throw new DoubleBookingException(s"Seat $seatId is already booked.")
    booked = true
  }

  def cancel(): Unit = {
    if (!booked) throw new SeatNotBookedException(s"Seat $seatId is not currently booked.")
    booked = false
  }

  def restoreBooked(): Unit = booked = true
}
