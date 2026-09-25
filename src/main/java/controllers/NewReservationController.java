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
import java.util.ArrayList;
import java.util.List;

public class NewReservationController {

  // ============================================================
  // CAMPOS FXML
  // ============================================================
  @FXML
  Label lblReservationTitle;
  @FXML
  private TextField txtCustomerSearch;
  @FXML
  private ListView<Customer> lstCustomers;
  @FXML
  private DatePicker dpCheckIn;
  @FXML
  private DatePicker dpCheckOut;
  @FXML
  private TextField txtNumberOfGuests;
  @FXML
  private TextField txtTotalRate;
  @FXML
  private ComboBox<ReservationStatus> cmbReservationStatus;
  @FXML
  private ComboBox<ReservationType> cmbReservationType;
  @FXML
  private TextArea txtReservationObservations;

  @FXML
  private FlowPane roomsContainer;

  @FXML
  private TextField txtPaymentAmount;
  @FXML
  private DatePicker dpPaymentDate;
  @FXML
  private ComboBox<PaymentMethod> cmbPaymentMethod;
  @FXML
  private ComboBox<PaymentStatus> cmbPaymentStatus;
  @FXML
  private TextArea txtPaymentObservations;

  @FXML
  private ComboBox<String> cmbConsumptionType;
  @FXML
  private ComboBox<Product> cmbProduct;
  @FXML
  private ComboBox<Service> cmbService;
  @FXML
  private TextField txtConsumptionQuantity;
  @FXML
  private TableView<Consumption> tblConsumptions;
  @FXML
  private TableColumn<Consumption, Integer> colConsumptionQuantity;
  @FXML
  private TableColumn<Consumption, BigDecimal> colConsumptionUnitPrice;
  @FXML
  private TableColumn<Consumption, BigDecimal> colConsumptionTotal;
  @FXML
  private TableColumn<Consumption, String> colConsumptionType;
  @FXML
  private TableColumn<Consumption, String> colConsumptionName;
  @FXML
  private TableColumn<Consumption, Void> colConsumptionActions;
  @FXML
  private TextField txtConsumptionTotal;
  @FXML
  private Button btnConsumptionAction;

  // ============================================================
  // ESTADO
  // ============================================================
  private final ObservableList<Customer> activeCustomers = FXCollections.observableArrayList();
  private Customer selectedCustomer;

  private final ObservableList<Room> activeRooms = FXCollections.observableArrayList();
  private final ObservableList<Room> selectedRooms = FXCollections.observableArrayList();
  private final ObservableList<Room> availableRooms = FXCollections.observableArrayList();

  private DashboardController dashboardController;
  private Reservation reservationToEdit;

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
  private Consumption consumptionBeingEdited = null;

  private final HotelTourService hotelTourService = new HotelTourService();
  private List<HotelTour> availableTours = new ArrayList<>();

  private boolean loadingReservation = false;

  // ============================================================
  // CONSTRUCTOR
  // ============================================================
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

