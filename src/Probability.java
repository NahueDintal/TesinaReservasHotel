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
  // cancelación en temporada alta
}
