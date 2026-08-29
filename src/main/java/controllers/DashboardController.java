package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
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

    private ToggleGroup menuGroup;

    @FXML
    public void initialize() {
        // 1. Crear un ToggleGroup para que solo un botón esté seleccionado a la vez
        menuGroup = new ToggleGroup();
        btnPlanilla.setToggleGroup(menuGroup);
        btnReservas.setToggleGroup(menuGroup);
        btnHabitaciones.setToggleGroup(menuGroup);
        btnClientes.setToggleGroup(menuGroup);
        btnReportes.setToggleGroup(menuGroup);
        btnConfiguracion.setToggleGroup(menuGroup);

        // 2. Seleccionar "Clientes" por defecto
        btnPlanilla.setSelected(true);

        // 3. Asignar acciones a los botones
        btnPlanilla.setOnAction(e -> {
            selectButton(btnPlanilla);
            loadView("/views/.fxml");
        });
        btnReservas.setOnAction(e -> {
            selectButton(btnReservas);
            loadView("/views/.fxml");
        });
        btnHabitaciones.setOnAction(e -> {
            selectButton(btnHabitaciones);
            loadView("/views/Room.fxml");
        });
        btnClientes.setOnAction(e -> {
            selectButton(btnClientes);
            loadView("/views/CustomerView.fxml");
        });
        btnReportes.setOnAction(e -> {
            selectButton(btnReportes);
            loadView("/views/.fxml");
        });
        btnConfiguracion.setOnAction(e -> {
            selectButton(btnConfiguracion);
            loadView("/views/.fxml");
        });
    }

    /**
     * Selecciona un botón y deselecciona los demás (a través del ToggleGroup)
     */
    private void selectButton(ToggleButton button) {
        button.setSelected(true);
    }

    /**
     * Carga un archivo FXML en el panel central.
     */
    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Si el controlador necesita referencia al Dashboard, se la pasamos
            Object controller = loader.getController();
            if (controller instanceof RoomController) {
                ((RoomController) controller).setDashboardController(this);
            } else if (controller instanceof RoomFormController) {
                ((RoomFormController) controller).setDashboardController(this);
            }

            // Limpiar el centerPane y agregar la vista
            centerPane.getChildren().clear();
            centerPane.getChildren().add(view);

            // === FORZAR QUE LA VISTA SE ESTIRE A TODOS LOS LADOS ===
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
            centerPane.getChildren().setAll(new AnchorPane());
        }
    }
}