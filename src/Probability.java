public class Probability {
  private int totalReservation;
  private int totalCancelled;

  public Probability(int totalReservation, int totalCancelled) {
    this.totalReservation = totalReservation;
    this.totalCancelled = totalCancelled;
  }

  public double probabilityCancelled() {
    return (double) totalCancelled / totalReservation;
  }

  // cancelación de temporada baja
  // tendría que hacer una consulta con los meses que se denominan como temp baja
  // cancelación en temporada alta
  // idem al anterior
}
