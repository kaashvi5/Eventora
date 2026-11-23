import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters.*
import scala.io.StdIn

object Main {
  private val reset = "\u001b[0m"
  private val cyan = "\u001b[36m"
  private val green = "\u001b[32m"
  private val yellow = "\u001b[33m"
  private val red = "\u001b[31m"
  private val bold = "\u001b[1m"

  private def rule(): Unit = println(cyan + "  " + "-" * 68 + reset)
  private def header(title: String): Unit = { println(); rule(); println(s"  ${bold}$title$reset"); rule() }
  private def read(prompt: String): String = { print(s"  $prompt"); Option(StdIn.readLine()).getOrElse("").trim }
  private def readInt(prompt: String): Option[Int] = read(prompt).toIntOption
  private def pause(): Unit = { println("\n  Press ENTER to return to the menu..."); StdIn.readLine() }
  private def money(amount: Double): String = f"Rs. $amount%,.2f"

  private def seedEvents(): Unit = {
    val samples = Seq(
      new Event(101, "Indie Music Festival", "Jawaharlal Nehru Stadium, Delhi", 20, "Music", "12 Dec 2026 | 6:00 PM", 1499),
      new Event(102, "FutureTech Summit", "Bharat Mandapam, New Delhi", 18, "Technology", "18 Dec 2026 | 9:30 AM", 999),
      new Event(103, "The Comedy Social", "The Laugh House, Gurugram", 15, "Comedy", "20 Dec 2026 | 7:00 PM", 599),
      new Event(104, "Startup Founders Meetup", "WeWork, Noida", 16, "Networking", "22 Dec 2026 | 11:00 AM", 399),
      new Event(105, "Art After Dark", "Lodhi Art District, Delhi", 12, "Art", "27 Dec 2026 | 5:00 PM", 749),
      new Event(106, "Food & Culture Carnival", "DLF Promenade, Vasant Kunj", 24, "Food", "30 Dec 2026 | 1:00 PM", 299)
    )
    samples.foreach(e => if (!BoxOffice.getAllEvents.exists(_.eventId == e.eventId)) BoxOffice.addEvent(e))
  }

  private def dashboard(): Unit = {
    val events = BoxOffice.getAllEvents
    val occupancy = if (BoxOffice.totalSeats == 0) 0 else ((BoxOffice.totalSeats - BoxOffice.totalAvailableSeats).toDouble / BoxOffice.totalSeats * 100).toInt
    println(s"\n${bold}${cyan}  E V E N T O R A${reset}  ${" " * 4}YOUR EVENT. YOUR MOMENT.")
    println("  " + "-" * 68)
    println(f"  EVENTS  ${events.size}%-5d  BOOKINGS  ${BoxOffice.totalBookings}%-5d  OCCUPANCY  $occupancy%%")
    println(s"  AVAILABLE SEATS  ${BoxOffice.totalAvailableSeats}  TICKET REVENUE  ${money(BoxOffice.totalRevenue)}")
    println("  " + "-" * 68)
  }

  private def eventCard(e: Event): Unit = {
    val status = if (e.availableSeats == 0) s"${red}SOLD OUT$reset" else s"${green}BOOKING OPEN$reset"
    println(s"  #${e.eventId.toString.padTo(4, ' ')} ${bold}${e.eventName.take(29).padTo(29, ' ')}$reset ${money(e.price)}")
    println(s"       ${e.category}  |  ${e.dateTime}")
    println(s"       ${e.venue}")
    println(s"       Seats ${e.availableSeats}/${e.totalSeatCount}  |  $status")
    println("  " + "-" * 68)
  }

  private def browseEvents(): Unit = {
    header("DISCOVER EVENTS")
    val events = BoxOffice.getAllEvents
    if (events.isEmpty) println("  No events are currently listed.") else events.foreach(eventCard)
  }

  private def searchEvents(): Unit = {
    header("FIND YOUR NEXT EXPERIENCE")
    val query = read("Search by event, venue, or category: ").toLowerCase
    if (query.isEmpty) { println("  Enter a search term to continue."); return }
    val matches = BoxOffice.getAllEvents.filter(e => Seq(e.eventName, e.venue, e.category).exists(_.toLowerCase.contains(query)))
    if (matches.isEmpty) println("  No matching events. Try a different keyword.") else matches.foreach(eventCard)
  }

  private def chooseEvent(): Option[Event] = {
    browseEvents()
    readInt("Enter event ID (0 to go back): ").flatMap {
      case 0 => None
      case id => Some(BoxOffice.getEvent(id))
    }
  }

  private def viewSeats(): Unit = {
    header("LIVE SEAT AVAILABILITY")
    chooseEvent().foreach(_.displaySeats())
  }

  private def bookTicket(): Unit = {
    header("RESERVE YOUR SEAT")
    chooseEvent().foreach { event =>
      if (event.availableSeats == 0) println(s"  ${red}This event is sold out.$reset")
      else {
        event.displaySeats()
        val name = read("Full name: ")
        val email = read("Email address (optional): ")
        val seat = read("Seat ID (e.g. S4): ").toUpperCase
        val ticket = BoxOffice.bookTicket(event.eventId, seat, name, email)
        println(s"\n  ${green}[OK] Booking confirmed!$reset")
        ticket.displayTicket()
      }
    }
  }

  private def myBookings(): Unit = {
    header("BOOKING LOOKUP")
    val query = read("Enter your name, email, or leave blank to show all bookings: ")
    val results = if (query.isEmpty) BoxOffice.getAllTickets else BoxOffice.findTickets(query)
    if (results.isEmpty) println("  No matching bookings found.") else results.foreach(_.displayTicket())
  }

