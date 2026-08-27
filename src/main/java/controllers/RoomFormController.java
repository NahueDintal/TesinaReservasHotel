package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import models.Room;
import repositories.RoomDAO;

public class RoomFormController {

    @FXML
    private TextField txtNumber;
    @FXML
    private TextField txtFloor;
    @FXML
    private TextField txtType;
    @FXML
    private TextField txtCapacity;
    @FXML
    private TextField txtView;
    @FXML
    private TextField txtFeatures;
    @FXML
    private TextField txtPrice;
    @FXML
    private TextField txtDescription;
    @FXML
    private CheckBox chkAvailable;
    @FXML
    private CheckBox chkOutOfService;

    private RoomDAO roomDAO = new RoomDAO();
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    private void handleSave() {
        try {
            Room room = new Room(
                    txtNumber.getText(),
                    txtFloor.getText(),
                    txtType.getText(),
                    txtCapacity.getText(),
                    txtView.getText(),
                    txtFeatures.getText(),
                    txtPrice.getText(),
                    txtDescription.getText()
            );
            room.setIsAvailable(chkAvailable.isSelected());
            room.setOutOfService(chkOutOfService.isSelected());

            roomDAO.insert(room);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText("Habitación creada correctamente.");
            alert.showAndWait();

            // Volver a la lista de habitaciones
            if (dashboardController != null) {
                dashboardController.loadView("/views/Room.fxml");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo guardar la habitación");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        // Volver a la lista sin guardar
        if (dashboardController != null) {
            dashboardController.loadView("/views/Room.fxml");
        }
    }
}
