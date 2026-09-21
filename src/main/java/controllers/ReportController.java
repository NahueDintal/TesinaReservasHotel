package controllers;

import models.Probability;
import repositories.ProbabilityDAO;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

public class ReportController {

  // --- Filters ---
  @FXML
  private DatePicker fromDatePicker;
  @FXML
  private DatePicker toDatePicker;
  @FXML
  private ComboBox<String> channelComboBox;
  @FXML
  private Button calculateButton;
  @FXML
  private Button exportButton;
  @FXML
  private Button clearButton;

  // --- KPIs ---
  @FXML
  private Label totalReservationsLabel;
  @FXML
  private Label cancelledReservationsLabel;
  @FXML
  private Label globalProbabilityLabel;
  @FXML
  private ProgressBar globalProbabilityBar;

  // --- Chart ---
  @FXML
  private BarChart<String, Number> probabilityChart;
  @FXML
  private CategoryAxis channelAxis;
  @FXML
  private NumberAxis probabilityAxis;

  // --- Table ---
  @FXML
  private TableView<Probability> dataTable;
  @FXML
  private TableColumn<Probability, String> channelColumn;
  @FXML
  private TableColumn<Probability, Integer> totalColumn;
  @FXML
  private TableColumn<Probability, Integer> cancelledColumn;
  @FXML
  private TableColumn<Probability, Double> probabilityColumn;
  @FXML
  private TableColumn<Probability, Double> visualColumn;
  @FXML
  private TableColumn<Probability, String> confidenceIntervalColumn;

  @FXML
  private Label statusLabel;

  private final ProbabilityDAO probabilityDAO = new ProbabilityDAO();
  private final ObservableList<Probability> currentData = FXCollections.observableArrayList();

  /** Reference to the dashboard (same pattern used by the other controllers). */
  private DashboardController dashboardController;

  public void setDashboardController(DashboardController dashboardController) {
    this.dashboardController = dashboardController;
  }

  // ============================================================
  @FXML
  public void initialize() {
    toDatePicker.setValue(LocalDate.now());
    fromDatePicker.setValue(LocalDate.now().minusDays(90));

    // Placeholder channels until the service is wired up
    ObservableList<String> channels = FXCollections.observableArrayList(
        "All", "Booking", "Airbnb", "Expedia", "Direct", "Phone", "Walk-in");
    channelComboBox.setItems(channels);
    channelComboBox.setValue("All");

    configureTable();
    loadData();
  }