  // ============================================================
  // INITIALIZE
  // ============================================================
  @FXML
  public void initialize() {
    System.out.println("=== NewReservationController.initialize() ===");

    // Verificación defensiva de los fx:id críticos
    if (txtNumberOfGuests == null)
      System.err.println("!! txtNumberOfGuests es null");
    if (roomsContainer == null)
      System.err.println("!! roomsContainer es null");
    if (txtTotalRate == null)
      System.err.println("!! txtTotalRate es null");

    // Cargar habitaciones activas
    activeRooms.setAll(roomDAO.listActive());
    System.out.println("Habitaciones activas cargadas: " + activeRooms.size());

    // Placeholder inicial (sin huéspedes → sin habitaciones)
    loadRoomCards();

    // Tabla consumos
    configureConsumptionTable();

    // Tipo consumo
    cmbConsumptionType.setItems(FXCollections.observableArrayList("Producto", "Servicio"));
    cmbConsumptionType.setOnAction(event -> {
      String typeValue = cmbConsumptionType.getValue();
      if (typeValue == null) {
        cmbProduct.setDisable(true);
        cmbService.setDisable(true);
        return;
      }
      if (typeValue.equals("Producto")) {
        cmbProduct.setDisable(false);
        cmbService.setDisable(true);
        cmbService.setValue(null);
      } else {
        cmbProduct.setDisable(true);
        cmbService.setDisable(false);
        cmbProduct.setValue(null);
      }
    });

    // Cargar combos
    loadReservationStatuses();
    loadReservationTypes();
    loadPaymentMethods();
    loadPaymentStatuses();
    loadProducts();
    loadServices();
    loadCustomers();
    configureCustomerSearch();

    // Estado inicial consumos
    txtConsumptionTotal.setText("0.00");
    txtConsumptionTotal.setEditable(false);
    cmbProduct.setDisable(true);
    cmbService.setDisable(true);

    // ------------------------------------------------------------
    // LISTENERS (los importantes para el filtro y el precio)
    // ------------------------------------------------------------
    txtNumberOfGuests.textProperty().addListener((obs, oldVal, newVal) -> {
      System.out.println("[listener] txtNumberOfGuests cambió a: '" + newVal
          + "' → parseado = " + getRequestedGuests());
      loadAvailableRooms();
      updateTotalRate();
    });

    dpCheckIn.valueProperty().addListener((obs, oldVal, newVal) -> {
      System.out.println("[listener] dpCheckIn = " + newVal);
      loadAvailableRooms();
      updateTotalRate();
    });

    dpCheckOut.valueProperty().addListener((obs, oldVal, newVal) -> {
      System.out.println("[listener] dpCheckOut = " + newVal);
      loadAvailableRooms();
      updateTotalRate();
    });

    System.out.println("=== NewReservationController.initialize() OK ===");
  }

  // ============================================================
  // DASHBOARD
  // ============================================================
  public void setDashboardController(DashboardController dashboardController) {
    this.dashboardController = dashboardController;
  }

  // ============================================================
  // CARGAR RESERVA A EDITAR
  // ============================================================
  public void setReservationToEdit(Reservation reservation) {
    this.reservationToEdit = reservation;
    this.loadingReservation = true;

    lblReservationTitle.setText("Editar Reserva");
    if (reservation == null) {
      loadingReservation = false;
      return;
    }

    // Cliente
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
    txtTotalRate.setText(reservation.getTotalRate() != null ? reservation.getTotalRate().toString() : "");

    cmbReservationStatus.getItems().stream()
        .filter(s -> s.getIdReservationStatus() == reservation.getIdReservationStatus())
        .findFirst().ifPresent(cmbReservationStatus::setValue);

    cmbReservationType.getItems().stream()
        .filter(t -> t.getIdReservationType() == reservation.getIdReservationType())
        .findFirst().ifPresent(cmbReservationType::setValue);

    // Rooms
    activeRooms.setAll(roomDAO.listActive());
    List<ReservationRoom> reservationRooms = reservationRoomRepo.getByReservation(reservationToEdit.getIdReservation());
    selectedRooms.clear();
    for (ReservationRoom rr : reservationRooms) {
      for (Room room : activeRooms) {
        if (room.getNumber() == rr.getRoomNumber()) {
          selectedRooms.add(room);
          break;
        }
      }
    }
    loadAvailableRooms();

    txtReservationObservations.setText(
        reservation.getObservations() == null ? "" : reservation.getObservations());

    loadReservationPayment(reservation.getIdReservation());
    loadReservationConsumptions(reservation.getIdReservation());

    loadingReservation = false;
  }

