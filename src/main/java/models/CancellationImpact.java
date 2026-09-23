package models;

/**
 * Aggregated cancellation impact for a category (channel, month, ...).
 * Includes counts and monetary amounts (total revenue and lost revenue).
 */
public class CancellationImpact {

  private String category;
  private int totalReservations;
  private int cancelledReservations;
  private double totalRevenue;
  private double lostRevenue;

  public CancellationImpact() {
  }

  public CancellationImpact(String category, int totalReservations, int cancelledReservations,
      double totalRevenue, double lostRevenue) {
    this.category = category;
    this.totalReservations = totalReservations;
    this.cancelledReservations = cancelledReservations;
    this.totalRevenue = totalRevenue;
    this.lostRevenue = lostRevenue;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public int getTotalReservations() {
    return totalReservations;
  }

  public void setTotalReservations(int totalReservations) {
    this.totalReservations = totalReservations;
  }

  public int getCancelledReservations() {
    return cancelledReservations;
  }

  public void setCancelledReservations(int cancelledReservations) {
    this.cancelledReservations = cancelledReservations;
  }

  public double getTotalRevenue() {
    return totalRevenue;
  }

  public void setTotalRevenue(double totalRevenue) {
    this.totalRevenue = totalRevenue;
  }

  public double getLostRevenue() {
    return lostRevenue;
  }

  public void setLostRevenue(double lostRevenue) {
    this.lostRevenue = lostRevenue;
  }

  // ---------- Derived ----------

  public double getRetainedRevenue() {
    return totalRevenue - lostRevenue;
  }

  /** Lost revenue as a fraction of total revenue for this category. */
  public double getLostRatio() {
    return totalRevenue == 0 ? 0.0 : lostRevenue / totalRevenue;
  }

  public String getLostRatioFormatted() {
    return String.format("%.2f %%", getLostRatio() * 100);
  }
}
