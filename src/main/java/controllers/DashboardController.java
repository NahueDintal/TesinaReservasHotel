package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.prefs.Preferences;

import auth.LoginGuard;
import auth.LoginManager;
import models.Reservation;
import java.time.LocalDate;
import models.Room;
import models.Reservation;

public class DashboardController {

  private static final boolean LOGIN_ENABLED = true;
  private static final String DARK_CLASS = "dark-theme";
  private static final Preferences PREFS = Preferences.userNodeForPackage(DashboardController.class);

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
  @FXML
  private ToggleButton btnTheme;

  private ToggleGroup menuGroup;

  @FXML
  public void initialize() {
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
    btnReservas.setToggleGroup(menuGroup);
    btnHabitaciones.setToggleGroup(menuGroup);
    btnClientes.setToggleGroup(menuGroup);
    btnReportes.setToggleGroup(menuGroup);
    btnPersonal.setToggleGroup(menuGroup);
    btnConfiguracion.setToggleGroup(menuGroup);

    loadView("/views/BookingChart.fxml");
    btnPlanilla.setSelected(true);

    btnPlanilla.setTooltip(new Tooltip("Planilla"));
    btnReservas.setTooltip(new Tooltip("Reservas"));
    btnHabitaciones.setTooltip(new Tooltip("Habitaciones"));
    btnClientes.setTooltip(new Tooltip("Clientes"));
    btnReportes.setTooltip(new Tooltip("Reportes"));
    btnPersonal.setTooltip(new Tooltip("Personal"));
    btnConfiguracion.setTooltip(new Tooltip("Configuracion"));

    btnPlanilla.setOnAction(e -> {
      selectButton(btnPlanilla);
      loadView("/views/BookingChart.fxml");
    });
    btnReservas.setOnAction(e -> {
      if (LOGIN_ENABLED && !ensureAuthenticated())
        return;
      selectButton(btnReservas);
      loadView("/views/reservations.fxml");
    });
    btnHabitaciones.setOnAction(e -> {
      if (LOGIN_ENABLED && !ensureAuthenticated())
        return;
      selectButton(btnHabitaciones);
      loadView("/views/RoomView.fxml");
    });
    btnClientes.setOnAction(e -> {
      if (LOGIN_ENABLED && !ensureAuthenticated())
        return;
      selectButton(btnClientes);
      loadView("/views/CustomerView.fxml");
    });
    btnReportes.setOnAction(e -> {
      if (LOGIN_ENABLED && !ensureAuthenticated())
        return;
      selectButton(btnReportes);
      loadView("/views/Report.fxml");
    });
    btnPersonal.setOnAction(e -> {
      if (LOGIN_ENABLED && !ensureAuthenticated())
        return;
      selectButton(btnPersonal);
      loadView("/views/StaffView.fxml");
    });
    btnConfiguracion.setOnAction(e -> {
      if (LOGIN_ENABLED && !ensureAuthenticated())
        return;
      selectButton(btnConfiguracion);
      loadView("/views/Configuration.fxml");
    });

    btnMenu.setTranslateX(-10);
    initThemeToggle();

    if (LOGIN_ENABLED)
      LoginManager.getInstance().refreshConfig();
    updateSessionUI();
  }

  private void initThemeToggle() {
    if (btnTheme == null)
      return;
    boolean dark = utils.ThemeManager.isDark();
    btnTheme.setSelected(dark);
    btnTheme.setText(dark ? "Modo oscuro" : "Modo claro");
    Platform.runLater(() -> {
      if (btnTheme.getScene() != null)
        utils.ThemeManager.applyTo(btnTheme.getScene().getRoot());
    });
  }

  @FXML
  private void toggleTheme() {
    boolean dark = btnTheme.isSelected();
    utils.ThemeManager.setDark(dark);
    if (btnTheme.getScene() != null)
      utils.ThemeManager.applyTo(btnTheme.getScene().getRoot());
    btnTheme.setText(dark ? "Modo claro" : "Modo oscuro");
  }

  @FXML
  private void toggleSidebar() {
    boolean collapsed = leftMenu.getStyleClass().contains("collapsed");
    if (collapsed) {
      leftMenu.getStyleClass().remove("collapsed");
      leftMenu.setPrefWidth(240);
      leftMenu.setMinWidth(240);
      leftMenu.setMaxWidth(240);
      ajustarEspaciador(false);
      ocultarLogo(false);
      btnMenu.setTranslateX(-10);
    } else {
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
    }
  }

