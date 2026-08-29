package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import models.Reservation;

import java.io.IOException;

public class DashboardController {

    @FXML
    private VBox leftMenu;

    @FXML
    private AnchorPane centerPane;

    @FXML
    private ToggleButton btnPlanilla;

    @FXML
    private ToggleButton btnReservas;

    @FXML
    private ToggleButton btnHabitaciones;

    @FXML
    private ToggleButton btnClientes;

    @FXML
    private ToggleButton btnReportes;

    @FXML
    private ToggleButton btnConfiguracion;

    @FXML
    public void initialize() {
        // Asignar acciones a los botones
        btnPlanilla.setOnAction(e -> loadView("/views/.fxml"));
        btnReservas.setOnAction(e -> loadView("/views/Reservations.fxml"));
        btnHabitaciones.setOnAction(e -> loadView("/views/Room.fxml"));
        btnClientes.setOnAction(e -> loadView("/views/CustomerView.fxml"));
        btnReportes.setOnAction(e -> loadView("/views/.fxml"));
        btnConfiguracion.setOnAction(e -> loadView("/views/.fxml"));
    }

    /**
     * Carga un archivo FXML en el panel central.
     *
     * @param fxmlPath Ruta del archivo FXML (debe comenzar con "/" y estar en
     *                 resources/views)
     */
    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath)
            );

            Parent view = loader.load();

            Object controller = loader.getController();

            if (controller instanceof RoomController) {
                ((RoomController) controller).setDashboardController(this);

            } else if (controller instanceof RoomFormController) {
                ((RoomFormController) controller).setDashboardController(this);

            } else if (controller instanceof ReservationsController) {
                ((ReservationsController) controller)
                        .setDashboardController(this);

            } else if (controller instanceof NewReservationController) {
                ((NewReservationController) controller)
                        .setDashboardController(this);
            }

            centerPane.getChildren().setAll(view);

            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (IOException e) {
            e.printStackTrace();

            AnchorPane errorPane = new AnchorPane();
            centerPane.getChildren().setAll(errorPane);
        }
    }

    public void loadEditReservation(Reservation reservation) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/views/NewReservation.fxml"
                            )
                    );

            Parent view = loader.load();

            NewReservationController controller =
                    loader.getController();

            controller.setDashboardController(this);

            controller.setReservationToEdit(
                    reservation
            );

            centerPane.getChildren().setAll(view);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}

