package models;

/**
 * Represents the cancellation probability for a given booking channel.
 * The probability is the empirical conditional probability P(cancel | channel),
 * and the confidence interval is computed using the Wilson score method.
 */
public class Probability {

  private String channel;
  private int totalReservations;
  private int cancelledReservations;

  public Probability() {
  }

  public Probability(String channel, int totalReservations, int cancelledReservations) {
    this.channel = channel;
    this.totalReservations = totalReservations;
    this.cancelledReservations = cancelledReservations;
  }

  // ---------- Getters / Setters ----------

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
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

  // ---------- Derived values ----------

  /** Empirical probability of cancellation for this channel. */
  public double getProbability() {
    if (totalReservations == 0)
      return 0.0;
    return (double) cancelledReservations / totalReservations;
  }

  /** Lower bound of the 95% Wilson confidence interval. */
  public double getConfidenceIntervalLower() {
    return wilsonScore(-1.96);
  }

  /** Upper bound of the 95% Wilson confidence interval. */
  public double getConfidenceIntervalUpper() {
    return wilsonScore(1.96);
  }

  public String getFormattedConfidenceInterval() {
    return String.format("%.2f%% - %.2f%%",
        getConfidenceIntervalLower() * 100,
        getConfidenceIntervalUpper() * 100);
  }

  /**
   * Wilson score interval for a binomial proportion.
   * 
   * @param z standard normal quantile (1.96 for 95% confidence)
   */
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

  @Override
  public String toString() {
    return "Probability{" +
        "channel='" + channel + '\'' +
        ", total=" + totalReservations +
        ", cancelled=" + cancelledReservations +
        ", probability=" + String.format("%.4f", getProbability()) +
        '}';
  }
}