  // ============================================================
  // LOAD PAGO / CONSUMOS
  // ============================================================
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
      txtPaymentObservations.setText(payment.getObservations() == null ? "" : payment.getObservations());
    } catch (Exception e) {
      System.err.println("Error cargando pago: " + e.getMessage());
    }
  }

  private void loadReservationConsumptions(int idReservation) {
    try {
      List<Consumption> consumptionsBD = consumptionRepo.getConsumptionsByReservation(idReservation);
      consumptions.clear();
      consumptions.addAll(consumptionsBD);
      tblConsumptions.refresh();
      updateConsumptionTotal();
    } catch (Exception e) {
      System.err.println("Error cargando consumos: " + e.getMessage());
    }
  }

  // ============================================================
  // ROOMS — CARGA DE TARJETAS
  // ============================================================
  private void loadRoomCards() {
    roomsContainer.getChildren().clear();

    int requested = getRequestedGuests();
    System.out.println("[loadRoomCards] requested=" + requested
        + " availableRooms=" + availableRooms.size()
        + " availableTours=" + availableTours.size());

    // 1. Sin cantidad de huéspedes
    if (requested <= 0) {
      Label placeholder = new Label(
          "Ingresá la cantidad de huéspedes para ver las habitaciones disponibles.");
      placeholder.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-font-size: 13;");
      roomsContainer.getChildren().add(placeholder);
      return;
    }

    // 2. Mostrar habitaciones directas (si las hay)
    if (!availableRooms.isEmpty()) {
      java.util.Set<Integer> selectedNumbers = selectedRooms.stream()
          .map(Room::getNumber)
          .collect(java.util.stream.Collectors.toSet());

      for (Room room : availableRooms) {
        VBox card = new VBox(5);

        Label lblNumber = new Label("Habitación " + room.getNumber());
        Label lblType = new Label(room.getTypeName());
        Label lblView = new Label(room.getViewName());
        Label lblCapacity = new Label("Capacidad: " + room.getCapacity());
        Label lblPrice = new Label(String.format("$ %.2f / noche", room.getPrice()));

        card.getChildren().addAll(lblNumber, lblType, lblView, lblCapacity, lblPrice);
        card.getStyleClass().add("room-card");

        if (selectedNumbers.contains(room.getNumber())) {
          card.getStyleClass().add("selected");
        }

        card.setOnMouseClicked(event -> {
          boolean isSelected = selectedRooms.stream()
              .anyMatch(r -> r.getNumber() == room.getNumber());
          if (isSelected) {
            selectedRooms.removeIf(r -> r.getNumber() == room.getNumber());
            card.getStyleClass().remove("selected");
          } else {
            selectedRooms.add(room);
            card.getStyleClass().add("selected");
          }
          updateTotalRate();
        });

        roomsContainer.getChildren().add(card);
      }
    }

    // 3. Mostrar tours (independiente de si hay o no habitaciones directas)
    if (!availableTours.isEmpty()) {
      Label lblTourTitle = new Label("🏨 Modo Hotel Tour — cambiás de habitación durante la estadía:");
      lblTourTitle.setStyle("-fx-text-fill: #2d6cdf; -fx-font-weight: bold; -fx-padding: 10 0 4 0;");
      roomsContainer.getChildren().add(lblTourTitle);

      for (HotelTour tour : availableTours) {
        VBox tourCard = new VBox(4);
        tourCard.setStyle("-fx-background-color: #eef4ff; -fx-border-color: #2d6cdf;"
            + " -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12;"
            + " -fx-cursor: hand;");

        Label lblRooms = new Label("Habitaciones: " + tour.getRoomsSummary());
        lblRooms.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d6cdf;");

        Label lblMoves = new Label(tour.getMoves() + " cambio(s) de habitación");
        Label lblPrice = new Label(String.format("Total: $ %.2f", tour.getTotalPrice()));

        tourCard.getChildren().addAll(lblRooms, lblMoves, lblPrice);

        for (TourSegment seg : tour.getSegments()) {
          Label segLabel = new Label(String.format("  • Hab. %d del %s al %s (%d noches, $%.2f)",
              seg.getRoom().getNumber(), seg.getFrom(), seg.getTo(),
              seg.getNights(), seg.getSubtotal()));
          segLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 11;");
          tourCard.getChildren().add(segLabel);
        }

        tourCard.setOnMouseClicked(e -> {
          selectedRooms.clear();
          for (TourSegment seg : tour.getSegments()) {
            selectedRooms.add(seg.getRoom());
          }
          loadRoomCards();
          updateTotalRate();
        });

        roomsContainer.getChildren().add(tourCard);
      }
    }

    // 4. Si NO hay NADA (ni habitaciones ni tours) → mensaje de error
    if (availableRooms.isEmpty() && availableTours.isEmpty()) {
      Label placeholder = new Label(
          "No hay habitaciones ni tours disponibles para " + requested + " huésped(es).");
      placeholder.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-font-size: 13;");
      roomsContainer.getChildren().add(placeholder);
    }
  }

  // ============================================================
  // ROOMS — FILTRO
  // ============================================================
  private void loadAvailableRooms() {
    int requested = getRequestedGuests();

    // Sin cantidad válida → vaciar y mostrar placeholder
    if (requested <= 0) {
      availableRooms.clear();
      loadRoomCards();
      return;
    }

    // Sin fechas → solo filtrar por capacidad
    if (dpCheckIn.getValue() == null || dpCheckOut.getValue() == null) {
      availableRooms.clear();
      for (Room room : activeRooms) {
        if (hasEnoughCapacity(room))
          availableRooms.add(room);
      }
      loadRoomCards();
      return;
    }

    if (!dpCheckOut.getValue().isAfter(dpCheckIn.getValue())) {
      return;
    }

    Integer idReservationToExclude = reservationToEdit != null
        ? reservationToEdit.getIdReservation()
        : null;

    List<Integer> occupiedRooms = reservationRoomRepo.getOccupiedRoomNumbers(
        dpCheckIn.getValue(), dpCheckOut.getValue(), idReservationToExclude);

    availableRooms.clear();
    for (Room room : activeRooms) {
      boolean isFree = !occupiedRooms.contains(room.getNumber());
      boolean alreadyPicked = selectedRooms.stream()
          .anyMatch(r -> r.getNumber() == room.getNumber());
      boolean hasCapacity = hasEnoughCapacity(room);

      if ((isFree && hasCapacity) || alreadyPicked) {
        availableRooms.add(room);
      }
    }
    if (getRequestedGuests() > 0
        && dpCheckIn.getValue() != null
        && dpCheckOut.getValue() != null
        && dpCheckOut.getValue().isAfter(dpCheckIn.getValue())) {

      HotelTourService.HotelTourResult result = hotelTourService.findOptions(
          dpCheckIn.getValue(),
          dpCheckOut.getValue(),
          getRequestedGuests(),
          activeRooms);

      availableTours = result.getTours();
    } else {
      availableTours = new ArrayList<>();
    }

    loadRoomCards();
  }

  private boolean hasEnoughCapacity(Room room) {
    int requested = getRequestedGuests();
    if (requested <= 0)
      return true;
    return room.getCapacity() >= requested;
  }

  /** Lee `txtNumberOfGuests`; devuelve 0 si está vacío o mal formado. */
  private int getRequestedGuests() {
    if (txtNumberOfGuests == null)
      return 0;
    String text = txtNumberOfGuests.getText();
    if (text == null || text.trim().isEmpty())
      return 0;
    try {
      int n = Integer.parseInt(text.trim());
      return n > 0 ? n : 0;
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  // ============================================================
  // TARIFA AUTOMÁTICA
  // ============================================================
  private void updateTotalRate() {
    if (loadingReservation)
      return;
    if (txtTotalRate.isFocused())
      return; // el usuario está editando → no pisar

    if (selectedRooms.isEmpty()) {
      System.out.println("[updateTotalRate] selectedRooms vacío → no recalculo");
      return;
    }

    double pricePerNight = selectedRooms.stream().mapToDouble(Room::getPrice).sum();

    long nights = 1;
    if (dpCheckIn.getValue() != null && dpCheckOut.getValue() != null) {
      nights = ChronoUnit.DAYS.between(dpCheckIn.getValue(), dpCheckOut.getValue());
      if (nights <= 0)
        nights = 1;
    }

    double total = pricePerNight * nights;
    System.out.println("[updateTotalRate] pricePerNight=" + pricePerNight
        + " nights=" + nights + " total=" + total);

    txtTotalRate.setText(String.format("%.2f", total));
  }

  // ============================================================
  // CUSTOMERS
  // ============================================================
  private void loadCustomers() {
    try {
      List<Customer> customers = customerDAO.listAll();
      activeCustomers.clear();
      for (Customer c : customers) {
        if (c.getIdCustomerStatus() == 1)
          activeCustomers.add(c);
      }
      lstCustomers.setItems(activeCustomers);
    } catch (Exception e) {
      System.err.println("Error loading customers: " + e.getMessage());
    }
  }

  private void configureCustomerSearch() {
    txtCustomerSearch.textProperty().addListener((obs, oldVal, newVal) -> {
      String text = newVal.trim().toLowerCase();
      if (text.isEmpty()) {
        lstCustomers.setItems(activeCustomers);
        return;
      }
      ObservableList<Customer> filtered = FXCollections.observableArrayList();
      for (Customer c : activeCustomers) {
        String name = c.getName().toLowerCase();
        String surname = c.getSurname().toLowerCase();
        String document = c.getDocumentNumber() != null ? c.getDocumentNumber().toLowerCase() : "";
        if (name.contains(text) || surname.contains(text) || document.contains(text)) {
          filtered.add(c);
        }
      }
      lstCustomers.setItems(filtered);
    });

    lstCustomers.setOnMouseClicked(e -> {
      Customer c = lstCustomers.getSelectionModel().getSelectedItem();
      if (c != null) {
        selectedCustomer = c;
        txtCustomerSearch.setText(c.getName() + " " + c.getSurname());
        lstCustomers.setVisible(false);
        lstCustomers.setManaged(false);
      }
    });

    txtCustomerSearch.focusedProperty().addListener((obs, oldVal, focused) -> {
      if (focused) {
        lstCustomers.setVisible(true);
        lstCustomers.setManaged(true);
      }
    });
  }

  // ============================================================
  // CARGAR COMBOS
  // ============================================================
  private void loadReservationStatuses() {
    try {
      cmbReservationStatus.getItems().setAll(reservationStatusRepo.getReservationStatuses());
    } catch (Exception e) {
      System.err.println("Error statuses: " + e.getMessage());
    }
  }

  private void loadReservationTypes() {
    try {
      cmbReservationType.getItems().setAll(reservationTypeRepo.getReservationTypes());
    } catch (Exception e) {
      System.err.println("Error types: " + e.getMessage());
    }
  }

  private void loadPaymentMethods() {
    try {
      cmbPaymentMethod.getItems().setAll(paymentMethodRepo.getPaymentMethods());
    } catch (Exception e) {
      System.err.println("Error payment methods: " + e.getMessage());
    }
  }

  private void loadPaymentStatuses() {
    try {
      cmbPaymentStatus.getItems().setAll(paymentStatusRepo.getPaymentStatuses());
    } catch (Exception e) {
      System.err.println("Error payment statuses: " + e.getMessage());
    }
  }

  private void loadProducts() {
    try {
      cmbProduct.getItems().setAll(productRepo.getActiveProducts());
    } catch (Exception e) {
      System.err.println("Error products: " + e.getMessage());
    }
  }

  private void loadServices() {
    try {
      cmbService.getItems().setAll(serviceRepo.getActiveServices());
    } catch (Exception e) {
      System.err.println("Error services: " + e.getMessage());
    }
  }

  // ============================================================
  // CONSUMOS
  // ============================================================
  private void configureConsumptionTable() {
    colConsumptionQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    colConsumptionUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
    colConsumptionTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

    colConsumptionType.setCellValueFactory(cellData -> {
      Consumption c = cellData.getValue();
      String t = c.getIdConsumptionType() == 1 ? "Producto"
          : c.getIdConsumptionType() == 2 ? "Servicio"
              : "Desconocido";
      return new javafx.beans.property.SimpleStringProperty(t);
    });

    colConsumptionName.setCellValueFactory(cellData -> {
      Consumption c = cellData.getValue();
      String name = "";
      if (c.getIdConsumptionType() == 1) {
        for (Product p : cmbProduct.getItems())
          if (p.getIdProduct() == c.getIdProduct()) {
            name = p.getName();
            break;
          }
      } else if (c.getIdConsumptionType() == 2) {
        for (Service s : cmbService.getItems())
          if (s.getIdService() == c.getIdService()) {
            name = s.getName();
            break;
          }
      }
      return new javafx.beans.property.SimpleStringProperty(name);
    });

    tblConsumptions.setItems(consumptions);

    colConsumptionActions.setCellFactory(column -> new TableCell<>() {
      private final Button btnEdit = new Button("✏️");
      private final Button btnDelete = new Button("❌");
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
    if (c == null)
      return;
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
    if (c == null)
      return;
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Anular consumo");
    confirm.setHeaderText("¿Está seguro de anular este consumo?");
    confirm.setContentText("El consumo no será eliminado de la base de datos.");
    confirm.showAndWait().ifPresent(r -> {
      if (r == ButtonType.OK) {
        c.setIdConsumptionStatus(2);
        consumptions.remove(c);
        modifiedConsumptions.remove(c);
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
          && !modifiedConsumptions.contains(consumptionBeingEdited)) {
        modifiedConsumptions.add(consumptionBeingEdited);
      }

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

    // Iniciar edición
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
    for (Consumption c : consumptions) {
      if (c.getTotal() != null)
        total = total.add(c.getTotal());
    }
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
        consumptions.remove(selected);
        modifiedConsumptions.remove(selected);
        tblConsumptions.refresh();
        updateConsumptionTotal();
      }
    });
  }

  // ============================================================
  // GUARDAR
  // ============================================================
  @FXML
  private void handleSave() {
    Connection conn = null;
    try {
      List<String> errors = new ArrayList<>();

      int idCustomer = 0;
      if (selectedCustomer == null)
        errors.add("Debe seleccionar un cliente.");
      else
        idCustomer = selectedCustomer.getIdCustomer();

      LocalDate checkIn = dpCheckIn.getValue();
      LocalDate checkOut = dpCheckOut.getValue();
      if (checkIn == null)
        errors.add("Seleccione fecha de check-in.");
      if (checkOut == null)
        errors.add("Seleccione fecha de check-out.");
      if (checkIn != null && checkOut != null && !checkOut.isAfter(checkIn))
        errors.add("Check-out debe ser posterior a check-in.");

      String guestsText = txtNumberOfGuests.getText().trim();
      int numberOfGuests = 0;
      if (guestsText.isEmpty())
        errors.add("Ingrese cantidad de huéspedes.");
      else {
        try {
          numberOfGuests = Integer.parseInt(guestsText);
          if (numberOfGuests <= 0)
            errors.add("La cantidad de huéspedes debe ser positiva.");
        } catch (NumberFormatException e) {
          errors.add("Cantidad de huéspedes inválida.");
        }
      }

      String rateText = txtTotalRate.getText().trim();
      BigDecimal totalRate = null;
      if (rateText.isEmpty())
        errors.add("Ingrese la tarifa total.");
      else {
        try {
          totalRate = new BigDecimal(rateText);
          if (totalRate.compareTo(BigDecimal.ZERO) <= 0)
            errors.add("La tarifa debe ser mayor a 0.");
          else if (totalRate.compareTo(new BigDecimal("999999999.99")) > 0)
            errors.add("La tarifa supera el máximo.");
        } catch (NumberFormatException e) {
          errors.add("Tarifa inválida.");
        }
      }

      ReservationStatus rs = cmbReservationStatus.getValue();
      if (rs == null)
        errors.add("Seleccione estado de la reserva.");

      ReservationType rt = cmbReservationType.getValue();
      if (rt == null)
        errors.add("Seleccione tipo de reserva.");

      String reservationObs = txtReservationObservations.getText();
      if (reservationObs != null && reservationObs.trim().isEmpty())
        reservationObs = null;

      String paymentText = txtPaymentAmount.getText().trim();
      BigDecimal paymentAmount = null;
      LocalDateTime paymentDate = null;
      PaymentMethod paymentMethod = null;
      PaymentStatus paymentStatus = null;
      String paymentObs = null;

      if (!paymentText.isEmpty()) {
        try {
          paymentAmount = new BigDecimal(paymentText);
          if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0)
            errors.add("El pago debe ser > 0.");
        } catch (NumberFormatException e) {
          errors.add("Importe del pago inválido.");
        }
        if (dpPaymentDate.getValue() == null)
          errors.add("Seleccione fecha del pago.");
        else
          paymentDate = dpPaymentDate.getValue().atStartOfDay();
        paymentMethod = cmbPaymentMethod.getValue();
        if (paymentMethod == null)
          errors.add("Seleccione método de pago.");
        paymentStatus = cmbPaymentStatus.getValue();
        if (paymentStatus == null)
          errors.add("Seleccione estado del pago.");
        paymentObs = txtPaymentObservations.getText();
        if (paymentObs != null && paymentObs.trim().isEmpty())
          paymentObs = null;
      }

      if (!errors.isEmpty()) {
        if (errors.size() == 1)
          showError(errors.get(0));
        else
          showErrors(errors);
        return;
      }

      Reservation reservation = new Reservation(
          idCustomer, LocalDateTime.now(), checkIn, checkOut,
          rs.getIdReservationStatus(), rt.getIdReservationType(),
          numberOfGuests, totalRate, reservationObs);

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
        if (!reservationRepo.updateReservation(conn, reservation)) {
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
          if (!consumptionRepo.createConsumption(conn, c)) {
            conn.rollback();
            showError("No se pudo registrar un consumo.");
            return;
          }
        }
      }

      for (Consumption c : modifiedConsumptions) {
        if (!consumptionRepo.updateConsumption(conn, c)) {
          conn.rollback();
          showError("No se pudo actualizar un consumo.");
          return;
        }
      }

      if (!paymentText.isEmpty()) {
        Payment payment = new Payment(idReservation, paymentAmount, paymentDate,
            paymentMethod.getIdPaymentMethod(), paymentStatus.getIdPaymentStatus(), paymentObs);

        if (reservationToEdit != null) {
          List<Payment> payments = paymentRepo.getPaymentsByReservation(idReservation);
          if (!payments.isEmpty()) {
            payment.setIdPayment(payments.get(0).getIdPayment());
            if (!paymentRepo.updatePayment(conn, payment)) {
              conn.rollback();
              showError("No se pudo actualizar el pago.");
              return;
            }
          } else if (!paymentRepo.createPayment(conn, payment)) {
            conn.rollback();
            showError("No se pudo registrar el pago.");
            return;
          }
        } else if (!paymentRepo.createPayment(conn, payment)) {
          conn.rollback();
          showError("No se pudo registrar el pago.");
          return;
        }
      }

      if (reservationToEdit != null) {
        reservationRoomRepo.deleteByReservation(conn, idReservation);
      }
      for (Room room : selectedRooms) {
        reservationRoomRepo.create(conn, new ReservationRoom(idReservation, room.getNumber()));
      }

      conn.commit();
      showSuccess(reservationToEdit == null
          ? "Reserva creada.\nN° " + idReservation
          : "Reserva modificada.\nN° " + idReservation);
      handleBack();

    } catch (SQLException e) {
      try {
        if (conn != null)
          conn.rollback();
      } catch (SQLException ex) {
      }
      showError("Error en la transacción: " + e.getMessage());
    } catch (Exception e) {
      try {
        if (conn != null)
          conn.rollback();
      } catch (SQLException ex) {
      }
      e.printStackTrace();
      showError("Error al guardar la reserva.");
    } finally {
      try {
        if (conn != null) {
          conn.setAutoCommit(true);
          conn.close();
        }
      } catch (SQLException e) {
      }
    }
  }

  // ============================================================
  // NAVEGACIÓN
  // ============================================================
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
          if (r2 == confirm)
            handleBack();
        });
      }
    });
  }

  // ============================================================
  // HELPERS
  // ============================================================
  private void showError(String msg) {
    Alert a = new Alert(Alert.AlertType.ERROR);
    a.setTitle("Error");
    a.setHeaderText("No se pudo guardar");
    a.setContentText(msg);
    a.showAndWait();
  }

  private void showErrors(List<String> errors) {
    Alert a = new Alert(Alert.AlertType.ERROR);
    a.setTitle("Errores");
    a.setHeaderText("Revise:");
    a.setContentText("• " + String.join("\n• ", errors));
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
}
