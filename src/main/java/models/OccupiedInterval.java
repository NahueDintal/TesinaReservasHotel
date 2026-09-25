package models;

import java.time.LocalDate;

/** Un rango ocupado (checkIn → checkOut) para una habitación. */
public class OccupiedInterval {

  private final int roomNumber;
  private final LocalDate from;
  private final LocalDate to;

  public OccupiedInterval(int roomNumber, LocalDate from, LocalDate to) {
    this.roomNumber = roomNumber;
    this.from = from;
    this.to = to;
  }

  public int getRoomNumber() {
    return roomNumber;
  }

  public LocalDate getFrom() {
    return from;
  }

  public LocalDate getTo() {
    return to;
  }
}
