package models;

import java.time.LocalDate;

public class Reservation {
<<<<<<< HEAD
  private Customer customer;
=======
  private Client client;
>>>>>>> Nahue
  private Room room;
  private LocalDate initDate;
  private LocalDate endDate;
  private double total;

<<<<<<< HEAD
  public Reservation(Customer customer, Room room, LocalDate initDate, LocalDate endDate) {
    if (customer != null) {
      this.customer = customer;
=======
  public Reservation(Client client, Room room, LocalDate initDate, LocalDate endDate) {
    if (client != null) {
      this.client = client;
>>>>>>> Nahue
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

<<<<<<< HEAD
  public Customer getClient() {
    return customer;
=======
  public Client getClient() {
    return client;
>>>>>>> Nahue
  }

  public double getTotal() {
    return total;
  }
}
