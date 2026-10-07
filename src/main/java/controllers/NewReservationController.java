package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.*;
import repositories.*;
import services.HotelTourService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class NewReservationController {

  @FXML Label lblReservationTitle;
  @FXML private TextField txtCustomerSearch;
  @FXML private ListView<Customer> lstCustomers;
  @FXML private DatePicker dpCheckIn;
  @FXML private DatePicker dpCheckOut;
  @FXML private TextField txtNumberOfGuests;
  @FXML private TextField txtTotalRate;
  @FXML private ComboBox<ReservationStatus> cmbReservationStatus;
  @FXML private ComboBox<ReservationType> cmbReservationType;
  @FXML private TextArea txtReservationObservations;
  @FXML private FlowPane roomsContainer;
  @FXML private TextField txtPaymentAmount;
  @FXML private DatePicker dpPaymentDate;
  @FXML private ComboBox<PaymentMethod> cmbPaymentMethod;
  @FXML private ComboBox<PaymentStatus> cmbPaymentStatus;
  @FXML private TextArea txtPaymentObservations;
  @FXML private ComboBox<String> cmbConsumptionType;
  @FXML private ComboBox<Product> cmbProduct;
  @FXML private ComboBox<Service> cmbService;
  @FXML private TextField txtConsumptionQuantity;
  @FXML private TableView<Consumption> tblConsumptions;
  @FXML private TableColumn<Consumption,Integer> colConsumptionQuantity;
  @FXML private TableColumn<Consumption,BigDecimal> colConsumptionUnitPrice;
  @FXML private TableColumn<Consumption,BigDecimal> colConsumptionTotal;
  @FXML private TableColumn<Consumption,String> colConsumptionType;
  @FXML private TableColumn<Consumption,String> colConsumptionName;
  @FXML private TableColumn<Consumption,Void> colConsumptionActions;
  @FXML private TextField txtConsumptionTotal;
  @FXML private Button btnConsumptionAction;
  @FXML private Button btnChangeRoom;

  @FXML private Button btnHotelTourConstruction; // <-- Agrega esta línea

  private final ObservableList<Customer> activeCustomers = FXCollections.observableArrayList();
  private Customer selectedCustomer;
  private final ObservableList<Room> activeRooms = FXCollections.observableArrayList();
  private final ObservableList<Room> selectedRooms = FXCollections.observableArrayList();
  private final ObservableList<Room> availableRooms = FXCollections.observableArrayList();
  private DashboardController dashboardController;
  private Reservation reservationToEdit;
  private boolean openedFromBookingChart = false;
  private Integer selectedRoomFromChart;

  private final RoomDAO roomDAO;
  private final ReservationRepo reservationRepo;
  private final ReservationStatusRepo reservationStatusRepo;
  private final ReservationTypeRepo reservationTypeRepo;
  private final ReservationRoomRepo reservationRoomRepo;
  private final PaymentRepo paymentRepo;
  private final PaymentMethodRepo paymentMethodRepo;
  private final PaymentStatusRepo paymentStatusRepo;
  private final ConsumptionRepo consumptionRepo;
  private final ProductRepo productRepo;
  private final ServiceRepo serviceRepo;
  private final CustomerDAO customerDAO;

  private final ObservableList<Consumption> consumptions = FXCollections.observableArrayList();
  private final List<Consumption> modifiedConsumptions = new ArrayList<>();
  private final List<Consumption> canceledConsumptions = new ArrayList<>();
  private Consumption consumptionBeingEdited = null;
  private final HotelTourService hotelTourService = new HotelTourService();
  private List<HotelTour> availableTours = new ArrayList<>();
  private boolean loadingReservation = false;

  private boolean allowRoomChange = false;

  private final Set<Control> touchedFields = new HashSet<>();
  private boolean roomsTouched = false;
  private Tooltip roomsValidationTooltip;

  public NewReservationController() {
    roomDAO = new RoomDAO();
    reservationRepo = new ReservationRepo();
    reservationStatusRepo = new ReservationStatusRepo();
    reservationTypeRepo = new ReservationTypeRepo();
    reservationRoomRepo = new ReservationRoomRepo();
    paymentRepo = new PaymentRepo();
    paymentMethodRepo = new PaymentMethodRepo();
    paymentStatusRepo = new PaymentStatusRepo();
    consumptionRepo = new ConsumptionRepo();
    productRepo = new ProductRepo();
    serviceRepo = new ServiceRepo();
    customerDAO = new CustomerDAO();
  }

  @FXML
  public void initialize() {
    System.out.println("=== NewReservationController.initialize() ===");

    if (btnChangeRoom != null) {
      btnChangeRoom.setVisible(false);
      btnChangeRoom.setManaged(false);
    }

    activeRooms.setAll(roomDAO.listActive());
    loadRoomCards();

    configureConsumptionTable();
    updateConsumptionTableHeight();

    cmbConsumptionType.setItems(FXCollections.observableArrayList("Producto", "Servicio"));
    cmbConsumptionType.setOnAction(event -> {
      String type = cmbConsumptionType.getValue();
      if (type == null) {
        cmbProduct.setDisable(true);
        cmbService.setDisable(true);
        return;
      }
      if (type.equals("Producto")) {
        cmbProduct.setDisable(false);
        cmbService.setDisable(true);
        cmbService.setValue(null);
      } else {
        cmbProduct.setDisable(true);
        cmbService.setDisable(false);
        cmbProduct.setValue(null);
      }
      if (touchedFields.contains(cmbConsumptionType)) clearInvalid(cmbConsumptionType);
    });

    loadReservationStatuses();
    loadReservationTypes();
    loadPaymentMethods();
    loadPaymentStatuses();
    loadProducts();
    loadServices();
    loadCustomers();
    configureCustomerSearch();

    txtConsumptionTotal.setText("0.00");
    txtConsumptionTotal.setEditable(false);
    cmbProduct.setDisable(true);
    cmbService.setDisable(true);

    txtNumberOfGuests.textProperty().addListener((obs, oldVal, newVal) -> {
      loadAvailableRooms();
      updateTotalRate();
      if (touchedFields.contains(txtNumberOfGuests)) validateGuestsField();
    });

    dpCheckIn.valueProperty().addListener((obs, oldVal, newVal) -> {
      loadAvailableRooms();
      updateTotalRate();
      if (touchedFields.contains(dpCheckIn) || touchedFields.contains(dpCheckOut)) validateDates();
    });

    dpCheckOut.valueProperty().addListener((obs, oldVal, newVal) -> {
      loadAvailableRooms();
      updateTotalRate();
      if (touchedFields.contains(dpCheckIn) || touchedFields.contains(dpCheckOut)) validateDates();
    });

    configureValidationListeners();

    installDecimalFilter(txtTotalRate);
    installDecimalFilter(txtPaymentAmount);
  }

  private void configureValidationListeners() {
    addFocusValidation(txtCustomerSearch, () -> validateCustomerField());
    addFocusValidation(dpCheckIn, () -> validateDates());
    addFocusValidation(dpCheckOut, () -> validateDates());
    addFocusValidation(txtNumberOfGuests, () -> validateGuestsField());
    addFocusValidation(txtTotalRate, () -> validateRateField());
    addFocusValidation(cmbReservationStatus, () -> validateReservationStatusField());
    addFocusValidation(cmbReservationType, () -> validateReservationTypeField());
    addFocusValidation(txtPaymentAmount, () -> validatePaymentFields());
    addFocusValidation(dpPaymentDate, () -> validatePaymentFields());
    addFocusValidation(cmbPaymentMethod, () -> validatePaymentFields());
    addFocusValidation(cmbPaymentStatus, () -> validatePaymentFields());

    txtCustomerSearch.textProperty().addListener((obs, oldVal, newVal) -> {
      if (selectedCustomer != null) {
        String selected = selectedCustomer.getName() + " " + selectedCustomer.getSurname();
        if (!selected.equals(newVal)) selectedCustomer = null;
      }
      if (touchedFields.contains(txtCustomerSearch)) validateCustomerField();
    });

    txtTotalRate.textProperty().addListener((obs, oldVal, newVal) -> {
      if (touchedFields.contains(txtTotalRate)) validateRateField();
    });

    txtPaymentAmount.textProperty().addListener((obs, oldVal, newVal) -> {
      if (touchedFields.contains(txtPaymentAmount) || touchedFields.contains(dpPaymentDate)
              || touchedFields.contains(cmbPaymentMethod) || touchedFields.contains(cmbPaymentStatus))
        validatePaymentFields();
    });

    cmbReservationStatus.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (touchedFields.contains(cmbReservationStatus)) validateReservationStatusField();
    });

    cmbReservationType.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (touchedFields.contains(cmbReservationType)) validateReservationTypeField();
    });

    cmbPaymentMethod.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (touchedFields.contains(cmbPaymentMethod)) validatePaymentFields();
    });

    cmbPaymentStatus.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (touchedFields.contains(cmbPaymentStatus)) validatePaymentFields();
    });
  }

  private void addFocusValidation(Control field, Runnable validation) {
    field.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (oldVal && !newVal) {
        touchedFields.add(field);
        validation.run();
      }
    });
  }

  private void validateCustomerField() {
    if (selectedCustomer == null) markInvalid(txtCustomerSearch, "Debe seleccionar un cliente.");
    else clearInvalid(txtCustomerSearch);
  }

  private void validateDates() {
    LocalDate in = dpCheckIn.getValue(), out = dpCheckOut.getValue();

    if (in == null) markInvalid(dpCheckIn, "Seleccione una fecha de check-in.");
    else clearInvalid(dpCheckIn);

    if (out == null) markInvalid(dpCheckOut, "Seleccione una fecha de check-out.");
    else clearInvalid(dpCheckOut);

    if (in != null && out != null) {
      if (!out.isAfter(in)) {
        markInvalid(dpCheckIn, "El check-in debe ser anterior al check-out.");
        markInvalid(dpCheckOut, "El check-out debe ser posterior al check-in.");
      } else {
        clearInvalid(dpCheckIn);
        clearInvalid(dpCheckOut);
      }
    }
  }

  private void validateGuestsField() {
    String text = txtNumberOfGuests.getText().trim();
    if (text.isEmpty()) {
      markInvalid(txtNumberOfGuests, "Ingrese la cantidad de huéspedes.");
      return;
    }
    try {
      int guests = Integer.parseInt(text);
      if (guests <= 0) markInvalid(txtNumberOfGuests, "La cantidad debe ser mayor a 0.");
      else clearInvalid(txtNumberOfGuests);
    } catch (NumberFormatException e) {
      markInvalid(txtNumberOfGuests, "Ingrese un número entero válido.");
    }
  }

  private void validateRateField() {
    String text = txtTotalRate.getText().trim();
    if (text.isEmpty()) {
      markInvalid(txtTotalRate, "Ingrese la tarifa total.");
      return;
    }
    try {
      BigDecimal rate = new BigDecimal(normalizeDecimal(text));
      if (rate.compareTo(BigDecimal.ZERO) <= 0)
        markInvalid(txtTotalRate, "La tarifa debe ser mayor a 0.");
      else if (rate.compareTo(new BigDecimal("999999999.99")) > 0)
        markInvalid(txtTotalRate, "La tarifa supera el máximo permitido.");
      else clearInvalid(txtTotalRate);
    } catch (NumberFormatException e) {
      markInvalid(txtTotalRate, "Ingrese un importe válido.");
    }
  }

  private void validateReservationStatusField() {
    if (cmbReservationStatus.getValue() == null)
      markInvalid(cmbReservationStatus, "Seleccione un estado.");
    else clearInvalid(cmbReservationStatus);
  }

  private void validateReservationTypeField() {
    if (cmbReservationType.getValue() == null)
      markInvalid(cmbReservationType, "Seleccione un tipo de reserva.");
    else clearInvalid(cmbReservationType);
  }

  private void validatePaymentFields() {
    String text = txtPaymentAmount.getText().trim();

    if (text.isEmpty()) {
      clearInvalid(txtPaymentAmount);
      clearInvalid(dpPaymentDate);
      clearInvalid(cmbPaymentMethod);
      clearInvalid(cmbPaymentStatus);
      return;
    }

    try {
      BigDecimal amount = new BigDecimal(normalizeDecimal(text));
      if (amount.compareTo(BigDecimal.ZERO) <= 0)
        markInvalid(txtPaymentAmount, "El pago debe ser mayor a 0.");
      else clearInvalid(txtPaymentAmount);
    } catch (NumberFormatException e) {
      markInvalid(txtPaymentAmount, "Ingrese un importe válido.");
    }

    if (dpPaymentDate.getValue() == null)
      markInvalid(dpPaymentDate, "Seleccione la fecha del pago.");
    else clearInvalid(dpPaymentDate);

    if (cmbPaymentMethod.getValue() == null)
      markInvalid(cmbPaymentMethod, "Seleccione un método de pago.");
    else clearInvalid(cmbPaymentMethod);

    if (cmbPaymentStatus.getValue() == null)
      markInvalid(cmbPaymentStatus, "Seleccione un estado del pago.");
    else clearInvalid(cmbPaymentStatus);
  }

  private boolean validateAllFields() {
    touchedFields.add(txtCustomerSearch);
    touchedFields.add(dpCheckIn);
    touchedFields.add(dpCheckOut);
    touchedFields.add(txtNumberOfGuests);
    touchedFields.add(txtTotalRate);
    touchedFields.add(cmbReservationStatus);
    touchedFields.add(cmbReservationType);
    touchedFields.add(txtPaymentAmount);
    touchedFields.add(dpPaymentDate);
    touchedFields.add(cmbPaymentMethod);
    touchedFields.add(cmbPaymentStatus);
    roomsTouched = true;

    validateCustomerField();
    validateDates();
    validateGuestsField();
    validateRateField();
    validateReservationStatusField();
    validateReservationTypeField();
    validatePaymentFields();

    boolean valid = selectedCustomer != null;

    LocalDate in = dpCheckIn.getValue(), out = dpCheckOut.getValue();
    if (in == null || out == null || !out.isAfter(in)) valid = false;

    try {
      int guests = Integer.parseInt(txtNumberOfGuests.getText().trim());
      if (guests <= 0) valid = false;
    } catch (Exception e) { valid = false; }

    try {
      BigDecimal rate = new BigDecimal(normalizeDecimal(txtTotalRate.getText()));
      if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(new BigDecimal("999999999.99")) > 0)
        valid = false;
    } catch (Exception e) { valid = false; }

    if (cmbReservationStatus.getValue() == null || cmbReservationType.getValue() == null) valid = false;

    String payment = txtPaymentAmount.getText().trim();
    if (!payment.isEmpty()) {
      try {
        if (new BigDecimal(normalizeDecimal(payment)).compareTo(BigDecimal.ZERO) <= 0) valid = false;
      } catch (Exception e) { valid = false; }
      if (dpPaymentDate.getValue() == null || cmbPaymentMethod.getValue() == null
              || cmbPaymentStatus.getValue() == null) valid = false;
    }

    if (selectedRooms.isEmpty()) {
      markRoomsInvalid("Debe seleccionar al menos una habitación.");
      valid = false;
    } else clearRoomsInvalid();

    return valid;
  }

  public void setDashboardController(DashboardController dashboardController) {
    this.dashboardController = dashboardController;
  }

  public void setReservationToEdit(Reservation reservation) {
    this.reservationToEdit = reservation;
    this.loadingReservation = true;
    lblReservationTitle.setText("Editar Reserva");

    allowRoomChange = false;
    if (btnChangeRoom != null) {
      btnChangeRoom.setText("⚠️ Cambiar habitación");
      btnChangeRoom.setVisible(true);
      btnChangeRoom.setManaged(true);
    }

    if (reservation == null) {
      loadingReservation = false;
      return;
    }

    try {
      Customer customer = customerDAO.searchById(reservation.getIdCustomer());
      if (customer != null) {
        selectedCustomer = customer;
        txtCustomerSearch.setText(customer.getName() + " " + customer.getSurname());
      }
    } catch (Exception e) {
      System.err.println("Error cargando cliente: " + e.getMessage());
    }

    dpCheckIn.setValue(reservation.getCheckIn());
    dpCheckOut.setValue(reservation.getCheckOut());
    txtNumberOfGuests.setText(String.valueOf(reservation.getNumberOfGuests()));
    txtTotalRate.setText(reservation.getTotalRate() != null
            ? reservation.getTotalRate().toString().replace('.', ',')
            : "");

    cmbReservationStatus.getItems().stream()
            .filter(s -> s.getIdReservationStatus() == reservation.getIdReservationStatus())
            .findFirst().ifPresent(cmbReservationStatus::setValue);

    cmbReservationType.getItems().stream()
            .filter(t -> t.getIdReservationType() == reservation.getIdReservationType())
            .findFirst().ifPresent(cmbReservationType::setValue);

    activeRooms.setAll(roomDAO.listActive());

    List<ReservationRoom> reservationRooms =
            reservationRoomRepo.getByReservation(reservationToEdit.getIdReservation());
    selectedRooms.clear();

    for (ReservationRoom rr : reservationRooms) {
      for (Room room : activeRooms) {
        if (room.getIdRoom() == rr.getIdRoom()) {
          selectedRooms.add(room);
          break;
        }
      }
    }

    loadAvailableRooms();

    txtReservationObservations.setText(
            reservation.getObservations() == null ? "" : reservation.getObservations()
    );

    loadReservationPayment(reservation.getIdReservation());
    loadReservationConsumptions(reservation.getIdReservation());
    loadingReservation = false;

    // ============================================================
    BigDecimal storedRate = reservation.getTotalRate();
    boolean isRateValid = storedRate != null
            && storedRate.compareTo(BigDecimal.ZERO) > 0;

    if (!isRateValid && selectedRooms != null && !selectedRooms.isEmpty()) {
      updateTotalRate();
    }

  }

  private void loadReservationPayment(int idReservation) {
    try {
      List<Payment> payments = paymentRepo.getPaymentsByReservation(idReservation);
      if (payments.isEmpty()) {
        txtPaymentAmount.clear();
        dpPaymentDate.setValue(null);
        cmbPaymentMethod.setValue(null);
        cmbPaymentStatus.setValue(null);
        txtPaymentObservations.clear();
        return;
      }

      Payment payment = payments.get(0);
      txtPaymentAmount.setText(payment.getAmount() != null ? payment.getAmount().toString() : "");

      if (payment.getPaymentDate() != null)
        dpPaymentDate.setValue(payment.getPaymentDate().toLocalDate());

      cmbPaymentMethod.getItems().stream()
              .filter(m -> m.getIdPaymentMethod() == payment.getIdPaymentMethod())
              .findFirst().ifPresent(cmbPaymentMethod::setValue);

      cmbPaymentStatus.getItems().stream()
              .filter(s -> s.getIdPaymentStatus() == payment.getIdPaymentStatus())
              .findFirst().ifPresent(cmbPaymentStatus::setValue);

      txtPaymentObservations.setText(
              payment.getObservations() == null ? "" : payment.getObservations()
      );
    } catch (Exception e) {
      System.err.println("Error cargando pago: " + e.getMessage());
    }
  }

  private void loadReservationConsumptions(int idReservation) {
    try {
      List<Consumption> consumptionsBD = consumptionRepo.getConsumptionsByReservation(idReservation);
      consumptions.clear();
      modifiedConsumptions.clear();
      canceledConsumptions.clear();
      consumptions.addAll(consumptionsBD);
      updateConsumptionTableHeight();
      tblConsumptions.refresh();
      updateConsumptionTotal();
    } catch (Exception e) {
      System.err.println("Error cargando consumos: " + e.getMessage());
    }
  }

  private void loadRoomCards() {
    roomsContainer.getChildren().clear();
    int requested = getRequestedGuests();

    // ============================================================
    // NUEVO: Mostrar el botón de "Hotel Tour (En Construcción)"
    // solo cuando se detecte que se necesita un tour.
    // ============================================================
    boolean needsHotelTour = !availableTours.isEmpty();
    if (btnHotelTourConstruction != null) {
      btnHotelTourConstruction.setVisible(needsHotelTour);
      btnHotelTourConstruction.setManaged(needsHotelTour);
    }
    // ============================================================

    if (requested <= 0 && !openedFromBookingChart) {
      Label placeholder = new Label(
              "Ingresá la cantidad de huéspedes para ver las habitaciones disponibles.");
      placeholder.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-font-size: 13;");
      roomsContainer.getChildren().add(placeholder);
      return;
    }

    /* BOOKING CHART MODE */
    if (openedFromBookingChart) {
      Room selectedRoom = null;
      for (Room room : availableRooms) {
        if (room.getNumber() == selectedRoomFromChart) {
          selectedRoom = room;
          break;
        }
      }

      if (selectedRoom != null) {
        Label selectedTitle = new Label("Habitación seleccionada");
        selectedTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E6D3D; -fx-padding: 5 0 5 0;");
        roomsContainer.getChildren().add(selectedTitle);

        boolean capacityOk = hasEnoughCapacity(selectedRoom);
        VBox selectedCard = createRoomCard(selectedRoom, true, !capacityOk);
        roomsContainer.getChildren().add(selectedCard);

        if (!capacityOk && requested > 0) {
          Label warning = new Label(
                  "⚠ La habitación " + selectedRoom.getNumber()
                          + " tiene capacidad para " + selectedRoom.getCapacity()
                          + " huésped(es), pero ingresaste " + requested + ".");
          warning.setStyle("-fx-text-fill: #b36b00; -fx-font-weight: bold; -fx-padding: 5 0 10 0;");
          roomsContainer.getChildren().add(warning);
        }
      }

      List<Room> alternativeRooms = new ArrayList<>();
      for (Room room : availableRooms) {
        if (room.getNumber() == selectedRoomFromChart) continue;
        if (hasEnoughCapacity(room)) alternativeRooms.add(room);
      }

      if (!alternativeRooms.isEmpty()) {
        Label title = new Label("Otras habitaciones disponibles para estas fechas");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-padding: 10 0 5 0;");
        roomsContainer.getChildren().add(title);

        for (Room room : alternativeRooms) {
          roomsContainer.getChildren().add(createRoomCard(room, false, false));
        }
      } else if (selectedRoom != null && requested > 0 && !hasEnoughCapacity(selectedRoom)) {
        Label no = new Label(
                "No hay otras habitaciones disponibles para " + requested + " huésped(es) en estas fechas.");
        no.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-padding: 5 0 5 0;");
        roomsContainer.getChildren().add(no);
      }
      return;
    }

    /* NORMAL RESERVATION MODE */

    // 1) Cards de habitaciones disponibles
    if (!availableRooms.isEmpty()) {
      Set<Integer> selectedIds = selectedRooms.stream()
              .map(Room::getIdRoom)
              .collect(java.util.stream.Collectors.toSet());

      for (Room room : availableRooms) {
        boolean selected = selectedIds.contains(room.getIdRoom());
        roomsContainer.getChildren().add(createRoomCard(room, selected, false));
      }
    }

    // ============================================================
    // 2) Tours de hotel — AHORA SOLO MUESTRA UN AVISO
    //    (Ya NO se crean las tarjetas azules complejas del tour)
    // ============================================================
    if (!availableTours.isEmpty()) {
      Label title = new Label("🏨 Se requiere cambiar de habitación durante la estadía:");
      title.setStyle("-fx-text-fill: #8e44ad; -fx-font-weight: bold; -fx-padding: 10 0 4 0; -fx-font-size: 13px;");

      Label subtitle = new Label(
              "El sistema detectó que necesita un Hotel Tour. Esta funcionalidad estará disponible próximamente.");
      subtitle.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic; -fx-font-size: 12px;");
      subtitle.setWrapText(true);

      roomsContainer.getChildren().addAll(title, subtitle);
    }

    // 3) Mensaje cuando no hay nada para mostrar
    if (availableRooms.isEmpty() && availableTours.isEmpty()) {
      String msg;

      if (reservationToEdit != null && !allowRoomChange) {
        msg = "La habitación asignada a esta reserva ya no está disponible. "
                + "Tocá \"🔄 Cambiar habitación\" para elegir otra.";
      } else if (reservationToEdit != null) {
        msg = "No hay habitaciones libres para las fechas actuales. "
                + "Probá cambiar las fechas o tocar \"✖ Cancelar cambio\".";
      } else {
        msg = "No hay habitaciones ni tours disponibles para " + requested + " huésped(es).";
      }

      Label placeholder = new Label(msg);
      placeholder.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-font-size: 13;");
      placeholder.setWrapText(true);
      roomsContainer.getChildren().add(placeholder);
    }
  }

  private VBox createRoomCard(Room room, boolean selected, boolean capacityWarning) {
    VBox card = new VBox(5);

    Label lblNumber = new Label("Habitación " + room.getNumber());
    lblNumber.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #245c32;");

    Label lblType = new Label(room.getTypeName());
    lblType.setStyle("-fx-font-size: 12px; -fx-text-fill: #3e864d;");

    Label lblView = new Label(room.getViewName());
    lblView.setStyle("-fx-font-size: 12px; -fx-text-fill: #3e864d;");

    Label lblCapacity = new Label("Capacidad: " + room.getCapacity());
    lblCapacity.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

    Label lblPrice = new Label(String.format("$ %.2f / noche", room.getPrice()));
    lblPrice.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2e6d3d; -fx-padding: 3 0 0 0;");

    card.getChildren().addAll(lblNumber, lblType, lblView, lblCapacity, lblPrice);
    card.getStyleClass().add("room-card");

    if (selected) card.getStyleClass().add("selected");
    if (capacityWarning) card.getStyleClass().add("capacity-warning");

    card.setOnMouseClicked(event -> {
      if (openedFromBookingChart) {
        selectedRooms.clear();
        selectedRooms.add(room);
        selectedRoomFromChart = room.getNumber();
        clearRoomsInvalid();
        loadRoomCards();
        updateTotalRate();
        return;
      }

      boolean isSelected = selectedRooms.stream()
              .anyMatch(r -> r.getIdRoom() == room.getIdRoom());      // ✅

      if (isSelected) {
        selectedRooms.removeIf(r -> r.getIdRoom() == room.getIdRoom());   // ✅
        card.getStyleClass().remove("selected");
      } else {
        selectedRooms.add(room);
        card.getStyleClass().add("selected");
      }

      if (!selectedRooms.isEmpty()) clearRoomsInvalid();

      updateTotalRate();
    });

    return card;
  }

  private void loadAvailableRooms() {
    int requested = getRequestedGuests();
    availableRooms.clear();
    availableTours = new ArrayList<>();

    if (requested <= 0) {
      loadRoomCards();
      return;
    }

    LocalDate checkIn = dpCheckIn.getValue(), checkOut = dpCheckOut.getValue();

    if (checkIn == null || checkOut == null) {
      for (Room room : activeRooms)
        if (hasEnoughCapacity(room)) availableRooms.add(room);
      loadRoomCards();
      return;
    }

    if (!checkOut.isAfter(checkIn)) {
      availableRooms.clear();
      availableTours.clear();
      loadRoomCards();
      return;
    }

    Integer idReservationToExclude =
            reservationToEdit != null ? reservationToEdit.getIdReservation() : null;

    List<Integer> occupiedRooms =
            reservationRoomRepo.getOccupiedRoomIds(checkIn, checkOut, idReservationToExclude);

    if (openedFromBookingChart && selectedRoomFromChart != null) {
      for (Room room : activeRooms)
        if (!occupiedRooms.contains(room.getIdRoom())) availableRooms.add(room);
      loadRoomCards();
      return;
    }

    boolean isEditMode = reservationToEdit != null && !allowRoomChange;

    for (Room room : activeRooms) {
      boolean isFree = !occupiedRooms.contains(room.getIdRoom());
      boolean alreadyPicked = selectedRooms.stream()
              .anyMatch(r -> r.getIdRoom() == room.getIdRoom());
      boolean hasCapacity = hasEnoughCapacity(room);

      if (isEditMode) {
        if (alreadyPicked) availableRooms.add(room);
      } else {
        if ((isFree && hasCapacity) || alreadyPicked) availableRooms.add(room);
      }
    }

    HotelTourService.HotelTourResult result =
            hotelTourService.findOptions(checkIn, checkOut, requested, activeRooms);
    availableTours = result.getTours();
    loadRoomCards();
  }

  private boolean hasEnoughCapacity(Room room) {
    int requested = getRequestedGuests();
    return requested <= 0 || room.getCapacity() >= requested;
  }

  private String normalizeDecimal(String text) {
    if (text == null) return null;
    return text.trim()
            .replace(" ", "")
            .replace(',', '.');
  }

  private int getRequestedGuests() {
    if (txtNumberOfGuests == null) return 0;
    String text = txtNumberOfGuests.getText();
    if (text == null || text.trim().isEmpty()) return 0;
    try {
      int n = Integer.parseInt(text.trim());
      return n > 0 ? n : 0;
    } catch (NumberFormatException e) { return 0; }
  }

  private void updateTotalRate() {
    if (loadingReservation || txtTotalRate.isFocused()) return;

    if (selectedRooms.isEmpty()) {
      txtTotalRate.setText("0.00");
      return;
    }

    double pricePerNight = selectedRooms.stream().mapToDouble(Room::getPrice).sum();
    long nights = 1;

    if (dpCheckIn.getValue() != null && dpCheckOut.getValue() != null) {
      nights = ChronoUnit.DAYS.between(dpCheckIn.getValue(), dpCheckOut.getValue());
      if (nights <= 0) nights = 1;
    }

    txtTotalRate.setText(String.format("%.2f", pricePerNight * nights));
  }

  private void loadCustomers() {
    try {
      List<Customer> customers = customerDAO.listAll();
      activeCustomers.clear();
      for (Customer c : customers)
        if (c.getIdCustomerStatus() == 1) activeCustomers.add(c);
      lstCustomers.setItems(activeCustomers);
    } catch (Exception e) {
      System.err.println("Error loading customers: " + e.getMessage());
    }
  }

  private void configureCustomerSearch() {
    txtCustomerSearch.textProperty().addListener((obs, oldVal, newVal) -> {
      String text = newVal == null ? "" : newVal.trim();

      if (text.isEmpty()) {
        // Sin texto: mostrar los precargados (los más recientes)
        lstCustomers.setItems(activeCustomers);
        if (selectedCustomer != null) selectedCustomer = null;
        if (touchedFields.contains(txtCustomerSearch)) validateCustomerField();
        return;
      }

      // Con texto: buscar en la BD (ve TODOS los clientes)
      try {
        List<Customer> results = customerDAO.searchCustomers(text);
        ObservableList<Customer> filtered = FXCollections.observableArrayList();

        // Solo mostrar los activos (para reservar solo se permite activos)
        for (Customer c : results) {
          if (c.getIdCustomerStatus() == 1) {
            filtered.add(c);
          }
        }

        lstCustomers.setItems(filtered);

        // Si el usuario tipea distinto al cliente seleccionado, deseleccionar
        if (selectedCustomer != null) {
          String selected = selectedCustomer.getName() + " " + selectedCustomer.getSurname();
          if (!selected.equalsIgnoreCase(text)) selectedCustomer = null;
        }

      } catch (java.sql.SQLException e) {
        System.err.println("Error buscando clientes: " + e.getMessage());
        lstCustomers.setItems(FXCollections.observableArrayList());
      }

      if (touchedFields.contains(txtCustomerSearch)) validateCustomerField();
    });

    // Clic en un resultado
    lstCustomers.setOnMouseClicked(e -> {
      Customer c = lstCustomers.getSelectionModel().getSelectedItem();
      if (c != null) {
        selectedCustomer = c;
        txtCustomerSearch.setText(c.getName() + " " + c.getSurname());
        clearInvalid(txtCustomerSearch);
        lstCustomers.setVisible(false);
        lstCustomers.setManaged(false);
      }
    });

    // Al enfocar, mostrar la lista
    txtCustomerSearch.focusedProperty().addListener((obs, oldVal, focused) -> {
      if (focused) {
        lstCustomers.setVisible(true);
        lstCustomers.setManaged(true);
      }
    });
  }

  private void loadReservationStatuses() {
    try {
      cmbReservationStatus.getItems().setAll(reservationStatusRepo.getReservationStatuses());
    } catch (Exception e) { System.err.println("Error statuses: " + e.getMessage()); }
  }

  private void loadReservationTypes() {
    try {
      cmbReservationType.getItems().setAll(reservationTypeRepo.getReservationTypes());
    } catch (Exception e) { System.err.println("Error types: " + e.getMessage()); }
  }

  private void loadPaymentMethods() {
    try {
      cmbPaymentMethod.getItems().setAll(paymentMethodRepo.getPaymentMethods());
    } catch (Exception e) { System.err.println("Error payment methods: " + e.getMessage()); }
  }

  private void loadPaymentStatuses() {
    try {
      cmbPaymentStatus.getItems().setAll(paymentStatusRepo.getPaymentStatuses());
    } catch (Exception e) { System.err.println("Error payment statuses: " + e.getMessage()); }
  }

  private void loadProducts() {
    try {
      cmbProduct.getItems().setAll(productRepo.getActiveProducts());
    } catch (Exception e) { System.err.println("Error products: " + e.getMessage()); }
  }

  private void loadServices() {
    try {
      cmbService.getItems().setAll(serviceRepo.getActiveServices());
    } catch (Exception e) { System.err.println("Error services: " + e.getMessage()); }
  }

  private void configureConsumptionTable() {
    colConsumptionQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    colConsumptionUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
    colConsumptionTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

    colConsumptionType.setCellValueFactory(cellData -> {
      Consumption c = cellData.getValue();
      String t = c.getIdConsumptionType() == 1 ? "Producto"
              : c.getIdConsumptionType() == 2 ? "Servicio" : "Desconocido";
      return new javafx.beans.property.SimpleStringProperty(t);
    });

    colConsumptionName.setCellValueFactory(cellData -> {
      Consumption c = cellData.getValue();
      String name = "";

      if (c.getIdConsumptionType() == 1) {
        for (Product p : cmbProduct.getItems())
          if (p.getIdProduct() == c.getIdProduct()) { name = p.getName(); break; }
      } else if (c.getIdConsumptionType() == 2) {
        for (Service s : cmbService.getItems())
          if (s.getIdService() == c.getIdService()) { name = s.getName(); break; }
      }

      return new javafx.beans.property.SimpleStringProperty(name);
    });

    tblConsumptions.setItems(consumptions);

    colConsumptionActions.setCellFactory(column -> new TableCell<>() {
      private final Button btnEdit = new Button("Editar");
      private final Button btnDelete = new Button("Anular");
      private final HBox box = new HBox(5, btnEdit, btnDelete);

      {
        btnEdit.setOnAction(e -> startConsumptionEdit(getTableView().getItems().get(getIndex())));
        btnDelete.setOnAction(e -> deleteConsumptionFromRow(getTableView().getItems().get(getIndex())));
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : box);
      }
    });
  }

  private void startConsumptionEdit(Consumption c) {
    if (c == null) return;

    if (c.getIdConsumptionStatus() != 1) {
      showAlert(Alert.AlertType.WARNING, "Consumo", "No se puede modificar un consumo anulado.");
      return;
    }

    cmbConsumptionType.setValue(c.getIdConsumptionType() == 1 ? "Producto" : "Servicio");
    txtConsumptionQuantity.setText(String.valueOf(c.getQuantity()));

    if (c.getIdConsumptionType() == 1) {
      Product p = cmbProduct.getItems().stream()
              .filter(x -> x.getIdProduct() == c.getIdProduct()).findFirst().orElse(null);
      cmbProduct.setValue(p);
      cmbService.setValue(null);
      cmbProduct.setDisable(false);
      cmbService.setDisable(true);
    } else {
      Service s = cmbService.getItems().stream()
              .filter(x -> x.getIdService() == c.getIdService()).findFirst().orElse(null);
      cmbService.setValue(s);
      cmbProduct.setValue(null);
      cmbProduct.setDisable(true);
      cmbService.setDisable(false);
    }

    consumptionBeingEdited = c;
    btnConsumptionAction.setText("Guardar modificación");
  }

  private void deleteConsumptionFromRow(Consumption c) {
    if (c == null) return;

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Anular consumo");
    confirm.setHeaderText("¿Está seguro de anular este consumo?");
    confirm.setContentText("El consumo no será eliminado de la base de datos.");

    confirm.showAndWait().ifPresent(r -> {
      if (r == ButtonType.OK) {
        c.setIdConsumptionStatus(2);

        if (c.getIdConsumption() > 0 && !canceledConsumptions.contains(c))
          canceledConsumptions.add(c);

        consumptions.remove(c);
        modifiedConsumptions.remove(c);

        if (consumptionBeingEdited == c) {
          consumptionBeingEdited = null;
          btnConsumptionAction.setText("Agregar consumo");
        }

        updateConsumptionTableHeight();
        tblConsumptions.refresh();
        updateConsumptionTotal();
      }
    });
  }

  @FXML
  private void handleAddConsumption() {
    if (consumptionBeingEdited != null) {
      handleModifyConsumption();
      return;
    }

    String selectedType = cmbConsumptionType.getValue();

    if (selectedType == null) {
      showError("Debe seleccionar el tipo de consumo.");
      return;
    }

    int consumptionType = selectedType.equals("Producto") ? 1 : 2;
    String qtyText = txtConsumptionQuantity.getText().trim();

    if (qtyText.isEmpty()) {
      showError("Debe ingresar la cantidad.");
      return;
    }

    int quantity;
    try {
      quantity = Integer.parseInt(qtyText);
    } catch (NumberFormatException e) {
      showError("La cantidad debe ser numérica.");
      return;
    }

    if (quantity <= 0) {
      showError("La cantidad debe ser mayor que 0.");
      return;
    }

    if (quantity > 30) {
      showError("La cantidad máxima es 30.");
      return;
    }

    BigDecimal unitPrice;
    int idProduct = 0, idService = 0;

    if (consumptionType == 1) {
      Product p = cmbProduct.getValue();
      if (p == null) {
        showError("Seleccione un producto.");
        return;
      }
      idProduct = p.getIdProduct();
      unitPrice = p.getPrice();
    } else {
      Service s = cmbService.getValue();
      if (s == null) {
        showError("Seleccione un servicio.");
        return;
      }
      idService = s.getIdService();
      unitPrice = s.getPrice();
    }

    BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));

    Consumption c = new Consumption(0, consumptionType, idProduct, idService,
            quantity, unitPrice, total, LocalDateTime.now(), 1, null);
    c.setIdConsumptionStatus(1);
    consumptions.add(c);

    updateConsumptionTableHeight();
    updateConsumptionTotal();

    cmbProduct.setValue(null);
    cmbService.setValue(null);
    txtConsumptionQuantity.clear();
  }

  @FXML
  private void handleModifyConsumption() {
    if (consumptionBeingEdited != null) {
      String selectedType = cmbConsumptionType.getValue();

      if (selectedType == null) {
        showError("Seleccione el tipo.");
        return;
      }

      int consumptionType = selectedType.equals("Producto") ? 1 : 2;
      String qtyText = txtConsumptionQuantity.getText().trim();

      if (qtyText.isEmpty()) {
        showError("Ingrese la cantidad.");
        return;
      }

      int quantity;
      try {
        quantity = Integer.parseInt(qtyText);
      } catch (NumberFormatException e) {
        showError("La cantidad debe ser numérica.");
        return;
      }

      if (quantity <= 0) {
        showError("La cantidad debe ser mayor que 0.");
        return;
      }

      if (quantity > 30) {
        showError("La cantidad máxima es 30.");
        return;
      }

      int idProduct = 0, idService = 0;
      BigDecimal unitPrice;

      if (consumptionType == 1) {
        Product p = cmbProduct.getValue();
        if (p == null) {
          showError("Seleccione un producto.");
          return;
        }
        idProduct = p.getIdProduct();
        unitPrice = p.getPrice();
      } else {
        Service s = cmbService.getValue();
        if (s == null) {
          showError("Seleccione un servicio.");
          return;
        }
        idService = s.getIdService();
        unitPrice = s.getPrice();
      }

      BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));

      consumptionBeingEdited.setIdConsumptionType(consumptionType);
      consumptionBeingEdited.setIdProduct(idProduct);
      consumptionBeingEdited.setIdService(idService);
      consumptionBeingEdited.setQuantity(quantity);
      consumptionBeingEdited.setUnitPrice(unitPrice);
      consumptionBeingEdited.setTotal(total);

      if (consumptionBeingEdited.getIdConsumption() > 0
              && !modifiedConsumptions.contains(consumptionBeingEdited))
        modifiedConsumptions.add(consumptionBeingEdited);

      tblConsumptions.refresh();
      updateConsumptionTotal();

      consumptionBeingEdited = null;
      btnConsumptionAction.setText("Agregar consumo");
      cmbConsumptionType.setValue(null);
      cmbProduct.setValue(null);
      cmbService.setValue(null);
      txtConsumptionQuantity.clear();
      cmbProduct.setDisable(true);
      cmbService.setDisable(true);

      showAlert(Alert.AlertType.INFORMATION, "Consumo", "El consumo fue modificado.");
      return;
    }

    Consumption selected = tblConsumptions.getSelectionModel().getSelectedItem();

    if (selected == null) {
      showAlert(Alert.AlertType.WARNING, "Consumo", "Seleccione un consumo para modificar.");
      return;
    }

    if (selected.getIdConsumptionStatus() != 1) {
      showAlert(Alert.AlertType.WARNING, "Consumo", "No se puede modificar un consumo anulado.");
      return;
    }

    cmbConsumptionType.setValue(selected.getIdConsumptionType() == 1 ? "Producto" : "Servicio");
    txtConsumptionQuantity.setText(String.valueOf(selected.getQuantity()));

    if (selected.getIdConsumptionType() == 1) {
      Product p = cmbProduct.getItems().stream()
              .filter(x -> x.getIdProduct() == selected.getIdProduct()).findFirst().orElse(null);
      cmbProduct.setValue(p);
      cmbService.setValue(null);
      cmbProduct.setDisable(false);
      cmbService.setDisable(true);
    } else {
      Service s = cmbService.getItems().stream()
              .filter(x -> x.getIdService() == selected.getIdService()).findFirst().orElse(null);
      cmbService.setValue(s);
      cmbProduct.setValue(null);
      cmbProduct.setDisable(true);
      cmbService.setDisable(false);
    }

    consumptionBeingEdited = selected;
    btnConsumptionAction.setText("Guardar modificación");
  }

  private void updateConsumptionTotal() {
    BigDecimal total = BigDecimal.ZERO;
    for (Consumption c : consumptions)
      if (c.getTotal() != null) total = total.add(c.getTotal());
    txtConsumptionTotal.setText(total.toString());
  }

  @FXML
  private void handleDeleteConsumption() {
    Consumption selected = tblConsumptions.getSelectionModel().getSelectedItem();

    if (selected == null) {
      showAlert(Alert.AlertType.WARNING, "Consumo", "Seleccione un consumo.");
      return;
    }

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Anular consumo");
    confirm.setHeaderText("¿Está seguro?");

    confirm.showAndWait().ifPresent(r -> {
      if (r == ButtonType.OK) {
        selected.setIdConsumptionStatus(2);

        if (selected.getIdConsumption() > 0 && !canceledConsumptions.contains(selected))
          canceledConsumptions.add(selected);

        consumptions.remove(selected);
        modifiedConsumptions.remove(selected);

        if (consumptionBeingEdited == selected) {
          consumptionBeingEdited = null;
          btnConsumptionAction.setText("Agregar consumo");
        }

        updateConsumptionTableHeight();
        tblConsumptions.refresh();
        updateConsumptionTotal();
      }
    });
  }

  @FXML
  private void handleSave() {
    if (!validateAllFields()) return;

    Connection conn = null;

    try {
      int idCustomer = selectedCustomer.getIdCustomer();
      LocalDate checkIn = dpCheckIn.getValue();
      LocalDate checkOut = dpCheckOut.getValue();
      int numberOfGuests = Integer.parseInt(txtNumberOfGuests.getText().trim());
      BigDecimal totalRate = new BigDecimal(normalizeDecimal(txtTotalRate.getText()));
      ReservationStatus rs = cmbReservationStatus.getValue();
      ReservationType rt = cmbReservationType.getValue();
      String reservationObs = txtReservationObservations.getText();

      if (reservationObs != null && reservationObs.trim().isEmpty()) reservationObs = null;

      String paymentText = txtPaymentAmount.getText().trim();
      BigDecimal paymentAmount = null;
      LocalDateTime paymentDate = null;
      PaymentMethod paymentMethod = null;
      PaymentStatus paymentStatus = null;
      String paymentObs = null;

      if (!paymentText.isEmpty()) {
        paymentAmount = new BigDecimal(normalizeDecimal(paymentText));
        paymentDate = dpPaymentDate.getValue().atStartOfDay();
        paymentMethod = cmbPaymentMethod.getValue();
        paymentStatus = cmbPaymentStatus.getValue();
        paymentObs = txtPaymentObservations.getText();
        if (paymentObs != null && paymentObs.trim().isEmpty()) paymentObs = null;
      }

      Reservation reservation = new Reservation(
              idCustomer, LocalDateTime.now(), checkIn, checkOut,
              rs.getIdReservationStatus(), rt.getIdReservationType(),
              numberOfGuests, totalRate, reservationObs
      );

      conn = ConexionDB.getConnection();

      if (conn == null) {
        showError("Sin conexión a la BD.");
        return;
      }

      conn.setAutoCommit(false);

      int idReservation;

      if (reservationToEdit == null) {
        idReservation = reservationRepo.createReservation(conn, reservation);
      } else {
        reservation.setIdReservation(reservationToEdit.getIdReservation());

        boolean updated = reservationRepo.updateReservation(conn, reservation);

        if (!updated) {
          conn.rollback();
          showError("No se pudo actualizar la reserva.");
          return;
        }

        idReservation = reservationToEdit.getIdReservation();
      }

      if (idReservation <= 0) {
        conn.rollback();
        showError("No se pudo guardar la reserva.");
        return;
      }

      for (Consumption c : consumptions) {
        if (c.getIdConsumption() == 0) {
          c.setIdReservation(idReservation);
          boolean created = consumptionRepo.createConsumption(conn, c);

          if (!created) {
            conn.rollback();
            showError("No se pudo registrar un consumo.");
            return;
          }
        }
      }

      for (Consumption c : modifiedConsumptions) {
        boolean updated = consumptionRepo.updateConsumption(conn, c);

        if (!updated) {
          conn.rollback();
          showError("No se pudo actualizar un consumo.");
          return;
        }
      }

      for (Consumption c : canceledConsumptions) {
        boolean updated = consumptionRepo.updateConsumption(conn, c);

        if (!updated) {
          conn.rollback();
          showError("No se pudo anular un consumo.");
          return;
        }
      }

      if (!paymentText.isEmpty()) {
        Payment payment = new Payment(
                idReservation, paymentAmount, paymentDate,
                paymentMethod.getIdPaymentMethod(),
                paymentStatus.getIdPaymentStatus(), paymentObs
        );

        if (reservationToEdit != null) {
          List<Payment> payments = paymentRepo.getPaymentsByReservation(idReservation);

          if (!payments.isEmpty()) {
            payment.setIdPayment(payments.get(0).getIdPayment());

            boolean updated = paymentRepo.updatePayment(conn, payment);

            if (!updated) {
              conn.rollback();
              showError("No se pudo actualizar el pago.");
              return;
            }
          } else {
            boolean created = paymentRepo.createPayment(conn, payment);

            if (!created) {
              conn.rollback();
              showError("No se pudo registrar el pago.");
              return;
            }
          }
        } else {
          boolean created = paymentRepo.createPayment(conn, payment);

          if (!created) {
            conn.rollback();
            showError("No se pudo registrar el pago.");
            return;
          }
        }
      }

      if (reservationToEdit != null)
        reservationRoomRepo.deleteByReservation(conn, idReservation);

      for (Room room : selectedRooms)
        reservationRoomRepo.create(conn, new ReservationRoom(idReservation, room.getIdRoom()));
      conn.commit();

      showSuccess(
              reservationToEdit == null
                      ? "Reserva creada.\nN° " + idReservation
                      : "Reserva modificada.\nN° " + idReservation
      );

      handleBack();

    } catch (SQLException e) {
      try {
        if (conn != null) conn.rollback();
      } catch (SQLException ex) { ex.printStackTrace(); }
      showError("Error en la transacción: " + e.getMessage());

    } catch (Exception e) {
      try {
        if (conn != null) conn.rollback();
      } catch (SQLException ex) { ex.printStackTrace(); }
      e.printStackTrace();
      showError("Error al guardar la reserva.");

    } finally {
      try {
        if (conn != null) {
          conn.setAutoCommit(true);
          conn.close();
        }
      } catch (SQLException e) { e.printStackTrace(); }
    }
  }

  @FXML
  private void handleChangeRoom() {
    allowRoomChange = !allowRoomChange;

    if (btnChangeRoom != null) {
      btnChangeRoom.setText(allowRoomChange
              ? "✖ Cancelar cambio"
              : "🔄 Cambiar habitación");
    }

    loadAvailableRooms();
  }

  @FXML
  private void handleBack() {
    if (dashboardController != null)
      dashboardController.loadView("/views/reservations.fxml");
  }

  @FXML
  private void handleCancel() {
    Alert c1 = new Alert(Alert.AlertType.CONFIRMATION);
    c1.setTitle("Cancelar reserva");
    c1.setHeaderText("¿Está seguro?");
    c1.setContentText("Los datos se perderán.");

    ButtonType yes = new ButtonType("Sí");
    ButtonType no = new ButtonType("No");
    c1.getButtonTypes().setAll(yes, no);

    c1.showAndWait().ifPresent(r -> {
      if (r == yes) {
        Alert c2 = new Alert(Alert.AlertType.CONFIRMATION);
        c2.setTitle("Confirmar");
        c2.setHeaderText("¿Desea continuar?");

        ButtonType confirm = new ButtonType("Sí, cancelar");
        ButtonType back = new ButtonType("Volver");
        c2.getButtonTypes().setAll(confirm, back);

        c2.showAndWait().ifPresent(r2 -> {
          if (r2 == confirm) handleBack();
        });
      }
    });
  }

  private void markInvalid(Control field, String message) {
    if (field == null) return;
    field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 5;");
    field.setTooltip(new Tooltip(message));
  }

  private void clearInvalid(Control field) {
    if (field == null) return;
    field.setStyle("");
    field.setTooltip(null);
  }

  private void markRoomsInvalid(String message) {
    roomsTouched = true;
    roomsContainer.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 5;");
    if (roomsValidationTooltip != null)
      Tooltip.uninstall(roomsContainer, roomsValidationTooltip);
    roomsValidationTooltip = new Tooltip(message);
    Tooltip.install(roomsContainer, roomsValidationTooltip);
  }

  private void clearRoomsInvalid() {
    roomsTouched = false;
    roomsContainer.setStyle("");

    if (roomsValidationTooltip != null) {
      Tooltip.uninstall(roomsContainer, roomsValidationTooltip);
      roomsValidationTooltip = null;
    }
  }

  public void setSelectedRoom(int roomNumber) {
    openedFromBookingChart = true;
    selectedRoomFromChart = roomNumber;
    selectedRooms.clear();

    for (Room room : activeRooms) {
      if (room.getNumber() == roomNumber) {
        selectedRooms.add(room);
        break;
      }
    }

    clearRoomsInvalid();
    loadRoomCards();
    updateTotalRate();
  }

  public void setSelectedDates(LocalDate checkIn, LocalDate checkOut) {
    dpCheckIn.setValue(checkIn);
    dpCheckOut.setValue(checkOut);
    loadAvailableRooms();
    updateTotalRate();
  }

  private void updateConsumptionTableHeight() {
    int cantidad = tblConsumptions.getItems().size();
    double headerHeight = 30, rowHeight = 32, minHeight = 35, maxHeight = 180;
    double calculatedHeight = cantidad == 0 ? minHeight : headerHeight + (cantidad * rowHeight);
    double finalHeight = Math.min(calculatedHeight, maxHeight);
    tblConsumptions.setPrefHeight(finalHeight);
    tblConsumptions.setMinHeight(finalHeight);
  }

  private void showError(String msg) {
    Alert a = new Alert(Alert.AlertType.ERROR);
    a.setTitle("Error");
    a.setHeaderText("No se pudo realizar la operación");
    a.setContentText(msg);
    a.showAndWait();
  }

  private void showSuccess(String msg) {
    Alert a = new Alert(Alert.AlertType.INFORMATION);
    a.setTitle("Éxito");
    a.setHeaderText("Operación realizada");
    a.setContentText(msg);
    a.showAndWait();
  }

  private void showAlert(Alert.AlertType type, String title, String msg) {
    Alert a = new Alert(type);
    a.setTitle(title);
    a.setHeaderText(null);
    a.setContentText(msg);
    a.showAndWait();
  }

  private void installDecimalFilter(TextField field) {
    if (field == null) return;

    field.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
      String newText = change.getControlNewText();

      // Allow empty (to clear the field)
      if (newText.isEmpty()) return change;

      // Allow digits with at most ONE dot or comma as decimal separator
      if (!newText.matches("\\d*[.,]?\\d*")) return null;

      return change;
    }));
  }
}