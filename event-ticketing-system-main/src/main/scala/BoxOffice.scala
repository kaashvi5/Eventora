import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import java.util.Base64
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

object BoxOffice {
  private val events = mutable.LinkedHashMap.empty[Int, Event]
  private val tickets = mutable.ArrayBuffer.empty[Ticket]
  private var ticketCounter = 101
  private var storageFile: Option[Path] = None

  def addEvent(event: Event): Unit = synchronized {
    if (events.contains(event.eventId)) throw new EventAlreadyExistsException("An event with this ID already exists.")
    events(event.eventId) = event
  }

  def getEvent(eventId: Int): Event = synchronized {
    events.getOrElse(eventId, throw new EventNotFoundException(s"Event #$eventId was not found."))
  }

  def getAllEvents: Seq[Event] = synchronized { events.values.toSeq }
  def getAllTickets: Seq[Ticket] = synchronized { tickets.toSeq }
  def findTickets(query: String): Seq[Ticket] = synchronized {
    val q = Option(query).getOrElse("").trim.toLowerCase
    tickets.filter(t => t.eventName.toLowerCase.contains(q) || t.customerName.toLowerCase.contains(q) || t.customerEmail.toLowerCase.contains(q)).toSeq
  }

  def bookTicket(eventId: Int, seatId: String): Ticket = bookTicket(eventId, seatId, "Guest", "")

  def bookTicket(eventId: Int, seatId: String, customerName: String, customerEmail: String): Ticket = synchronized {
    val name = Option(customerName).getOrElse("").trim
    val email = Option(customerEmail).getOrElse("").trim
    if (name.length < 2 || name.length > 60) throw new InvalidCustomerException("Enter a name between 2 and 60 characters.")
    if (email.nonEmpty && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
      throw new InvalidCustomerException("Enter a valid email address or leave it blank.")

    if (ticketCounter > 999) throw new TicketIdLimitException("Ticket ID limit reached (101-999). No more bookings can be created.")

    val event = getEvent(eventId)
    if (event.availableSeats == 0) throw new EventSoldOutException("This event is sold out.")
    val seat = event.getSeat(seatId)
    seat.book()
    val ticket = new Ticket(ticketCounter, event.eventId, event.eventName, seat.seatId, event.venue, name, email, event.price)
    ticketCounter += 1
    tickets += ticket
    persistIfConfigured()
    ticket
  }

  def cancelTicket(ticketId: Int): Unit = synchronized {
    val index = tickets.indexWhere(_.ticketId == ticketId)
    if (index < 0) throw new BookingNotFoundException(s"Ticket EVT-$ticketId was not found.")
    val ticket = tickets(index)
    getEvent(ticket.eventId).getSeat(ticket.seatId).cancel()
    tickets.remove(index)
    persistIfConfigured()
  }

  def totalBookings: Int = synchronized { tickets.size }
  def totalAvailableSeats: Int = synchronized { events.values.map(_.availableSeats).sum }
  def totalSeats: Int = synchronized { events.values.map(_.totalSeatCount).sum }
  def totalRevenue: Double = synchronized { tickets.map(_.pricePaid).sum }

  /** Load active bookings from a local TSV file and restore occupied seats. */
  def initializePersistence(fileName: String = "data/bookings.tsv"): Unit = synchronized {
    storageFile = Some(Paths.get(fileName))
    val path = storageFile.get
    if (Files.exists(path)) {
      val lines = Files.readAllLines(path, StandardCharsets.UTF_8).asScala.toSeq
      lines.filter(_.trim.nonEmpty).foreach { line =>
        val fields = line.split("\\t", -1)
        if (fields.length == 9) {
          try {
            val ticket = new Ticket(fields(0).toInt, fields(1).toInt, decode(fields(2)), decode(fields(3)), decode(fields(4)), decode(fields(5)), decode(fields(6)), fields(7).toDouble, decode(fields(8)))
            val event = getEvent(ticket.eventId)
            event.getSeat(ticket.seatId).restoreBooked()
            tickets += ticket
            ticketCounter = math.max(ticketCounter, ticket.ticketId + 1)
          } catch {
            case _: NumberFormatException => System.err.println("Skipped one invalid booking record in the data file.")
            case _: TicketingException => System.err.println("Skipped a booking whose event or seat is no longer available.")
          }
        }
      }
    }
  }

  private def encode(value: String): String = Base64.getEncoder.encodeToString(value.getBytes(StandardCharsets.UTF_8))
  private def decode(value: String): String = new String(Base64.getDecoder.decode(value), StandardCharsets.UTF_8)

  private def persistIfConfigured(): Unit = storageFile.foreach { path =>
    val parent = path.getParent
    if (parent != null) Files.createDirectories(parent)
    val rows = tickets.map { t =>
      Seq(t.ticketId.toString, t.eventId.toString, encode(t.eventName), encode(t.venue), encode(t.seatId), encode(t.customerName), encode(t.customerEmail), t.pricePaid.toString, encode(t.bookedAt)).mkString("\t")
    }
    val temp = path.resolveSibling(path.getFileName.toString + ".tmp")
    Files.write(temp, rows.asJava, StandardCharsets.UTF_8)
    try Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    catch { case _: java.nio.file.AtomicMoveNotSupportedException => Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING) }
  }

  /** Test helper; does not delete the persistent data file. */
  def resetForTests(): Unit = synchronized {
    events.clear(); tickets.clear(); ticketCounter = 101; storageFile = None
  }
}
