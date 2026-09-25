package models;

import java.time.LocalDate;
import java.util.List;

/** Un tour completo: cadena de segmentos que cubre el rango solicitado. */
public class HotelTour {

  private final List<TourSegment> segments;
  private final LocalDate checkIn;
  private final LocalDate checkOut;
  private final double totalPrice;

  public HotelTour(List<TourSegment> segments, LocalDate checkIn, LocalDate checkOut, double totalPrice) {
    this.segments = segments;
    this.checkIn = checkIn;
    this.checkOut = checkOut;
    this.totalPrice = totalPrice;
  }

  public List<TourSegment> getSegments() {
    return segments;
  }

  public LocalDate getCheckIn() {
    return checkIn;
  }

  public LocalDate getCheckOut() {
    return checkOut;
  }

  public double getTotalPrice() {
    return totalPrice;
  }

  public int getMoves() {
    return Math.max(0, segments.size() - 1);
  }

  public String getRoomsSummary() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < segments.size(); i++) {
      if (i > 0)
        sb.append(" → ");
      sb.append(segments.get(i).getRoom().getNumber());
    }
    return sb.toString();
  }
}
