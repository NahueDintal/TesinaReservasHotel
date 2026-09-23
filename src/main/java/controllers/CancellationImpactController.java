package controllers;

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
import models.CancellationImpact;
import repositories.CancellationImpactDAO;
import repositories.ProbabilityDAO;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

public class CancellationImpactController {

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

  @FXML
  private Label totalReservationsLabel;
  @FXML
  private Label cancelledReservationsLabel;
  @FXML
  private Label totalRevenueLabel;
  @FXML
  private Label lostRevenueLabel;
  @FXML
  private Label lostRatioLabel;

  @FXML
  private BarChart<String, Number> revenueChart;
  @FXML
  private CategoryAxis channelAxis;
  @FXML
  private NumberAxis revenueAxis;

  @FXML
  private TableView<CancellationImpact> dataTable;
  @FXML
  private TableColumn<CancellationImpact, String> channelColumn;
  @FXML
  private TableColumn<CancellationImpact, Integer> totalColumn;
  @FXML
  private TableColumn<CancellationImpact, Integer> cancelledColumn;
  @FXML
  private TableColumn<CancellationImpact, Double> totalRevenueColumn;
  @FXML
  private TableColumn<CancellationImpact, Double> lostRevenueColumn;
  @FXML
  private TableColumn<CancellationImpact, Double> lostRatioColumn;

  @FXML
  private Label statusLabel;

  private final CancellationImpactDAO impactDAO = new CancellationImpactDAO();
  private final ProbabilityDAO probabilityDAO = new ProbabilityDAO();
  private final ObservableList<CancellationImpact> currentData = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    toDatePicker.setValue(LocalDate.now().plusDays(90));
    fromDatePicker.setValue(LocalDate.now().minusDays(90));

    ObservableList<String> channels = FXCollections.observableArrayList("All");
    channels.addAll(probabilityDAO.findAllChannels());
    channelComboBox.setItems(channels);
    channelComboBox.setValue("All");

    configureTable();
    loadData();
  }

  private void configureTable() {
    channelColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCategory()));
    totalColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTotalReservations()));
    cancelledColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCancelledReservations()));
    totalRevenueColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTotalRevenue()));
    lostRevenueColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getLostRevenue()));
    lostRatioColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getLostRatio()));

    totalRevenueColumn.setCellFactory(c -> moneyCell("#27ae60"));
    lostRevenueColumn.setCellFactory(c -> moneyCell("#c0392b"));
    lostRatioColumn.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(Double v, boolean empty) {
        super.updateItem(v, empty);
        if (empty || v == null) {
          setText(null);
          return;
        }
        setText(String.format("%.2f %%", v * 100));
      }
    });

    dataTable.setItems(currentData);
  }

  private TableCell<CancellationImpact, Double> moneyCell(String color) {
    return new TableCell<>() {
      @Override
      protected void updateItem(Double v, boolean empty) {
        super.updateItem(v, empty);
        if (empty || v == null) {
          setText(null);
          return;
        }
        setText(String.format("%,.2f", v));
        setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
      }
    };
  }

  @FXML
  private void onCalculate() {
    if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null
        && fromDatePicker.getValue().isAfter(toDatePicker.getValue())) {
      new Alert(Alert.AlertType.WARNING, "'From' date cannot be after 'To' date.").showAndWait();
      return;
    }
    loadData();
  }

  @FXML
  private void onClear() {
    fromDatePicker.setValue(LocalDate.now().minusDays(90));
    toDatePicker.setValue(LocalDate.now().plusDays(90));
    channelComboBox.setValue("All");
    loadData();
  }

  private void loadData() {
    LocalDate from = fromDatePicker.getValue();
    LocalDate to = toDatePicker.getValue();
    String channel = "All".equals(channelComboBox.getValue()) ? null : channelComboBox.getValue();

    List<CancellationImpact> data = impactDAO.findByChannel(from, to, channel);
    currentData.setAll(data);

    updateKpis();
    updateChart();

    statusLabel.setText(String.format("Showing %d channel(s). Period: %s -> %s.",
        currentData.size(), from, to));
  }

  private void updateKpis() {
    int total = currentData.stream().mapToInt(CancellationImpact::getTotalReservations).sum();
    int cancelled = currentData.stream().mapToInt(CancellationImpact::getCancelledReservations).sum();
    double totalRev = currentData.stream().mapToDouble(CancellationImpact::getTotalRevenue).sum();
    double lostRev = currentData.stream().mapToDouble(CancellationImpact::getLostRevenue).sum();
    double ratio = totalRev == 0 ? 0 : lostRev / totalRev;

    totalReservationsLabel.setText(String.valueOf(total));
    cancelledReservationsLabel.setText(String.valueOf(cancelled));
    totalRevenueLabel.setText(String.format("%,.2f", totalRev));
    lostRevenueLabel.setText(String.format("%,.2f", lostRev));
    lostRatioLabel.setText(String.format("%.2f %% of total revenue", ratio * 100));
  }

  private void updateChart() {
    revenueChart.getData().clear();
    XYChart.Series<String, Number> series = new XYChart.Series<>();

    for (CancellationImpact p : currentData) {
      series.getData().add(new XYChart.Data<>(p.getCategory(), p.getLostRevenue()));
    }
    revenueChart.getData().add(series);

    for (XYChart.Data<String, Number> d : series.getData()) {
      if (d.getNode() == null)
        continue;
      CancellationImpact p = currentData.stream()
          .filter(x -> x.getCategory().equals(d.getXValue()))
          .findFirst().orElse(null);
      if (p != null) {
        Tooltip.install(d.getNode(), new Tooltip(
            p.getCategory() + "\n" +
                "Reservations: " + p.getTotalReservations() + "\n" +
                "Cancelled: " + p.getCancelledReservations() + "\n" +
                "Total revenue: " + String.format("%,.2f", p.getTotalRevenue()) + "\n" +
                "Lost revenue: " + String.format("%,.2f", p.getLostRevenue()) + "\n" +
                "Lost ratio: " + p.getLostRatioFormatted()));
      }
    }
  }

  @FXML
  private void onExport() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Export report");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
    fc.setInitialFileName("cancellation_impact.csv");
    File file = fc.showSaveDialog(exportButton.getScene().getWindow());
    if (file == null)
      return;

    try (PrintWriter pw = new PrintWriter(file)) {
      pw.println("Channel;Reservations;Cancelled;Total revenue;Lost revenue;Lost ratio");
      for (CancellationImpact p : currentData) {
        pw.printf("%s;%d;%d;%.2f;%.2f;%.4f%n",
            p.getCategory(), p.getTotalReservations(), p.getCancelledReservations(),
            p.getTotalRevenue(), p.getLostRevenue(), p.getLostRatio());
      }
      statusLabel.setText("Exported to: " + file.getAbsolutePath());
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error while exporting: " + ex.getMessage()).showAndWait();
    }
  }
}
