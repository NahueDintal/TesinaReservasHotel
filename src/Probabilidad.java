public class Probabilidad {
  private int totalReservas;
  private int totalCanceladas;

  public Probabilidad(int totalReservas, int totalCanceladas) {
    this.totalReservas = totalReservas;
    this.totalCanceladas = totalCanceladas;
  }

  public double probabilidadCancelacion() {
    return (double) totalCanceladas / totalReservas;
  }

  public double probabilidadDoubleCancelacion() {
    return 1.0 - probabilidadCancelacion();
  }

  // cancelación de temporada baja
  // cancelación en temporada alta
}
