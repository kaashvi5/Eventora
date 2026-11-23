class MySuite extends munit.FunSuite {
  private def add(id: Int, seats: Int = 2): Event = {
    val event = new Event(id, "Test Concert", "Delhi", seats, "Music", "2026-12-12", 500)
    BoxOffice.addEvent(event)
    event
  }

  test("successful booking stores customer and price") {
    BoxOffice.resetForTests()
    val event = add(1001)
    val ticket = BoxOffice.bookTicket(1001, "s1", "Asha Sharma", "asha@example.com")
    assertEquals(ticket.seatId, "S1")
    assertEquals(ticket.customerName, "Asha Sharma")
    assertEquals(ticket.pricePaid, 500.0)
    assertEquals(event.availableSeats, 1)
  }

  test("prevent double booking") {
    BoxOffice.resetForTests()
    add(1002)
    BoxOffice.bookTicket(1002, "S1")
    intercept[DoubleBookingException] { BoxOffice.bookTicket(1002, "S1") }
  }

  test("reject invalid seat") {
    BoxOffice.resetForTests()
    add(1003)
    intercept[SeatNotFoundException] { BoxOffice.bookTicket(1003, "S99") }
  }

  test("prevent booking when sold out") {
    BoxOffice.resetForTests()
    add(1004, 1)
    BoxOffice.bookTicket(1004, "S1")
    intercept[EventSoldOutException] { BoxOffice.bookTicket(1004, "S1") }
  }

  test("reject invalid event ID") {
    BoxOffice.resetForTests()
    intercept[EventNotFoundException] { BoxOffice.bookTicket(999999, "S1") }
  }

  test("cancellation releases seat and removes active booking") {
    BoxOffice.resetForTests()
    val event = add(1005, 1)
    val ticket = BoxOffice.bookTicket(1005, "S1")
    BoxOffice.cancelTicket(ticket.ticketId)
    assertEquals(event.availableSeats, 1)
    assertEquals(BoxOffice.totalBookings, 0)
  }

  test("reject invalid customer details") {
    BoxOffice.resetForTests()
    add(1006)
    intercept[InvalidCustomerException] { BoxOffice.bookTicket(1006, "S1", "A", "bad-email") }
  }

  test("reject duplicate event IDs") {
    BoxOffice.resetForTests()
    add(1007)
    intercept[EventAlreadyExistsException] { add(1007) }
  }
}
