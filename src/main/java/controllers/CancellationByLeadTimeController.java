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
import models.Probability;
import repositories.ProbabilityDAO;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

public class CancellationByLeadTimeController {

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
  private Label highestRiskLabel;
  @FXML
  private Label highestRiskDetailLabel;

  @FXML
  private BarChart<String, Number> leadTimeChart;
  @FXML
  private CategoryAxis leadTimeAxis;
  @FXML
  private NumberAxis probabilityAxis;

  @FXML
  private TableView<Probability> dataTable;
  @FXML
  private TableColumn<Probability, String> bucketColumn;
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

  @FXML
  public void initialize() {
    fromDatePicker.setValue(LocalDate.now().minusMonths(12));
    toDatePicker.setValue(LocalDate.now().plusMonths(12));

    ObservableList<String> channels = FXCollections.observableArrayList("Todos");
    channels.addAll(probabilityDAO.findAllChannels());
    channelComboBox.setItems(channels);
    channelComboBox.setValue("Todos");

    configureTable();
    loadData();
  }

  private void configureTable() {
    bucketColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCategory()));
    totalColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTotalReservations()));
    cancelledColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCancelledReservations()));
    probabilityColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getProbability()));
    visualColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getProbability()));
    confidenceIntervalColumn
        .setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getFormattedConfidenceInterval()));

    probabilityColumn.setCellFactory(c -> new TableCell<>() {
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

    visualColumn.setCellFactory(c -> new TableCell<>() {
      private final ProgressBar bar = new ProgressBar(0);

      @Override
      protected void updateItem(Double v, boolean empty) {
        super.updateItem(v, empty);
        if (empty || v == null) {
          setGraphic(null);
          return;
        }
        bar.setProgress(v);
        bar.setMaxWidth(Double.MAX_VALUE);
        setGraphic(bar);
      }
    });

    dataTable.setItems(currentData);
  }

  @FXML
  private void onCalculate() {
    if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null
        && fromDatePicker.getValue().isAfter(toDatePicker.getValue())) {
      new Alert(Alert.AlertType.WARNING,
          "'Desde' no puede ser posterior a 'Hasta'.").showAndWait();
      return;
    }
    loadData();
  }

  @FXML
  private void onClear() {
    fromDatePicker.setValue(LocalDate.now().minusMonths(12));
    toDatePicker.setValue(LocalDate.now().plusMonths(12));
    channelComboBox.setValue("Todos");
    loadData();
  }

  private void loadData() {
    LocalDate from = fromDatePicker.getValue();
    LocalDate to = toDatePicker.getValue();
    String channel = "Todos".equals(channelComboBox.getValue()) ? null : channelComboBox.getValue();

    List<Probability> data = probabilityDAO.findByLeadTime(from, to, channel);
    currentData.setAll(data);

    updateKpis();
    updateChart();

    statusLabel.setText(String.format("Mostrando %d rango(s). Período: %s → %s.",
        currentData.size(), from, to));
  }

  private void updateKpis() {
    int total = currentData.stream().mapToInt(Probability::getTotalReservations).sum();
    int cancelled = currentData.stream().mapToInt(Probability::getCancelledReservations).sum();

    totalReservationsLabel.setText(String.valueOf(total));
    cancelledReservationsLabel.setText(String.valueOf(cancelled));

    Probability riskiest = currentData.stream()
        .filter(p -> p.getTotalReservations() > 0)
        .max((a, b) -> Double.compare(a.getProbability(), b.getProbability()))
        .orElse(null);

    if (riskiest != null) {
      highestRiskLabel.setText(String.format("%.2f %%", riskiest.getProbability() * 100));
      highestRiskDetailLabel.setText(riskiest.getCategory()
          + "  (" + riskiest.getCancelledReservations()
          + "/" + riskiest.getTotalReservations() + ")");
    } else {
      highestRiskLabel.setText("—");
      highestRiskDetailLabel.setText("");
    }
  }

  private void updateChart() {
    leadTimeChart.getData().clear();
    XYChart.Series<String, Number> series = new XYChart.Series<>();

    for (Probability p : currentData) {
      series.getData().add(new XYChart.Data<>(p.getCategory(), p.getProbability() * 100));
    }
    leadTimeChart.getData().add(series);

    for (XYChart.Data<String, Number> d : series.getData()) {
      if (d.getNode() == null)
        continue;
      Probability p = currentData.stream()
          .filter(x -> x.getCategory().equals(d.getXValue()))
          .findFirst().orElse(null);
      if (p != null) {
        Tooltip.install(d.getNode(), new Tooltip(
            p.getCategory() + "\n" +
                "Reservas: " + p.getTotalReservations() + "\n" +
                "Canceladas: " + p.getCancelledReservations() + "\n" +
                "Probabilidad: " + String.format("%.2f %%", p.getProbability() * 100) + "\n" +
                "IC 95%: " + p.getFormattedConfidenceInterval()));
      }
    }
  }

  @FXML
  private void onExport() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Exportar reporte");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
    fc.setInitialFileName("cancelaciones_por_anticipacion.csv");
    File file = fc.showSaveDialog(exportButton.getScene().getWindow());
    if (file == null)
      return;

    try (PrintWriter pw = new PrintWriter(file)) {
      pw.println("Anticipación;Reservas;Canceladas;Probabilidad;IC 95% inferior;IC 95% superior");
      for (Probability p : currentData) {
        pw.printf("%s;%d;%d;%.4f;%.4f;%.4f%n",
            p.getCategory(), p.getTotalReservations(), p.getCancelledReservations(),
            p.getProbability(), p.getConfidenceIntervalLower(), p.getConfidenceIntervalUpper());
      }
      statusLabel.setText("Exportado a: " + file.getAbsolutePath());
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al exportar: " + ex.getMessage()).showAndWait();
    }
  }
}