  private void configureTable() {
    channelColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getChannel()));
    totalColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTotalReservations()));
    cancelledColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCancelledReservations()));
    probabilityColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getProbability()));
    visualColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getProbability()));
    confidenceIntervalColumn
        .setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getFormattedConfidenceInterval()));

    probabilityColumn.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(Double value, boolean empty) {
        super.updateItem(value, empty);
        if (empty || value == null) {
          setText(null);
          getStyleClass().removeAll("prob-high", "prob-medium", "prob-low");
          return;
        }
        setText(String.format("%.2f %%", value * 100));
        getStyleClass().removeAll("prob-high", "prob-medium", "prob-low");
        if (value >= 0.30)
          getStyleClass().add("prob-high");
        else if (value >= 0.15)
          getStyleClass().add("prob-medium");
        else
          getStyleClass().add("prob-low");
      }
    });

    visualColumn.setCellFactory(c -> new TableCell<>() {
      private final ProgressBar bar = new ProgressBar(0);

      @Override
      protected void updateItem(Double value, boolean empty) {
        super.updateItem(value, empty);
        if (empty || value == null) {
          setGraphic(null);
          return;
        }
        bar.setProgress(value);
        bar.setMaxWidth(Double.MAX_VALUE);
        setGraphic(bar);
      }
    });

    dataTable.setItems(currentData);
  }

  // ============================================================
  @FXML
  private void onCalculate() {
    LocalDate from = fromDatePicker.getValue();
    LocalDate to = toDatePicker.getValue();
    if (from != null && to != null && from.isAfter(to)) {
      new Alert(Alert.AlertType.WARNING,
          "'From' date cannot be after 'To' date.").showAndWait();
      return;
    }
    loadData();
  }

  @FXML
  private void onClear() {
    fromDatePicker.setValue(LocalDate.now().minusDays(90));
    toDatePicker.setValue(LocalDate.now());
    channelComboBox.setValue("All");
    loadData();
  }

  private void loadData() {
    LocalDate from = fromDatePicker.getValue();
    LocalDate to = toDatePicker.getValue();
    String channel = "All".equals(channelComboBox.getValue()) ? null : channelComboBox.getValue();

    List<Probability> data = probabilityDAO.findByPeriod(from, to, channel);
    currentData.setAll(data);

    updateKpis();
    updateChart();

    statusLabel.setText(String.format("Showing %d channel(s). Period: %s -> %s.",
        currentData.size(), from, to));
  }

  /**
   * Temporary sample data. Remove once the service is wired up.
   */
  private ObservableList<Probability> buildSampleData(String channelFilter) {
    ObservableList<Probability> data = FXCollections.observableArrayList(
        new Probability("Booking", 420, 118),
        new Probability("Airbnb", 310, 52),
        new Probability("Expedia", 180, 63),
        new Probability("Direct", 260, 21),
        new Probability("Phone", 95, 12),
        new Probability("Walk-in", 40, 1));
    if (channelFilter == null || channelFilter.isBlank())
      return data;

    ObservableList<Probability> filtered = FXCollections.observableArrayList();
    for (Probability p : data) {
      if (channelFilter.equalsIgnoreCase(p.getChannel()))
        filtered.add(p);
    }
    return filtered;
  }

  private void updateKpis() {
    int total = currentData.stream().mapToInt(Probability::getTotalReservations).sum();
    int cancelled = currentData.stream().mapToInt(Probability::getCancelledReservations).sum();
    double probability = total == 0 ? 0 : (double) cancelled / total;

    totalReservationsLabel.setText(String.valueOf(total));
    cancelledReservationsLabel.setText(String.valueOf(cancelled));
    globalProbabilityLabel.setText(String.format("%.2f %%", probability * 100));
    globalProbabilityBar.setProgress(probability);
  }

  private void updateChart() {
    probabilityChart.getData().clear();
    XYChart.Series<String, Number> series = new XYChart.Series<>();

    for (Probability p : currentData) {
      XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(p.getChannel(), p.getProbability() * 100);
      series.getData().add(dataPoint);
    }
    probabilityChart.getData().add(series);

    for (XYChart.Data<String, Number> dataPoint : series.getData()) {
      if (dataPoint.getNode() == null)
        continue;
      Probability p = currentData.stream()
          .filter(x -> x.getChannel().equals(dataPoint.getXValue()))
          .findFirst().orElse(null);
      if (p != null) {
        Tooltip.install(dataPoint.getNode(), new Tooltip(
            p.getChannel() + "\n" +
                "Reservations: " + p.getTotalReservations() + "\n" +
                "Cancelled: " + p.getCancelledReservations() + "\n" +
                "Probability: " + String.format("%.2f %%", p.getProbability() * 100) + "\n" +
                "95% CI: " + p.getFormattedConfidenceInterval()));
      }
    }
  }

  // ============================================================
  @FXML
  private void onExport() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Export report");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
    fileChooser.setInitialFileName("cancellation_report.csv");
    File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
    if (file == null)
      return;

    try (PrintWriter writer = new PrintWriter(file)) {
      writer.println("Channel;Total reservations;Cancelled;Probability;95% CI lower;95% CI upper");
      for (Probability p : currentData) {
        writer.printf("%s;%d;%d;%.4f;%.4f;%.4f%n",
            p.getChannel(), p.getTotalReservations(), p.getCancelledReservations(),
            p.getProbability(), p.getConfidenceIntervalLower(), p.getConfidenceIntervalUpper());
      }
      statusLabel.setText("Exported to: " + file.getAbsolutePath());
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error while exporting: " + ex.getMessage()).showAndWait();
    }
  }
}
