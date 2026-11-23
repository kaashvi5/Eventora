import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Ticket(
    val ticketId: Int,
    val eventId: Int,
    val eventName: String,
    val seatId: String,
    val venue: String,
    val customerName: String = "Guest",
    val customerEmail: String = "",
    val pricePaid: Double = 0.0,
    val bookedAt: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
) {
  def displayTicket(): Unit = {
    val border = "=" * 56
    println(s"\n  $border")
    println("                      EVENTORA")
    println("                  BOOKING CONFIRMATION")
    println(s"  $border")
    println(f"  Ticket reference : EVT-$ticketId%06d")
    println(s"  Event            : $eventName")
    println(s"  Venue            : $venue")
    println(s"  Seat             : $seatId")
    println(s"  Customer         : $customerName")
    println(s"  Email            : ${if (customerEmail.isEmpty) "Not provided" else customerEmail}")
    println(f"  Amount paid      : Rs. $pricePaid%.2f")
    println(s"  Booked on        : $bookedAt")
    println(s"  $border")
    println("  Please keep your ticket reference for entry.")
  }
}