  @FXML
  private void handleSessionButton() {
    if (!LOGIN_ENABLED)
      return;
    LoginManager lm = LoginManager.getInstance();

    if (!lm.isLoginEnabled()) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION, "El sistema de login está desactivado.");
      alert.showAndWait();
      return;
    }

    if (lm.isAuthenticated()) {
      lm.logout();
      selectButton(btnPlanilla);
      loadView("/views/BookingChart.fxml");
    } else {
      // 👇 Vuelve a usar tu LoginGuard, que ya sabe abrir la ventana y esperar
      LoginGuard.requireAccess();
    }
    updateSessionUI();
  }

  private void updateSessionUI() {
    if (!LOGIN_ENABLED) {
      lblUsuario.setVisible(false);
      lblUsuario.setManaged(false);
      btnSession.setVisible(false);
      btnSession.setManaged(false);
      return;
    }

    lblUsuario.setVisible(true);
    lblUsuario.setManaged(true);
    btnSession.setVisible(true);
    btnSession.setManaged(true);

    LoginManager lm = LoginManager.getInstance();

    if (!lm.isLoginEnabled()) {
      lblUsuario.setText("Login off");
      btnSession.setText("Login off");
      btnSession.setDisable(true);
      return;
    }

    btnSession.setDisable(false);

    if (lm.isAuthenticated()) {
      lblUsuario.setText("👤 " + lm.getCurrentUser().getFullName() + " (" + lm.getCurrentUser().getRole() + ")");
      btnSession.setText("Cerrar sesión");
    } else {
      lblUsuario.setText("Sin sesión");
      btnSession.setText("Iniciar sesión");
    }
  }

  private boolean ensureAuthenticated() {
    if (!LOGIN_ENABLED)
      return true;
    // LoginGuard hace todo el trabajo pesado acá
    if (LoginGuard.requireAccess()) {
      updateSessionUI();
      return true;
    }
    selectButton(btnPlanilla);
    return false;
  }

  private void selectButton(ToggleButton button) {
    button.setSelected(true);
  }

  public void loadView(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent view = loader.load();
      Object controller = loader.getController();

      if (controller instanceof BookingChartController)
        ((BookingChartController) controller).setDashboardController(this);
      if (controller instanceof ReservationsController)
        ((ReservationsController) controller).setDashboardController(this);
      if (controller instanceof NewReservationController)
        ((NewReservationController) controller).setDashboardController(this);
      if (controller instanceof CustomerController)
        ((CustomerController) controller).setDashboardController(this);

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

  // =========================================================
  // NUEVA RESERVA (formulario vacío)
  // =========================================================
  public void loadNewReservation() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/NewReservation.fxml"));
      Parent view = loader.load();

      NewReservationController controller = loader.getController();
      controller.setDashboardController(this);

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

  // =========================================================
  // NUEVA RESERVA (con habitación y fechas preseleccionadas
  // desde la planilla)
  // =========================================================
  public void loadNewReservation(Room room, LocalDate checkIn, LocalDate checkOut) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/NewReservation.fxml"));
      Parent view = loader.load();

      NewReservationController controller = loader.getController();
      controller.setDashboardController(this);

      if (room != null) {
        controller.setSelectedRoom(room.getNumber());
      }
      if (checkIn != null && checkOut != null) {
        controller.setSelectedDates(checkIn, checkOut);
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

  // =========================================================
  // EDITAR RESERVA
  // =========================================================
  public void loadEditReservation(Reservation reservation) {
    if (reservation == null)
      return;

    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/NewReservation.fxml"));
      Parent view = loader.load();

      NewReservationController controller = loader.getController();
      controller.setDashboardController(this);
      controller.setReservationToEdit(reservation);

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

  // =========================================================
  // CONSUMOS DE UNA RESERVA
  // (reusa el formulario de reserva, ya cargado con sus
  // consumos; el usuario solo tiene que bajar a la sección
  // de consumos)
  // =========================================================
  public void loadReservationConsumptions(Reservation reservation) {
    if (reservation == null)
      return;

    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/NewReservation.fxml"));
      Parent view = loader.load();

      NewReservationController controller = loader.getController();
      controller.setDashboardController(this);
      controller.setReservationToEdit(reservation);

      centerPane.getChildren().clear();
      centerPane.getChildren().add(view);

      AnchorPane.setTopAnchor(view, 0.0);
      AnchorPane.setBottomAnchor(view, 0.0);
      AnchorPane.setLeftAnchor(view, 0.0);
      AnchorPane.setRightAnchor(view, 0.0);

      // Si en el FXML de NewReservation tenés una pestaña o
      // sección de consumos, podés forzarla acá:
      // controller.focusConsumptionsSection();

    } catch (IOException e) {
      e.printStackTrace();
      centerPane.getChildren().setAll(new AnchorPane());
    }
  }

  // =========================================================
  // DETALLE DE RESERVA (solo lectura)
  // =========================================================
  public void loadReservationDetail(Reservation reservation) {
    if (reservation == null)
      return;

    try {
      // Si tenés una vista /views/ReservationDetail.fxml, usala acá.
      // Si no, caemos al formulario en modo edición (que ya muestra
      // todos los datos de la reserva).
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/NewReservation.fxml"));
      Parent view = loader.load();

      NewReservationController controller = loader.getController();
      controller.setDashboardController(this);
      controller.setReservationToEdit(reservation);

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
    }
  }

  private void ocultarLogo(boolean ocultar) {
    logoApp.setVisible(!ocultar);
    logoApp.setManaged(!ocultar);
  }

  private void ajustarEspaciador(boolean colapsada) {
    espaciadorTop.setMinHeight(colapsada ? 50 : 15);
    espaciadorTop.setPrefHeight(colapsada ? 50 : 15);
  }
}
