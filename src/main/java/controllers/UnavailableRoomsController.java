package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Room;
import repositories.RoomDAO;

import java.sql.SQLException;

public class UnavailableRoomsController {

  @FXML
  private TableView<Room> tableUnavailableRooms;
  @FXML
  private TableColumn<Room, Integer> colNumber;
  @FXML
  private TableColumn<Room, Integer> colFloor;
  @FXML
  private TableColumn<Room, String> colType;
  @FXML
  private TableColumn<Room, Integer> colCapacity;
  @FXML
  private TableColumn<Room, Double> colPrice;

  @FXML
  private Button btnReactivate;
  @FXML
  private TextField txtSearch;
  @FXML
  private Label lblTotalUnavailable;

  private RoomDAO roomDAO = new RoomDAO();
  private ObservableList<Room> unavailableRooms = FXCollections.observableArrayList();
  private FilteredList<Room> filteredUnavailable;

  @FXML
  public void initialize() {
    colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
    colFloor.setCellValueFactory(new PropertyValueFactory<>("floor"));
    colType.setCellValueFactory(new PropertyValueFactory<>("typeName")); // <-- corregido
    colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

    loadUnavailableRooms();

    txtSearch.textProperty().addListener((obs, old, newVal) -> {
      filteredUnavailable.setPredicate(room -> {
        if (newVal == null || newVal.isEmpty()) return true;
        String lower = newVal.toLowerCase();
        return String.valueOf(room.getNumber()).contains(lower) ||
                String.valueOf(room.getFloor()).contains(lower) ||
                (room.getTypeName() != null && room.getTypeName().toLowerCase().contains(lower)) || // <-- corregido
                String.valueOf(room.getCapacity()).contains(lower) ||
                String.valueOf(room.getPrice()).contains(lower);
      });
      updateCounter();
    });

    btnReactivate.setDisable(true);
    tableUnavailableRooms.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, newVal) -> btnReactivate.setDisable(newVal == null));

    btnReactivate.setOnAction(e -> reactivateRoom());
  }

  private void loadUnavailableRooms() {
    try {
      unavailableRooms.setAll(roomDAO.listActive());
      unavailableRooms.removeIf(Room::isAvailable); // solo no disponibles
      filteredUnavailable = new FilteredList<>(unavailableRooms, p -> true);
      tableUnavailableRooms.setItems(filteredUnavailable);
      updateCounter();
    } catch (RuntimeException e) {
      showAlert("Error", "No se pudieron cargar las habitaciones no disponibles", e.getMessage());
    }
  }

  private void reactivateRoom() {
    Room selected = tableUnavailableRooms.getSelectionModel().getSelectedItem();
    if (selected == null) return;

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Reactivar habitación");
    alert.setHeaderText("¿Desea volver a marcar como disponible esta habitación?");
    alert.setContentText("La habitación " + selected.getNumber() + " estará disponible nuevamente.");
    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        try {
          selected.setAvailable(true);
          if (roomDAO.update(selected)) {
            unavailableRooms.remove(selected);
            filteredUnavailable.remove(selected);
            tableUnavailableRooms.refresh();
            updateCounter();
            showAlert("Éxito", "Habitación reactivada", "La habitación ha sido reactivada correctamente.");
            Stage stage = (Stage) tableUnavailableRooms.getScene().getWindow();
            stage.close();
          }
        } catch (SQLException e) {
          showAlert("Error", "No se pudo reactivar la habitación", e.getMessage());
        }
      }
    });
  }

  private void updateCounter() {
    int count = filteredUnavailable != null ? filteredUnavailable.size() : 0;
    lblTotalUnavailable.setText("Mostrando " + count + " habitaciones no disponibles");
  }

  private void showAlert(String title, String header, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }
}