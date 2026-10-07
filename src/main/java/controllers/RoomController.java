package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import models.OccupiedInterval;
import repositories.RoomOccupancyDAO;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.IOException;

public class RoomController {

  private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

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
  private Button btnOutOfService; // Marcar fuera de servicio
  @FXML
  private Button btnDelete;

  @FXML
  private TextField lblDetailNumber;
  @FXML
  private TextField lblDetailFloor;
  @FXML
  private TextField lblDetailType;
  @FXML
  private TextField lblDetailCapacity;
  @FXML
  private TextField lblDetailView;
  @FXML
  private TextField lblDetailPrice;
  @FXML
  private TextField lblDetailFeatures;
  @FXML
  private TextField lblDetailDescription;
  @FXML
  private Label lblDetailStatus;

  @FXML
  private TextField txtSearch;
  @FXML
  private Label lblTotalRooms;

  private final RoomDAO roomDAO = new RoomDAO();
  private final ObservableList<Room> masterRoomList = FXCollections.observableArrayList();
  private FilteredList<Room> filteredRooms;
  private final RoomOccupancyDAO roomOccupancyDAO = new RoomOccupancyDAO();
  private Set<Integer> occupiedTodayRoomNumbers = new HashSet<>();

  @FXML
  public void initialize() {
    colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
    colFloor.setCellValueFactory(new PropertyValueFactory<>("floor"));
    colType.setCellValueFactory(new PropertyValueFactory<>("typeName"));
    colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
    colView.setCellValueFactory(new PropertyValueFactory<>("viewName"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
    colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

    colPrice.setCellFactory(tc -> new TableCell<>() {
      @Override
      protected void updateItem(Double price, boolean empty) {
        super.updateItem(price, empty);
        setText(empty || price == null ? "" : String.format("$ %.2f", price));
      }
    });

    colAvailable.setCellFactory(tc -> new TableCell<Room, Boolean>() {
      @Override
      protected void updateItem(Boolean available, boolean empty) {
        super.updateItem(available, empty);
        Room room = getTableRow() != null ? getTableRow().getItem() : null;
        if (empty || room == null) {
          setText(null);
          setStyle("");
          return;
        }

        boolean occupied = occupiedTodayRoomNumbers.contains(room.getNumber());
        boolean noDisp = !room.isAvailable();

        if (occupied) {
          setText("Ocupada");
          setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
        } else if (noDisp) {
          setText("Fuera de sercio");
          setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
        } else {
          setText("Disponible");
          setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        }
      }
    });

    loadRooms();

    txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
      filteredRooms.setPredicate(room -> {
        if (newVal == null || newVal.isEmpty())
          return true;
        String lower = newVal.toLowerCase();
        String featuresStr = room.getFeatures() != null
            ? String.join(" ", room.getFeatures()).toLowerCase()
            : "";
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

    tableRooms.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
      if (newVal != null) {
        showDetail(newVal);
      } else {
        clearDetail();
      }
      boolean selected = newVal != null;
      btnEdit.setDisable(!selected);
      btnOutOfService.setDisable(!selected);
      btnDelete.setDisable(!selected);
    });

    btnEdit.setDisable(true);
    btnOutOfService.setDisable(true);
    btnDelete.setDisable(true);

    // ---- Wiring ----
    btnNewRoom.setOnAction(e -> openRoomForm(null));
    btnViewUnavailable.setOnAction(e -> openUnavailableRoomsWindow());
    btnEdit.setOnAction(e -> openRoomForm(tableRooms.getSelectionModel().getSelectedItem()));
    btnOutOfService.setOnAction(e -> markAsOutOfService()); // → "fuera de servicio"
    btnDelete.setOnAction(e -> deleteRoom());
  }

  private void loadRooms() {
    try {
      masterRoomList.setAll(roomDAO.listActive());
      masterRoomList.removeIf(Room::isOutOfService);

      LocalDate today = LocalDate.now();
      Map<Integer, List<OccupiedInterval>> occupied = roomOccupancyDAO.findOccupiedInRange(today, today.plusDays(1));
      occupiedTodayRoomNumbers = occupied.keySet();

      filteredRooms = new FilteredList<>(masterRoomList, p -> true);
      tableRooms.setItems(filteredRooms);
      updateCounter();

    } catch (RuntimeException e) {
      logger.error("No se pudieron cargar las habitaciones.", e);
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
    lblDetailFeatures.setText(
        r.getFeatures() != null && !r.getFeatures().isEmpty()
            ? String.join(", ", r.getFeatures())
            : "--");
    lblDetailDescription.setText(r.getDescription() != null ? r.getDescription() : "--");

    boolean occupied = occupiedTodayRoomNumbers.contains(r.getNumber());
    boolean noDisp = !r.isAvailable();

    if (occupied) {
      lblDetailStatus.setText("Ocupada");
      lblDetailStatus.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
    } else if (noDisp) {
      lblDetailStatus.setText("Fuera de servicio");
      lblDetailStatus.setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
    } else {
      lblDetailStatus.setText("Disponible");
      lblDetailStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
    }
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
    lblDetailStatus.setStyle("");
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
      if (room != null)
        controller.setRoom(room);

      stage.showAndWait();
      loadRooms();
      tableRooms.refresh();
      updateCounter();
    } catch (IOException e) {
      logger.error("No se pudo abrir el formulario para room {}", room, e);
      showAlert("Error", "No se pudo abrir el formulario", e.getMessage());
    }
  }

  private void openUnavailableRoomsWindow() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UnavailableRoomsView.fxml"));
      Stage stage = new Stage();
      stage.setScene(new Scene(loader.load()));
      stage.setTitle("Habitaciones Fuera de Servicio");
      stage.initModality(Modality.WINDOW_MODAL);
      stage.initOwner(tableRooms.getScene().getWindow());
      stage.showAndWait();
      loadRooms();
      tableRooms.refresh();
      updateCounter();
    } catch (IOException e) {
      logger.error("No se pudo abrir la ventana de fuera de servicio.", e);
      showAlert("Error", "No se pudo abrir la ventana", e.getMessage());
    }
  }

