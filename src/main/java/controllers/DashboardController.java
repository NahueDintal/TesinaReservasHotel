package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import models.Reservation;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import javafx.scene.control.Button;
import auth.LoginGuard;
import auth.LoginManager;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;

public class DashboardController {

  @FXML
  private ImageView logoApp;
  @FXML
  private Region espaciadorTop;
  @FXML
  private VBox leftMenu;
  @FXML
  private Button btnMenu;
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
  private ToggleButton btnPersonal;
  @FXML
  private ToggleButton btnConfiguracion;
  @FXML
  private Label lblUsuario;
  @FXML
  private Button btnSession;

  private ToggleGroup menuGroup;

  @FXML
  public void initialize() {
    // 1. Crear un ToggleGroup para que solo un botón esté seleccionado a la vez
    leftMenu.getStyleClass().add("collapsed");
    leftMenu.setPrefWidth(70);
    leftMenu.setMinWidth(70);
    leftMenu.setMaxWidth(70);
    ocultarLogo(true);
    ajustarEspaciador(true);

    Platform.runLater(() -> {
      double anchoBoton = btnMenu.getWidth() > 0 ? btnMenu.getWidth() : 40;
      btnMenu.setTranslateX(-(70 - anchoBoton) / 2);
    });

    menuGroup = new ToggleGroup();
    btnPlanilla.setToggleGroup(menuGroup);
    loadView("/views/BookingChart.fxml");
    btnReservas.setToggleGroup(menuGroup);
    btnHabitaciones.setToggleGroup(menuGroup);
    btnClientes.setToggleGroup(menuGroup);
    btnReportes.setToggleGroup(menuGroup);
    btnPersonal.setToggleGroup(menuGroup);
    btnConfiguracion.setToggleGroup(menuGroup);

    // 2. Asignar tooltips
    btnPlanilla.setTooltip(new Tooltip("Planilla"));
    btnReservas.setTooltip(new Tooltip("Reservas"));
    btnHabitaciones.setTooltip(new Tooltip("Habitaciones"));
    btnClientes.setTooltip(new Tooltip("Clientes"));
    btnReportes.setTooltip(new Tooltip("Reportes"));
    btnPersonal.setTooltip(new Tooltip("Personal"));
    btnConfiguracion.setTooltip(new Tooltip("Configuracion"));

    // 3. Seleccionar "Planilla" por defecto
    btnPlanilla.setSelected(true);

    // 4. Asignar acciones a los botones
    btnPlanilla.setOnAction(e -> {
      selectButton(btnPlanilla);
      loadView("/views/BookingChart.fxml");
    });
    btnReservas.setOnAction(e -> {
      // if (!ensureAuthenticated())
      // return;
      selectButton(btnReservas);
      loadView("/views/reservations.fxml");
    });
    btnHabitaciones.setOnAction(e -> {
      // if (!ensureAuthenticated())
      // return;
      selectButton(btnHabitaciones);
      loadView("/views/RoomView.fxml");
    });
    btnClientes.setOnAction(e -> {
      // if (!ensureAuthenticated())
      // return;
      selectButton(btnClientes);
      loadView("/views/CustomerView.fxml");
    });
    btnReportes.setOnAction(e -> {
      // if (!ensureAuthenticated())
      // return;
      selectButton(btnReportes);
      loadView("/views/Report.fxml");
    });
    btnPersonal.setOnAction(e -> {
      // if (!ensureAuthenticated())
      // return;
      selectButton(btnPersonal);
      loadView("/views/StaffView.fxml");
    });
    btnConfiguracion.setOnAction(e -> {
      // if (!ensureAuthenticated())
      // return;
      selectButton(btnConfiguracion);
      loadView("/views/Configuration.fxml");
    });

    // 5. Centrar btn hamburguesa al iniciar
    btnMenu.setTranslateX(-10);

    LoginManager.getInstance().refreshConfig(); // por si cambiaron el toggle
    updateSessionUI();
  }

