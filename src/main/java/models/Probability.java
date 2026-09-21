package models;

/**
 * Represents the empirical cancellation probability for a given category
 * (a booking channel, a lead time bucket, etc.) with a 95% Wilson confidence
 * interval.
 */
public class Probability {

  private String category;
  private int totalReservations;
  private int cancelledReservations;

  public Probability() {
  }

  public Probability(String category, int totalReservations, int cancelledReservations) {
    this.category = category;
    this.totalReservations = totalReservations;
    this.cancelledReservations = cancelledReservations;
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

  public double getProbability() {
    return totalReservations == 0 ? 0.0 : (double) cancelledReservations / totalReservations;
  }

  public double getConfidenceIntervalLower() {
    return wilsonScore(-1.96);
  }

  public double getConfidenceIntervalUpper() {
    return wilsonScore(1.96);
  }

  public String getFormattedConfidenceInterval() {
    return String.format("%.2f%% - %.2f%%",
        getConfidenceIntervalLower() * 100,
        getConfidenceIntervalUpper() * 100);
  }

  /** Wilson score interval for a binomial proportion. z = 1.96 for 95% CI. */
  private double wilsonScore(double z) {
    int n = totalReservations;
    if (n == 0)
      return 0.0;
    double p = getProbability();
    double z2 = z * z;
    double denominator = 1.0 + z2 / n;
    double center = p + z2 / (2.0 * n);
    double margin = z * Math.sqrt((p * (1 - p) / n) + (z2 / (4.0 * n * n)));
    return (center + margin) / denominator;
  }
}
