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
import java.util.prefs.Preferences; // ⬅️ NUEVO

import auth.LoginGuard;
import auth.LoginManager;
import models.Reservation;

public class DashboardController {

  /**
   * ============================================================
   * FLAG MAESTRO DEL LOGIN
   * ------------------------------------------------------------
   * true -> el login vuelve a estar activo (botón "Cerrar sesión",
   * guard en navegación, label de usuario, etc.)
   * false -> login apagado (botón de sesión oculto, sin guard).
   *
   * Cambiá SOLO esta línea para prender/apagar todo el sistema.
   * ============================================================
   */
  private static final boolean LOGIN_ENABLED = false;

  // ========== TEMA (preferencia persistida) ⬅️ NUEVO ==========
  private static final String DARK_CLASS = "dark-theme";
  private static final Preferences PREFS = Preferences.userNodeForPackage(DashboardController.class);

  // ========== FXML FIELDS ==========
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
  private ToggleButton btnTheme; // ⬅️ NUEVO

  private ToggleGroup menuGroup;

  // ========== INIT ==========
  @FXML
  public void initialize() {

    // Sidebar colapsada al arrancar
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

    // ToggleGroup del menú
    menuGroup = new ToggleGroup();
    btnPlanilla.setToggleGroup(menuGroup);
    btnReservas.setToggleGroup(menuGroup);
    btnHabitaciones.setToggleGroup(menuGroup);
    btnClientes.setToggleGroup(menuGroup);
    btnReportes.setToggleGroup(menuGroup);
    btnPersonal.setToggleGroup(menuGroup);
    btnConfiguracion.setToggleGroup(menuGroup);

    // Vista por defecto
    loadView("/views/BookingChart.fxml");
    btnPlanilla.setSelected(true);

    // Tooltips
    btnPlanilla.setTooltip(new Tooltip("Planilla"));
    btnReservas.setTooltip(new Tooltip("Reservas"));
    btnHabitaciones.setTooltip(new Tooltip("Habitaciones"));
    btnClientes.setTooltip(new Tooltip("Clientes"));
    btnReportes.setTooltip(new Tooltip("Reportes"));
    btnPersonal.setTooltip(new Tooltip("Personal"));
    btnConfiguracion.setTooltip(new Tooltip("Configuracion"));

    // Acciones de navegación (con guard condicional)
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

    // Centrar hamburguesa al iniciar
    btnMenu.setTranslateX(-10);

    // ⬇️⬇️⬇️ NUEVO: inicializar el toggle de tema ⬇️⬇️⬇️
    initThemeToggle();

    // Estado inicial de la sesión (label + botón)
    if (LOGIN_ENABLED) {
      LoginManager.getInstance().refreshConfig();
    }
    updateSessionUI();
  }

  // ============================================================
  // TEMA CLARO / OSCURO ⬅️ NUEVO
  // ============================================================

  private void initThemeToggle() {
    if (btnTheme == null)
      return;

    boolean dark = utils.ThemeManager.isDark();
    btnTheme.setSelected(dark);

    // Invertimos el texto para que muestre el estado actual
    btnTheme.setText(dark ? "Modo oscuro" : "Modo claro");

    btnTheme.setTooltip(new Tooltip(dark
        ? "Cambiar a tema claro"
        : "Cambiar a tema oscuro"));

    javafx.application.Platform.runLater(() -> {
      if (btnTheme.getScene() != null) {
        utils.ThemeManager.applyTo(btnTheme.getScene().getRoot());
      }
    });
  }

  @FXML
  private void toggleTheme() {
    boolean dark = btnTheme.isSelected();
    utils.ThemeManager.setDark(dark);

    if (btnTheme.getScene() != null) {
      utils.ThemeManager.applyTo(btnTheme.getScene().getRoot());
    }

    btnTheme.setText(dark ? "Modo claro" : "Modo oscuro");
    btnTheme.setTooltip(new Tooltip(dark
        ? "Cambiar a tema claro"
        : "Cambiar a tema oscuro"));
  }

  // ========== SIDEBAR ==========
  @FXML
  private void toggleSidebar() {
    boolean collapsed = leftMenu.getStyleClass().contains("collapsed");

    if (collapsed) {
      // Expandir
      leftMenu.getStyleClass().remove("collapsed");
      leftMenu.setPrefWidth(240);
      leftMenu.setMinWidth(240);
      leftMenu.setMaxWidth(240);
      ajustarEspaciador(false);
      ocultarLogo(false);
      btnMenu.setTranslateX(-10);
    } else {
      // Colapsar
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

  // ========== SESIÓN (con flag) ==========

  @FXML
  private void handleSessionButton() {
    if (!LOGIN_ENABLED) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Login desactivado");
      alert.setHeaderText(null);
      alert.setContentText(
          "El sistema de login está desactivado.\n" +
              "Para activarlo, poné LOGIN_ENABLED = true en DashboardController.");
      alert.showAndWait();
      return;
    }

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
    if (!LOGIN_ENABLED) {
      lblUsuario.setText("");
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
    if (!LOGIN_ENABLED)
      return true;

    if (LoginGuard.requireAccess()) {
      updateSessionUI();
      return true;
    }

    selectButton(btnPlanilla);
    return false;
  }

  // ========== NAVEGACIÓN ==========

  private void selectButton(ToggleButton button) {
    button.setSelected(true);
  }

  public void loadView(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent view = loader.load();

      Object controller = loader.getController();

      if (controller instanceof BookingChartController) {
        ((BookingChartController) controller).setDashboardController(this);
      }
      if (controller instanceof ReservationsController) {
        ((ReservationsController) controller).setDashboardController(this);
      }
      if (controller instanceof NewReservationController) {
        ((NewReservationController) controller).setDashboardController(this);
      }
      if (controller instanceof CustomerController) {
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
          getClass().getResource("/views/ConsumptionManagement.fxml"));
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
          getClass().getResource("/views/ReservationDetail.fxml"));
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

  // ========== HELPERS DE UI ==========
  private void ocultarLogo(boolean ocultar) {
    logoApp.setVisible(!ocultar);
    logoApp.setManaged(!ocultar);
  }

  private void ajustarEspaciador(boolean colapsada) {
    espaciadorTop.setMinHeight(colapsada ? 50 : 15);
    espaciadorTop.setPrefHeight(colapsada ? 50 : 15);
  }
}