  @FXML
  private void toggleSidebar() {
    boolean collapsed = leftMenu.getStyleClass().contains("collapsed");

    if (collapsed) {
      // Expandir
      leftMenu.getStyleClass().remove("collapsed");
      leftMenu.setPrefWidth(240);
      leftMenu.setMinWidth(240);
      leftMenu.setMaxWidth(240);
      ajustarEspaciador(!collapsed);
      ocultarLogo(false);
      btnMenu.setTranslateX(-10);
    } else {
      // Colapsar
      leftMenu.getStyleClass().add("collapsed");
      leftMenu.setPrefWidth(70);
      leftMenu.setMinWidth(70);
      leftMenu.setMaxWidth(70);
      ocultarLogo(true);
      ajustarEspaciador(!collapsed);
      Platform.runLater(() -> {
        double anchoBoton = btnMenu.getWidth() > 0 ? btnMenu.getWidth() : 40;
        btnMenu.setTranslateX(-(70 - anchoBoton) / 2);
      });
    }
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

      Object controller = loader.getController();

      if (controller instanceof BookingChartController) {
        ((BookingChartController) controller)
            .setDashboardController(this);
      }

      if (controller instanceof ReservationsController) {
        ((ReservationsController) controller).setDashboardController(this);
      }
      if (controller instanceof NewReservationController) {
        ((NewReservationController) controller).setDashboardController(this);
      }
      if (controller instanceof CustomerController) { // ← NUEVO
        ((CustomerController) controller).setDashboardController(this);
      }

      centerPane.getChildren().clear();
      centerPane.getChildren().add(view);

      AnchorPane.setTopAnchor(view, 0.0);
      AnchorPane.setBottomAnchor(view, 0.0);
      AnchorPane.setLeftAnchor(view, 0.0);
      AnchorPane.setRightAnchor(view, 0.0);

    } catch (IOException e) {
      e.printStackTrace();
      centerPane.getChildren().setAll(new AnchorPane());
    }
  }

  public void loadEditReservation(Reservation reservation) {
    selectSidebarButton("reservas");
    try {

      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/views/NewReservation.fxml"));

      Parent view = loader.load();

      NewReservationController controller = loader.getController();

      controller.setDashboardController(this);
      controller.setReservationToEdit(reservation);

      // Colocar la vista exactamente igual que las demás
      centerPane.getChildren().setAll(view);

      AnchorPane.setTopAnchor(view, 0.0);
      AnchorPane.setBottomAnchor(view, 0.0);
      AnchorPane.setLeftAnchor(view, 0.0);
      AnchorPane.setRightAnchor(view, 0.0);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadNewReservation() {
    selectSidebarButton("reservas");

    try {

      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/views/NewReservation.fxml"));

      Parent view = loader.load();

      NewReservationController controller = loader.getController();

      controller.setDashboardController(this);

      centerPane.getChildren().setAll(view);

      AnchorPane.setTopAnchor(view, 0.0);
      AnchorPane.setBottomAnchor(view, 0.0);
      AnchorPane.setLeftAnchor(view, 0.0);
      AnchorPane.setRightAnchor(view, 0.0);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadReservationConsumptions(Reservation reservation) {
    selectSidebarButton("reservas");
    try {

      FXMLLoader loader = new FXMLLoader(
          getClass().getResource(
              "/views/ConsumptionManagement.fxml"));

      Parent view = loader.load();

      ConsumptionManagementController controller = loader.getController();

      controller.setDashboardController(this);
      controller.setReservation(reservation);

      centerPane.getChildren().setAll(view);

      AnchorPane.setTopAnchor(view, 0.0);
      AnchorPane.setBottomAnchor(view, 0.0);
      AnchorPane.setLeftAnchor(view, 0.0);
      AnchorPane.setRightAnchor(view, 0.0);

    } catch (IOException e) {

      e.printStackTrace();
    }
  }

  public void loadReservationDetail(Reservation reservation) {
    selectSidebarButton("reservas");
    try {

      FXMLLoader loader = new FXMLLoader(
          getClass().getResource(
              "/views/ReservationDetail.fxml"));

      Parent view = loader.load();

      ReservationDetailController controller = loader.getController();

      controller.setDashboardController(this);
      controller.setReservation(reservation);

      centerPane.getChildren().setAll(view);

      AnchorPane.setTopAnchor(view, 0.0);
      AnchorPane.setBottomAnchor(view, 0.0);
      AnchorPane.setLeftAnchor(view, 0.0);
      AnchorPane.setRightAnchor(view, 0.0);

    } catch (IOException e) {

      e.printStackTrace();
    }
  }

  /**
   * Selecciona programáticamente un botón de la sidebar.
   * Útil cuando se navega a una vista desde otra (ej: historial → reservas).
   */
  public void selectSidebarButton(String buttonName) {
    switch (buttonName) {
      case "reservas":
        selectButton(btnReservas);
        break;
      case "habitaciones":
        selectButton(btnHabitaciones);
        break;
      case "clientes":
        selectButton(btnClientes);
        break;
      case "planilla":
        selectButton(btnPlanilla);
        break;
      case "reportes":
        selectButton(btnReportes);
        break;
      case "personal":
        selectButton(btnPersonal);
        break;
      case "configuracion":
        selectButton(btnConfiguracion);
        break;
      default:
        System.err.println("Botón desconocido: " + buttonName);
    }
  }

  private void ocultarLogo(boolean ocultar) {
    logoApp.setVisible(!ocultar);
    logoApp.setManaged(!ocultar); // ← clave: no participa del layout
  }

  private void ajustarEspaciador(boolean colapsada) {
    espaciadorTop.setMinHeight(colapsada ? 50 : 0);
    espaciadorTop.setPrefHeight(colapsada ? 50 : 0);
  }

  @FXML
  private void handleSessionButton() {
    LoginManager lm = LoginManager.getInstance();

    if (!lm.isLoginEnabled()) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Login desactivado");
      alert.setHeaderText(null);
      alert.setContentText(
          "El sistema de login está desactivado.\n" +
              "Para activarlo, un administrador debe hacerlo desde Configuración → Seguridad.");
      alert.showAndWait();
      return;
    }

    if (lm.isAuthenticated()) {
      lm.logout();

      selectButton(btnPlanilla);
      loadView("/views/BookingChart.fxml");
    } else {
      LoginGuard.requireAccess();
    }

    updateSessionUI();
  }

  private void updateSessionUI() {
    LoginManager lm = LoginManager.getInstance();

    if (!lm.isLoginEnabled()) {
      lblUsuario.setText("Login off");
      btnSession.setText("Login off");
      btnSession.setDisable(true);
      return;
    }

    btnSession.setDisable(false);

    if (lm.isAuthenticated()) {
      String nombre = lm.getCurrentUser().getFullName();
      String rol = lm.getCurrentUser().getRole();
      lblUsuario.setText("👤 " + nombre + " (" + rol + ")");
      btnSession.setText("Cerrar sesión");
    } else {
      lblUsuario.setText("Sin sesión");
      btnSession.setText("Iniciar sesión");
    }
  }

  private boolean ensureAuthenticated() {
    if (LoginGuard.requireAccess()) {
      updateSessionUI(); // refresca label/button si acaba de loguearse
      return true;
    }

    selectButton(btnPlanilla);
    return false;
  }
}
