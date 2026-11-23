class Event(
    val eventId: Int,
    val eventName: String,
    val venue: String,
    totalSeats: Int,
    val category: String = "General",
    val dateTime: String = "Date to be announced",
    val price: Double = 499.0
) {
  require(eventId > 0, "Event ID must be positive")
  require(eventName.trim.nonEmpty, "Event name cannot be empty")
  require(venue.trim.nonEmpty, "Venue cannot be empty")
  require(totalSeats > 0, "Total seats must be greater than zero")
  require(price >= 0, "Ticket price cannot be negative")

  val seats: Map[String, Seat] =
    (1 to totalSeats).map(n => s"S$n" -> new Seat(s"S$n")).toMap

  def totalSeatCount: Int = seats.size
  def availableSeats: Int = seats.values.count(seat => !seat.isBooked)
  def bookedSeats: Int = totalSeatCount - availableSeats
  def occupancyPercent: Int = (bookedSeats.toDouble / totalSeatCount * 100).toInt

  def getSeat(seatId: String): Seat =
    seats.getOrElse(Option(seatId).getOrElse("").trim.toUpperCase,
      throw new SeatNotFoundException(s"Seat '$seatId' does not exist for this event."))

  def displaySeats(): Unit = {
    println(s"\n  SEAT MAP - $eventName")
    println("  [XX] Booked     [S#] Available\n")
    seats.values.toSeq.sortBy(_.seatId.drop(1).toInt).grouped(5).foreach { group =>
      println("  " + group.map(s => if (s.isBooked) "[XX]" else f"[${s.seatId}%-3s]").mkString("   "))
    }
    println("\n  SCREEN THIS WAY  ^")
    println(s"  Available: $availableSeats / $totalSeatCount")
  }
}
