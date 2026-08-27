package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import models.Room;
import repositories.RoomDAO;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RoomController implements Initializable {

    @FXML
    private TableView<Room> tablaHabitaciones;

    private RoomDAO roomDAO = new RoomDAO();
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

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
    public void handleInsertRoom() {
        if (dashboardController != null) {
            dashboardController.loadView("/views/RoomForm.fxml");
        } else {
            System.err.println("DashboardController no disponible");
        }
    }
}
