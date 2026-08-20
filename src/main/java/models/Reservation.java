package models;

import java.time.LocalDate;

public class Reservation {
  private Customer customer;
  private Room room;
  private LocalDate initDate;
  private LocalDate endDate;
  private double total;

  public Reservation(Customer customer, Room room, LocalDate initDate, LocalDate endDate) {
    if (customer != null) {
      this.customer = customer;
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

  public Room getRoom() {
    return room;
  }

  public Customer getClient() {
    return customer;
  }

  public double getTotal() {
    return total;
  }
}
