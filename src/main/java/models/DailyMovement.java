package models;

import java.time.LocalDate;

/**
 * Represents a single movement (check-in or check-out) for the daily schedule.
 * Used by the DailySchedule report.
 */
public class DailyMovement {

  private int idReservation;
  private String customerName;
  private LocalDate movementDate;
  private int numberOfGuests;
  private double totalRate;
  private String reservationStatus;
  private String movementType; // "Check-in" | "Check-out"
  private String observations;

  public DailyMovement() {
  }

  public DailyMovement(int idReservation, String customerName, LocalDate movementDate,
      int numberOfGuests, double totalRate, String reservationStatus,
      String movementType, String observations) {
    this.idReservation = idReservation;
    this.customerName = customerName;
    this.movementDate = movementDate;
    this.numberOfGuests = numberOfGuests;
    this.totalRate = totalRate;
    this.reservationStatus = reservationStatus;
    this.movementType = movementType;
    this.observations = observations;
  }

  public int getIdReservation() {
    return idReservation;
  }

  public void setIdReservation(int idReservation) {
    this.idReservation = idReservation;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public LocalDate getMovementDate() {
    return movementDate;
  }

  public void setMovementDate(LocalDate movementDate) {
    this.movementDate = movementDate;
  }

  public int getNumberOfGuests() {
    return numberOfGuests;
  }

  public void setNumberOfGuests(int numberOfGuests) {
    this.numberOfGuests = numberOfGuests;
  }

  public double getTotalRate() {
    return totalRate;
  }

  public void setTotalRate(double totalRate) {
    this.totalRate = totalRate;
  }

  public String getReservationStatus() {
    return reservationStatus;
  }

  public void setReservationStatus(String reservationStatus) {
    this.reservationStatus = reservationStatus;
  }

  public String getMovementType() {
    return movementType;
  }

  public void setMovementType(String movementType) {
    this.movementType = movementType;
  }

  public String getObservations() {
    return observations;
  }

  public void setObservations(String observations) {
    this.observations = observations;
  }
}
