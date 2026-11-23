# EVENTORA — Event Ticketing System

A feature-rich, console-based event booking application built with **Scala 3**, **sbt**, and **MUnit**. This version expands the starter project with customer details, event discovery, seat maps, booking cancellation, persistent storage, analytics, and CSV export.

## Highlights

- **Event catalogue:** six sample events with category, venue, date/time, capacity, and ticket price.
- **Event discovery:** search by event name, venue, or category.
- **Seat selection:** live seat map, clear booked/available states, and double-booking protection.
- **Customer-aware booking:** capture customer name and optional email with validation.
- **Ticket receipt:** unique reference, event, venue, seat, customer, price, and booking timestamp.
- **Cancellation:** cancel by ticket reference; the seat becomes available again.
- **Persistent bookings:** active bookings are stored locally in `data/bookings.tsv` and restored on the next launch.
- **Dashboard and analytics:** booking count, seat availability, occupancy, and revenue.
- **CSV export:** export active bookings to `exports/bookings.csv` for spreadsheet use.
- **Defensive validation:** invalid event, seat, customer details, sold-out events, duplicate bookings, and missing ticket references are handled with domain-specific exceptions.
- **Automated tests:** booking, cancellation, validation, sold-out, and duplicate-event scenarios.

> This is an educational/demo application. It does not process real payments, send emails, or provide online multi-user booking. Revenue is the total value of active bookings, not a payment-gateway settlement.

## Technology

| Component | Version |
|---|---|
| Java | JDK 17 or later |
| Scala | 3.3.6 |
| sbt | 1.10.7 |
| Tests | MUnit 1.0.2 |

## Run on Windows

1. Install JDK 17+ and sbt.
2. Extract the ZIP.
3. Open the **inner** `event-ticketing-system-main` folder in VS Code (the folder containing `build.sbt`).
4. Open Terminal → New Terminal and run:

```powershell
sbt clean
sbt compile
sbt run
```

Run the test suite:

```powershell
sbt test
```

If PowerShell opens in the outer extracted folder, enter the inner project folder first:

```powershell
cd .\event-ticketing-system-main
```

## Menu

```text
1  Browse events          5  My bookings
2  Search events          6  Cancel booking
3  View seat map          7  Sales analytics
4  Book a ticket           8  Export bookings (CSV)
                            9  Exit
```

## Persistence and generated files

- `data/bookings.tsv` is created automatically after the first successful booking.
- The application restores active bookings and marks their seats as occupied when it starts again.
- `exports/bookings.csv` is created when you choose **Export bookings (CSV)**.
- Both folders are ignored by Git and are local to the project directory.

To start with a clean demo, exit the app and delete `data/bookings.tsv`. This clears saved bookings on the next launch.

## Project structure

```text
event-ticketing-system-main/
├── build.sbt
├── README.md
├── project/
│   └── build.properties
└── src/
    ├── main/scala/
    │   ├── Main.scala          # Console UI and user workflows
    │   ├── BoxOffice.scala     # Booking rules, persistence, analytics
    │   ├── Event.scala         # Event metadata and seat map
    │   ├── Seat.scala          # Seat state and transitions
    │   ├── Ticket.scala        # Booking receipt
    │   └── Exceptions.scala   # Domain-specific errors
    └── test/scala/
        └── MySuite.scala      # Automated tests
```

## OOP concepts demonstrated

- **Encapsulation:** seat state is private and changes through controlled operations.
- **Abstraction:** `BoxOffice` exposes booking operations while hiding storage details.
- **Inheritance:** domain exceptions inherit from `TicketingException`.
- **Composition:** events own seats; bookings reference events and seats.
- **Collections:** immutable event seat maps and managed mutable booking/event collections.
- **Synchronization:** booking and cancellation operations are synchronized within the running process.

## Important scope notes

Persistence uses a local file and is suitable for a single-user classroom demonstration. For a production deployment, replace it with a transactional database, add authentication and authorization, use a payment provider, and expose a secured web/API layer. Synchronization here protects threads in one JVM; it is not a substitute for database transactions across multiple application instances.
