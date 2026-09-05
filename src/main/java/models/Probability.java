package models;

public class Probability {

  // cantidad de reservas activas
  // cantidad de reservas concreatadas 'que tengan checkout finalizado en tiempo y
  // forma'
  // cantidad de reservas canceladas
  // cantidad de reservas checklate
  // cantidad de reservas con alteración de dias de hospedaje

  private int totalReservation;
  private int reservationCancelled;

  // opcionales
  // private int reservarionChecklate;
  // private int reservationUpdate;

  public Probability(int totalReservation, int reservationCancelled) {
    this.totalReservation = totalReservation;
    this.reservationCancelled = reservationCancelled;
  }

  public double probabilityCancelled() {
    return (double) reservationCancelled / totalReservation;
  }

  // cancelación de temporada baja
  // tendría que hacer una consulta con los meses que se denominan como temp baja
  // cancelación en temporada alta
  // idem al anterior

  public int getTotalReservation() {
    return totalReservation;
  }

  public int getReservationCancelled() {
    return reservationCancelled;
  }

  public void setTotalReservation(int totalReservation) {
    this.totalReservation = totalReservation;
  }

  public void setReservationCancelled(int reservationCancelled) {
    this.reservationCancelled = reservationCancelled;
  }
}
