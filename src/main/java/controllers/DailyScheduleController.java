package controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import models.DailyMovement;
import repositories.DailyMovementDAO;
//import utils.DatePickerFormatter;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DailyScheduleController {

  @FXML
  private DatePicker datePicker;
  @FXML
  private Button todayButton;
  @FXML
  private Button calculateButton;
  @FXML
  private Button exportButton;

  @FXML
  private Label checkInCountLabel;
  @FXML
  private Label checkOutCountLabel;
  @FXML
  private Label guestsArrivingLabel;
  @FXML
  private Label guestsDepartingLabel;

  @FXML
  private TableView<DailyMovement> checkInTable;
  @FXML
  private TableColumn<DailyMovement, Integer> ciIdColumn;
  @FXML
  private TableColumn<DailyMovement, String> ciCustomerColumn;
  @FXML
  private TableColumn<DailyMovement, Integer> ciGuestsColumn;
  @FXML
  private TableColumn<DailyMovement, Double> ciRateColumn;
  @FXML
  private TableColumn<DailyMovement, String> ciStatusColumn;
  @FXML
  private TableColumn<DailyMovement, String> ciObsColumn;

  @FXML
  private TableView<DailyMovement> checkOutTable;
  @FXML
  private TableColumn<DailyMovement, Integer> coIdColumn;
  @FXML
  private TableColumn<DailyMovement, String> coCustomerColumn;
  @FXML
  private TableColumn<DailyMovement, Integer> coGuestsColumn;
  @FXML
  private TableColumn<DailyMovement, Double> coRateColumn;
  @FXML
  private TableColumn<DailyMovement, String> coStatusColumn;
  @FXML
  private TableColumn<DailyMovement, String> coObsColumn;

  @FXML
  private Label checkInFooterLabel;
  @FXML
  private Label checkOutFooterLabel;
  @FXML
  private Label statusLabel;

  private final DailyMovementDAO movementDAO = new DailyMovementDAO();
  private final ObservableList<DailyMovement> checkIns = FXCollections.observableArrayList();
  private final ObservableList<DailyMovement> checkOuts = FXCollections.observableArrayList();

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  @FXML
  public void initialize() {
    // DatePickerFormatter.apply(datePicker);
    datePicker.setValue(LocalDate.now());

    configureTable(checkInTable, ciIdColumn, ciCustomerColumn, ciGuestsColumn,
        ciRateColumn, ciStatusColumn, ciObsColumn, checkIns);
    configureTable(checkOutTable, coIdColumn, coCustomerColumn, coGuestsColumn,
        coRateColumn, coStatusColumn, coObsColumn, checkOuts);

    loadData();
  }

  private void configureTable(TableView<DailyMovement> table,
      TableColumn<DailyMovement, Integer> idCol,
      TableColumn<DailyMovement, String> customerCol,
      TableColumn<DailyMovement, Integer> guestsCol,
      TableColumn<DailyMovement, Double> rateCol,
      TableColumn<DailyMovement, String> statusCol,
      TableColumn<DailyMovement, String> obsCol,
      ObservableList<DailyMovement> items) {

    idCol.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getIdReservation()));
    customerCol.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCustomerName()));
    guestsCol.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getNumberOfGuests()));
    rateCol.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTotalRate()));
    statusCol.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getReservationStatus()));
    obsCol.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(
        d.getValue().getObservations() != null ? d.getValue().getObservations() : ""));

    rateCol.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(Double v, boolean empty) {
        super.updateItem(v, empty);
        setText(empty || v == null ? "" : String.format("$ %,.2f", v));
      }
    });

    table.setItems(items);
  }

  @FXML
  private void onCalculate() {
    if (datePicker.getValue() == null) {
      new Alert(Alert.AlertType.WARNING, "Seleccione una fecha.").showAndWait();
      return;
    }
    loadData();
  }

  @FXML
  private void onToday() {
    datePicker.setValue(LocalDate.now());
    loadData();
  }

  private void loadData() {
    LocalDate date = datePicker.getValue();

    List<DailyMovement> ins = movementDAO.findCheckInsByDate(date);
    List<DailyMovement> outs = movementDAO.findCheckOutsByDate(date);

    checkIns.setAll(ins);
    checkOuts.setAll(outs);

    updateKpis(ins, outs);

    String formatted = date.format(DATE_FMT);
    statusLabel.setText(String.format("Cronograma del %s cargado.", formatted));
    checkInFooterLabel.setText(ins.size() + " check-in(s) el " + formatted);
    checkOutFooterLabel.setText(outs.size() + " check-out(s) el " + formatted);
  }

  private void updateKpis(List<DailyMovement> ins, List<DailyMovement> outs) {
    int totalIn = ins.size();
    int totalOut = outs.size();
    int guestsIn = ins.stream().mapToInt(DailyMovement::getNumberOfGuests).sum();
    int guestsOut = outs.stream().mapToInt(DailyMovement::getNumberOfGuests).sum();

    checkInCountLabel.setText(String.valueOf(totalIn));
    checkOutCountLabel.setText(String.valueOf(totalOut));
    guestsArrivingLabel.setText(String.valueOf(guestsIn));
    guestsDepartingLabel.setText(String.valueOf(guestsOut));
  }

  @FXML
  private void onExport() {
    if (datePicker.getValue() == null)
      return;

    FileChooser fc = new FileChooser();
    fc.setTitle("Exportar cronograma");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
    String dateStr = datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    fc.setInitialFileName("cronograma_" + dateStr + ".csv");

    File file = fc.showSaveDialog(exportButton.getScene().getWindow());
    if (file == null)
      return;

    try (PrintWriter pw = new PrintWriter(file)) {
      pw.println("Tipo;Reserva;Huésped;Huéspedes;Tarifa;Estado;Observaciones");
      for (DailyMovement m : checkIns) {
        pw.printf("Check-in;%d;%s;%d;%.2f;%s;%s%n",
            m.getIdReservation(), m.getCustomerName(), m.getNumberOfGuests(),
            m.getTotalRate(), m.getReservationStatus(),
            m.getObservations() != null ? m.getObservations() : "");
      }
      for (DailyMovement m : checkOuts) {
        pw.printf("Check-out;%d;%s;%d;%.2f;%s;%s%n",
            m.getIdReservation(), m.getCustomerName(), m.getNumberOfGuests(),
            m.getTotalRate(), m.getReservationStatus(),
            m.getObservations() != null ? m.getObservations() : "");
      }
      statusLabel.setText("Exportado a: " + file.getAbsolutePath());
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Error al exportar: " + ex.getMessage()).showAndWait();
    }
  }
}
