import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Reservation{
  private Client client;
  private Room room;
  private LocalDate initDate;
  private LocalDate endDate;
  private double total;

  public Reservation(Client client, Room room, LocalDate initDate, LocalDate endDate) {
    if (client != null) {
      this.client = client;
    }
    this.room = room;
    this.initDate = initDate; // aca tendría que haber una de error por la fecha
    this.endDate = endDate;
    // this.total = dias * precio por noche * servicios * consumos en bar
  }

  public LocalDate getInitDate() {
    return initDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

}
