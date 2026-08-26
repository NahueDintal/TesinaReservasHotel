package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import models.Room;
import repositories.RoomDAO;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RoomController implements Initializable {

    @FXML
    private TableView<Room> tablaHabitaciones;

    private RoomDAO roomDAO = new RoomDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarHabitaciones();
    }

    public void cargarHabitaciones() {
        try {
            List<Room> lista = roomDAO.listAll();
            ObservableList<Room> habitaciones = FXCollections.observableArrayList(lista);
            tablaHabitaciones.setItems(habitaciones);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleInsertRoom() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RoomForm.fxml"));
            Parent root = loader.load();

            RoomFormController formController = loader.getController();
            formController.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Nueva Habitación");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Al cerrar, recargar la tabla
            cargarHabitaciones();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
