package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Room;
import repositories.RoomDAO; // Ajusta el paquete según corresponda

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
    private RoomController parentController;

    public void setParentController(RoomController controller) {
        this.parentController = controller;
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

            if (parentController != null) {
                parentController.cargarHabitaciones();
            }

            Stage stage = (Stage) txtNumber.getScene().getWindow();
            stage.close();

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
        Stage stage = (Stage) txtNumber.getScene().getWindow();
        stage.close();
    }
}