  private void markAsOutOfService() {
    Room selected = tableRooms.getSelectionModel().getSelectedItem();
    if (selected == null)
      return;

    if (occupiedTodayRoomNumbers.contains(selected.getNumber())) {
      showAlert("No permitido",
          "Habitación ocupada",
          "No se puede marcar como no disponible una habitación ocupada hoy.");
      return;
    }

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Marcar fuera de servicio");
    alert.setHeaderText("¿Desea marcar esta habitación como fuera de servicio?");
    alert.setContentText("La habitación " + selected.getNumber()
        + " quedará inutilizable hasta que la reactives desde la ventana aparte.");
    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        try {
          selected.setOutOfService(true); // ← fuerza available=false internamente
          if (roomDAO.update(selected)) {
            masterRoomList.remove(selected);
            filteredRooms.remove(selected);
            tableRooms.refresh();
            clearDetail();
            updateCounter();
            // showAlert("Éxito", "Habitación fuera de servicio",
            // "Ahora aparece en la ventana de fuera de servicio.");
          }
        } catch (RuntimeException e) {
          logger.error("No se pudo marcar fuera de servicio {}", selected.getNumber(), e);
          showAlert("Error", "No se pudo actualizar", e.getMessage());
        }
      }
    });
  }

  private void deleteRoom() {
    Room selected = tableRooms.getSelectionModel().getSelectedItem();
    if (selected == null)
      return;

    if (occupiedTodayRoomNumbers.contains(selected.getNumber())) {
      showAlert("No permitido",
          "Habitación ocupada",
          "No se puede marcar como no disponible una habitación ocupada hoy.");
      return;
    }

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Eliminar habitación");
    alert.setHeaderText("¿Desea eliminar esta habitación?");
    alert.setContentText("La habitación " + selected.getNumber()
        + " se dará de baja permanentemente.");
    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        try {
          selected.setActive(false);
          if (roomDAO.update(selected)) {
            masterRoomList.remove(selected);
            filteredRooms.remove(selected);
            tableRooms.refresh();
            clearDetail();
            updateCounter();
            // showAlert("Éxito", "Habitación eliminada", "");
          }
        } catch (RuntimeException e) {
          logger.error("No se pudo eliminar habitación {}", selected.getNumber(), e);
          showAlert("Error", "No se pudo eliminar", e.getMessage());
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
