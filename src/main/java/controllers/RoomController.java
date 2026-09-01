package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Room;
import repositories.RoomDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class RoomController {

  @FXML
  private TableView<Room> tableRooms;
  @FXML
  private TableColumn<Room, Integer> colNumber;
  @FXML
  private TableColumn<Room, Integer> colFloor;
  @FXML
  private TableColumn<Room, String> colType;
  @FXML
  private TableColumn<Room, Integer> colCapacity;
  @FXML
  private TableColumn<Room, String> colView;
  @FXML
  private TableColumn<Room, Double> colPrice;
  @FXML
  private TableColumn<Room, Boolean> colAvailable;

  @FXML
  private Button btnNewRoom;
  @FXML
  private Button btnViewUnavailable;
  @FXML
  private Button btnEdit;
  @FXML
  private Button btnDeactivate;

  @FXML
  private Label lblDetailNumber;
  @FXML
  private Label lblDetailFloor;
  @FXML
  private Label lblDetailType;
  @FXML
  private Label lblDetailCapacity;
  @FXML
  private Label lblDetailView;
  @FXML
  private Label lblDetailPrice;
  @FXML
  private Label lblDetailFeatures;
  @FXML
  private Label lblDetailDescription;
  @FXML
  private Label lblDetailStatus;

  @FXML
  private TextField txtSearch;
  @FXML
  private Label lblTotalRooms;

  private RoomDAO roomDAO = new RoomDAO();
  private ObservableList<Room> masterRoomList = FXCollections.observableArrayList();
  private FilteredList<Room> filteredRooms;

  @FXML
  public void initialize() {
    // Configurar columnas (usar nombres correctos del modelo)
    colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
    colFloor.setCellValueFactory(new PropertyValueFactory<>("floor"));
    colType.setCellValueFactory(new PropertyValueFactory<>("typeName"));
    colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
    colView.setCellValueFactory(new PropertyValueFactory<>("viewName"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
    colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

    // Formato de precio
    colPrice.setCellFactory(tc -> new TableCell<>() {
      @Override
      protected void updateItem(Double price, boolean empty) {
        super.updateItem(price, empty);
        setText(empty || price == null ? "" : String.format("$ %.2f", price));
      }
    });

    // Formato de disponibilidad
    colAvailable.setCellFactory(tc -> new TableCell<>() {
      @Override
      protected void updateItem(Boolean available, boolean empty) {
        super.updateItem(available, empty);
        setText(empty || available == null ? "" : (available ? "Disponible" : "No disponible"));
        setStyle(empty || available == null ? "" : (available ? "-fx-text-fill: green;" : "-fx-text-fill: red;"));
      }
    });

    // Cargar habitaciones (solo disponibles inicialmente)
    loadRooms(true);

    // Buscador
    txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
      filteredRooms.setPredicate(room -> {
        if (newVal == null || newVal.isEmpty()) return true;
        String lower = newVal.toLowerCase();
        // Convertir lista de características a una sola cadena para buscar
        String featuresStr = room.getFeatures() != null ? String.join(" ", room.getFeatures()).toLowerCase() : "";
        return String.valueOf(room.getNumber()).contains(lower) ||
                String.valueOf(room.getFloor()).contains(lower) ||
                (room.getTypeName() != null && room.getTypeName().toLowerCase().contains(lower)) ||
                String.valueOf(room.getCapacity()).contains(lower) ||
                (room.getViewName() != null && room.getViewName().toLowerCase().contains(lower)) ||
                String.valueOf(room.getPrice()).contains(lower) ||
                featuresStr.contains(lower) ||
                (room.getDescription() != null && room.getDescription().toLowerCase().contains(lower));
      });
      updateCounter();
    });

    // Selección en tabla
    tableRooms.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
      if (newVal != null) showDetail(newVal);
      else clearDetail();
    });

    // Habilitar/deshabilitar botones de edición y desactivación
    btnEdit.setDisable(true);
    btnDeactivate.setDisable(true);
    tableRooms.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
      boolean selected = newVal != null;
      btnEdit.setDisable(!selected);
      btnDeactivate.setDisable(!selected);
    });

    // Acciones
    btnNewRoom.setOnAction(e -> openRoomForm(null));
    btnViewUnavailable.setOnAction(e -> openUnavailableRoomsWindow());
    btnEdit.setOnAction(e -> openRoomForm(tableRooms.getSelectionModel().getSelectedItem()));
    btnDeactivate.setOnAction(e -> deactivateRoom());
  }

  private void loadRooms(boolean onlyAvailable) {
    try {
      masterRoomList.setAll(roomDAO.listActive());
      if (onlyAvailable) {
        masterRoomList.removeIf(room -> !room.isAvailable());
      }
      filteredRooms = new FilteredList<>(masterRoomList, p -> true);
      tableRooms.setItems(filteredRooms);
      updateCounter();
    } catch (RuntimeException e) {
      showAlert("Error", "No se pudieron cargar las habitaciones", e.getMessage());
    }
  }

  private void showDetail(Room r) {
    lblDetailNumber.setText(String.valueOf(r.getNumber()));
    lblDetailFloor.setText(String.valueOf(r.getFloor()));
    lblDetailType.setText(r.getTypeName() != null ? r.getTypeName() : "--");
    lblDetailCapacity.setText(String.valueOf(r.getCapacity()));
    lblDetailView.setText(r.getViewName() != null ? r.getViewName() : "--");
    lblDetailPrice.setText(String.format("$ %.2f", r.getPrice()));
    lblDetailFeatures.setText(r.getFeatures() != null ? String.join(", ", r.getFeatures()) : "--");
    lblDetailDescription.setText(r.getDescription() != null ? r.getDescription() : "--");
    lblDetailStatus.setText(r.isAvailable() ? "Disponible" : "No disponible");
    lblDetailStatus.setStyle(r.isAvailable() ? "-fx-text-fill: green; -fx-font-weight: bold;"
            : "-fx-text-fill: red; -fx-font-weight: bold;");
  }

  private void clearDetail() {
    lblDetailNumber.setText("--");
    lblDetailFloor.setText("--");
    lblDetailType.setText("--");
    lblDetailCapacity.setText("--");
    lblDetailView.setText("--");
    lblDetailPrice.setText("--");
    lblDetailFeatures.setText("--");
    lblDetailDescription.setText("--");
    lblDetailStatus.setText("");
  }

  private void openRoomForm(Room room) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RoomFormView.fxml"));
      Stage stage = new Stage();
      stage.setScene(new Scene(loader.load()));
      stage.initModality(Modality.WINDOW_MODAL);
      stage.initOwner(tableRooms.getScene().getWindow());
      stage.setTitle(room == null ? "Nueva Habitación" : "Editar Habitación");

      RoomFormController controller = loader.getController();
      if (room != null) controller.setRoom(room);

      stage.showAndWait();
      loadRooms(true);
      tableRooms.refresh();
      updateCounter();
    } catch (IOException e) {
      showAlert("Error", "No se pudo abrir el formulario", e.getMessage());
    }
  }

  private void openUnavailableRoomsWindow() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UnavailableRoomsView.fxml"));
      Stage stage = new Stage();
      stage.setScene(new Scene(loader.load()));
      stage.setTitle("Habitaciones No Disponibles");
      stage.initModality(Modality.WINDOW_MODAL);
      stage.initOwner(tableRooms.getScene().getWindow());
      stage.showAndWait();
      loadRooms(true);
      tableRooms.refresh();
      updateCounter();
    } catch (IOException e) {
      showAlert("Error", "No se pudo abrir la ventana de no disponibles", e.getMessage());
    }
  }

  private void deactivateRoom() {
    Room selected = tableRooms.getSelectionModel().getSelectedItem();
    if (selected == null) return;

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Marcar como no disponible");
    alert.setHeaderText("¿Desea marcar esta habitación como no disponible?");
    alert.setContentText("La habitación " + selected.getNumber() + " no podrá ser reservada.");
    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        try {
          selected.setAvailable(false);
          if (roomDAO.update(selected)) {
            masterRoomList.remove(selected);
            filteredRooms.remove(selected);
            tableRooms.refresh();
            clearDetail();
            updateCounter();
            showAlert("Éxito", "Habitación actualizada", "");
          }
        } catch (SQLException e) {
          showAlert("Error", "No se pudo actualizar", e.getMessage());
        }
      }
    });
  }

  private void updateCounter() {
    lblTotalRooms.setText("Mostrando " + (filteredRooms != null ? filteredRooms.size() : 0) + " habitaciones");
  }

  private void showAlert(String title, String header, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }
}