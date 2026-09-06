package controllers;

import models.StaffHistory;
import repositories.StaffHistoryDAO;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class StaffHistoryController {

    @FXML private TableView<StaffHistory> tableHistory;
    @FXML private TableColumn<StaffHistory, String> colChangedAt;
    @FXML private TableColumn<StaffHistory, String> colChangeType;
    @FXML private TableColumn<StaffHistory, String> colDescription;

    private final StaffHistoryDAO staffHistoryDAO = new StaffHistoryDAO();

    @FXML
    public void initialize() {
        colChangedAt.setCellValueFactory(new PropertyValueFactory<>("changedAt"));
        colChangeType.setCellValueFactory(new PropertyValueFactory<>("changeType"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    // Se llama desde StaffController justo después de abrir esta ventana
    public void loadHistory(String idStaff) {
        try {
            tableHistory.setItems(FXCollections.observableArrayList(staffHistoryDAO.findByStaffId(idStaff)));
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo cargar el historial: " + e.getMessage());
            alert.showAndWait();
        }
    }
}