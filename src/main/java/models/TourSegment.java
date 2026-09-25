package models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Un tramo de un hotel tour: cuántas noches en qué habitación. */
public class TourSegment {

  private final Room room;
  private final LocalDate from;
  private final LocalDate to;

  public TourSegment(Room room, LocalDate from, LocalDate to) {
    this.room = room;
    this.from = from;
    this.to = to;
  }

  public Room getRoom() {
    return room;
  }

  public LocalDate getFrom() {
    return from;
  }

  public LocalDate getTo() {
    return to;
  }

  public long getNights() {
    return ChronoUnit.DAYS.between(from, to);
  }

  public double getSubtotal() {
    return getNights() * room.getPrice();
  }

  @Override
  public String toString() {
    return String.format("Hab. %d: %s → %s (%d noches)",
        room.getNumber(), from, to, getNights());
  }
}