  private def cancelBooking(): Unit = {
    header("CANCEL A BOOKING")
    if (BoxOffice.getAllTickets.isEmpty) { println("  There are no active bookings to cancel."); return }
    BoxOffice.getAllTickets.foreach { t =>
      val reference = "EVT-" + t.ticketId.toString.reverse.padTo(6, '0').reverse
      println(s"  $reference  ${t.eventName}  |  ${t.seatId}  |  ${t.customerName}")
    }
    readInt("Ticket number to cancel (0 to go back): ").foreach { id =>
      if (id != 0) {
        val confirm = read(s"Confirm cancellation of EVT-$id? (Y/N): ").toUpperCase
        if (confirm == "Y") { BoxOffice.cancelTicket(id); println(s"  ${green}Booking cancelled. Seat released.$reset") }
        else println("  Cancellation aborted.")
      }
    }
  }

  private def reports(): Unit = {
    header("VENUE & SALES ANALYTICS")
    println(f"  Active bookings : ${BoxOffice.totalBookings}%d")
    println(s"  Gross revenue   : ${money(BoxOffice.totalRevenue)}")
    println(f"  Seats sold      : ${BoxOffice.totalSeats - BoxOffice.totalAvailableSeats}%d / ${BoxOffice.totalSeats}%d")
    println("\n  EVENT PERFORMANCE")
    BoxOffice.getAllEvents.foreach { e =>
      val filled = (e.occupancyPercent / 5).max(0).min(20)
      val bar = "#" * filled + "-" * (20 - filled)
      println(s"  ${e.eventName.take(27).padTo(27, ' ')} $bar ${e.occupancyPercent}%  ${e.bookedSeats}/${e.totalSeatCount}")
    }
  }

  private def exportBookings(): Unit = {
    header("EXPORT BOOKINGS")
    val path = Paths.get("exports", "bookings.csv")
    Files.createDirectories(path.getParent)
    def csv(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""
    val rows = Seq("ticket_id,event_id,event,venue,seat,customer,email,price,booked_at") ++ BoxOffice.getAllTickets.map { t =>
      Seq(t.ticketId.toString, t.eventId.toString, t.eventName, t.venue, t.seatId, t.customerName, t.customerEmail, f"${t.pricePaid}%.2f", t.bookedAt).map(csv).mkString(",")
    }
    Files.write(path, rows.asJava, StandardCharsets.UTF_8)
    println(s"  ${green}[OK] Exported ${BoxOffice.totalBookings} booking(s) to ${path.toString}.$reset")
  }

  private def welcomeScreen(): Unit = {
    val gold = "\u001b[38;5;220m"
    val purple = "\u001b[38;5;141m"
    val white = "\u001b[97m"
    val muted = "\u001b[38;5;250m"

    println()
    println(s"  ${gold}+====================================================================+$reset")
    println(s"  ${gold}|                                                                    |$reset")
    println(s"  ${gold}|${purple}                         E V E N T O R A                            ${gold}|$reset")
    println(s"  ${gold}|${white}                    YOUR EVENT. YOUR MOMENT.                        ${gold}|$reset")
    println(s"  ${gold}|${muted}              Discover  -  Reserve  -  Experience                   ${gold}|$reset")
    println(s"  ${gold}|                                                                    |$reset")
    println(s"  ${gold}+--------------------------------------------------------------------+$reset")
    println(s"  ${white}       [ DISCOVER ]  --->  [ CHOOSE ]  --->  [ BOOK ]  --->  [ ENJOY ]$reset")
    println(s"  ${gold}+--------------------------------------------------------------------+$reset")
    println(s"  ${gold}|${white}  WELCOME TO YOUR EVENT DISCOVERY AND TICKETING DESK                 ${gold}|$reset")
    println(s"  ${gold}|${muted}  Find an event, pick your seat, and make your next memory.           ${gold}|$reset")
    println(s"  ${gold}+====================================================================+$reset")
    println()
  }

  private def printMenu(): Unit = {
    println("\n  1  Browse events          5  My bookings")
    println("  2  Search events          6  Cancel booking")
    println("  3  View seat map          7  Sales analytics")
    println("  4  Book a ticket           8  Export bookings (CSV)")
    println("                            9  Exit")
  }

  def main(args: Array[String]): Unit = {
    try {
      welcomeScreen()
      seedEvents()
      BoxOffice.initializePersistence()
      var running = true
      while (running) {
        dashboard()
        printMenu()
        read("Choose an option: ") match {
          case "1" => browseEvents(); pause()
          case "2" => searchEvents(); pause()
          case "3" => try viewSeats() catch { case e: TicketingException => println(s"  ${yellow}! ${e.getMessage}$reset") }; pause()
          case "4" => try bookTicket() catch { case e: TicketingException => println(s"  ${red}! ${e.getMessage}$reset") }; pause()
          case "5" => myBookings(); pause()
          case "6" => try cancelBooking() catch { case e: TicketingException => println(s"  ${red}! ${e.getMessage}$reset") }; pause()
          case "7" => reports(); pause()
          case "8" => exportBookings(); pause()
          case "9" => running = false
          case _ => println(s"  ${yellow}Choose a valid option from 1 to 9.$reset"); pause()
        }
      }
      println(s"\n  ${green}Thanks for choosing EVENTORA. See you at the next event!$reset\n")
    } catch {
      case e: Exception => System.err.println(s"\nApplication error: ${e.getMessage}")
    }
  }
}
