package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import models.Reservation;
import models.ReservationRoom;
import models.Room;

import repositories.ReservationRepo;
import repositories.ReservationRoomRepo;
import repositories.RoomDAO;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class BookingChartController {

  @FXML
  private Button btnPreviousMonth;
  @FXML
  private Button btnNextMonth;
  @FXML
  private Button btnNewReservation;

  @FXML
  private ToggleButton btnFullMonth;
  @FXML
  private ToggleButton btnFirstFortnight;
  @FXML
  private ToggleButton btnSecondFortnight;
  @FXML
  private ToggleButton btnLastFirstFortnight;

  @FXML
  private Label lblMonth;

  @FXML
  private VBox roomColumnContainer;
  @FXML
  private Label fixedRoomHeader;
  @FXML
  private ScrollPane roomScrollPane;
  @FXML
  private VBox roomColumn;

  @FXML
  private HBox fixedDayHeader;
  @FXML
  private ScrollPane headerScrollPane;
  @FXML
  private ScrollPane scrollPane;
  @FXML
  private VBox gridContainer;

  private int currentYear = LocalDate.now().getYear();
  private int currentMonth = LocalDate.now().getMonthValue();

  private enum ViewMode {
    FULL_MONTH,
    FIRST_FORTNIGHT,
    SECOND_FORTNIGHT,
    LAST_FIRST_FORTNIGHT
  }

  private ViewMode currentViewMode = ViewMode.FULL_MONTH;

  private DashboardController dashboardController;

  private final RoomDAO roomDAO = new RoomDAO();
  private final ReservationRepo reservationRepo = new ReservationRepo();
  private final ReservationRoomRepo reservationRoomRepo = new ReservationRoomRepo();

  private Reservation selectedReservation;
  private VBox selectedBookingBlock;

  @FXML
  public void initialize() {

    // =====================================================
    // MES ANTERIOR
    // =====================================================

    btnPreviousMonth.setOnAction(event -> {

      currentMonth--;

      if (currentMonth < 1) {
        currentMonth = 12;
        currentYear--;
      }

      updateBookingChart();
    });

    // =====================================================
    // MES SIGUIENTE
    // =====================================================

    btnNextMonth.setOnAction(event -> {

      currentMonth++;

      if (currentMonth > 12) {
        currentMonth = 1;
        currentYear++;
      }

      updateBookingChart();
    });

    // =====================================================
    // GRUPO DE VISTAS
    // =====================================================

    ToggleGroup viewGroup = new ToggleGroup();

    btnFullMonth.setToggleGroup(viewGroup);
    btnFirstFortnight.setToggleGroup(viewGroup);
    btnSecondFortnight.setToggleGroup(viewGroup);
    btnLastFirstFortnight.setToggleGroup(viewGroup);

    btnFullMonth.setSelected(true);

    // =====================================================
    // MES COMPLETO
    // =====================================================

    btnFullMonth.setOnAction(event -> {

      currentViewMode = ViewMode.FULL_MONTH;

      scrollPane.setHvalue(0);
      scrollPane.setVvalue(0);

      createBookingGrid();
    });

    // =====================================================
    // PRIMERA QUINCENA
    // =====================================================

    btnFirstFortnight.setOnAction(event -> {

      currentViewMode = ViewMode.FIRST_FORTNIGHT;

      scrollPane.setHvalue(0);
      scrollPane.setVvalue(0);

      createBookingGrid();
    });

    // =====================================================
    // SEGUNDA QUINCENA
    // =====================================================

    btnSecondFortnight.setOnAction(event -> {

      currentViewMode = ViewMode.SECOND_FORTNIGHT;

      scrollPane.setHvalue(0);
      scrollPane.setVvalue(0);

      createBookingGrid();
    });

    // =====================================================
    // ÚLTIMA + 1° QUINCENA
    // =====================================================

    btnLastFirstFortnight.setOnAction(event -> {

      currentViewMode = ViewMode.LAST_FIRST_FORTNIGHT;

      scrollPane.setHvalue(0);
      scrollPane.setVvalue(0);

      createBookingGrid();
    });

    // =====================================================
    // NUEVA RESERVA
    // =====================================================

    btnNewReservation.setOnAction(event -> openNewReservation());

    // =====================================================
    // SCROLL DE HABITACIONES
    // =====================================================

    roomScrollPane.vvalueProperty().bind(
        scrollPane.vvalueProperty());

    headerScrollPane.hvalueProperty().bind(
        scrollPane.hvalueProperty());
    // =====================================================
    // SCROLL INICIAL
    // =====================================================

    scrollPane.setVvalue(0);
    scrollPane.setHvalue(0);

    Platform.runLater(this::createBookingGrid);

    // ... código anterior de initialize() ...

    scrollPane.setVvalue(0);
    scrollPane.setHvalue(0);

    // AQUÍ ESTÁ LA MAGIA: Escuchar cuando el ScrollPane obtenga su ancho real
    scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null && newVal.getWidth() > 0) {
        // Si el ancho cambió (o es la primera vez que se calcula), redibujamos
        if (oldVal == null || oldVal.getWidth() != newVal.getWidth()) {
          Platform.runLater(this::createBookingGrid);
        }
      }
    });

    Platform.runLater(this::createBookingGrid);
  }

  // =========================================================
  // NUEVA RESERVA
  // =========================================================

  private void openNewReservation() {

    if (dashboardController != null) {
      dashboardController.loadNewReservation();
    }
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================

  private void updateBookingChart() {
    createBookingGrid();
  }

  // =========================================================
  // DASHBOARD
  // =========================================================

  public void setDashboardController(
      DashboardController dashboardController) {

    this.dashboardController = dashboardController;
  }

  // =========================================================
  // CREAR PLANILLA
  // =========================================================

  private void createBookingGrid() {

    fixedDayHeader.getChildren().clear();
    roomColumn.getChildren().clear();
    gridContainer.getChildren().clear();

    YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);

    // =====================================================
    // FECHAS VISIBLES
    // =====================================================

    List<LocalDate> visibleDates = new ArrayList<>();

    switch (currentViewMode) {

      case FIRST_FORTNIGHT -> {

        for (int day = 1; day <= 15; day++) {
          visibleDates.add(
              yearMonth.atDay(day));
        }
      }

      case SECOND_FORTNIGHT -> {

        for (int day = 16; day <= yearMonth.lengthOfMonth(); day++) {

          visibleDates.add(
              yearMonth.atDay(day));
        }
      }

      case LAST_FIRST_FORTNIGHT -> {

        // Última quincena del mes actual

        for (int day = 16; day <= yearMonth.lengthOfMonth(); day++) {

          visibleDates.add(
              yearMonth.atDay(day));
        }

        // Primera quincena del mes siguiente

        YearMonth nextMonth = yearMonth.plusMonths(1);

        for (int day = 1; day <= 15; day++) {

          visibleDates.add(
              nextMonth.atDay(day));
        }
      }

      default -> {

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {

          visibleDates.add(
              yearMonth.atDay(day));
        }
      }
    }

    // =====================================================
    // TÍTULO
    // =====================================================

    if (currentViewMode == ViewMode.LAST_FIRST_FORTNIGHT) {

      YearMonth nextMonth = yearMonth.plusMonths(1);

      lblMonth.setText(
          formatMonth(yearMonth)
              + " + "
              + formatMonth(nextMonth));

    } else {

      lblMonth.setText(
          formatMonth(yearMonth));
    }

    // =====================================================
    // DIMENSIONES
    // =====================================================

    int numberOfDays = visibleDates.size();

    double roomColumnWidth = 55;

    double availableWidth = scrollPane.getViewportBounds()
        .getWidth();

    if (availableWidth <= 0) {
      availableWidth = 1000;
    }

    double dayWidth = availableWidth / numberOfDays;

    double gridWidth = dayWidth * numberOfDays;

    double viewportWidth = scrollPane.getViewportBounds()
        .getWidth();

    if (viewportWidth > 0) {

      gridWidth = Math.max(
          gridWidth,
          viewportWidth);
    }

    // =====================================================
    // HEADER FIJO DE HABITACIONES
    // =====================================================

    fixedRoomHeader.setPrefWidth(
        roomColumnWidth);

    fixedRoomHeader.setMinWidth(
        roomColumnWidth);

    fixedRoomHeader.setMaxWidth(
        roomColumnWidth);

    fixedRoomHeader.setPrefHeight(45);
    fixedRoomHeader.setMinHeight(45);
    fixedRoomHeader.setMaxHeight(45);

    // =====================================================
    // HEADER DE DÍAS
    // =====================================================

    HBox headerRow = fixedDayHeader;

    headerRow.setPrefWidth(gridWidth);
    headerRow.setMinWidth(gridWidth);
    headerRow.setMaxWidth(gridWidth);

    for (LocalDate date : visibleDates) {

      String dayOfWeek = switch (date.getDayOfWeek()) {

        case MONDAY -> "MON";
        case TUESDAY -> "TUE";
        case WEDNESDAY -> "WED";
        case THURSDAY -> "THU";
        case FRIDAY -> "FRI";
        case SATURDAY -> "SAT";
        case SUNDAY -> "SUN";
      };

      VBox dayBox = new VBox();

      dayBox.setPrefWidth(dayWidth);
      dayBox.setMinWidth(dayWidth);
      dayBox.setMaxWidth(dayWidth);

      dayBox.setPrefHeight(45);
      dayBox.setMinHeight(45);
      dayBox.setMaxHeight(45);

      dayBox.setAlignment(
          javafx.geometry.Pos.CENTER);

      dayBox.getStyleClass()
          .add("day-header");

      if (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
          ||
          date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {

        dayBox.getStyleClass()
            .add("day-header-weekend");
      }

      Label dayName = new Label(dayOfWeek);

      dayName.setStyle(
          "-fx-font-size: 9px;");

      Label dayNumber = new Label(
          String.valueOf(
              date.getDayOfMonth()));

      dayNumber.setStyle(
          "-fx-font-size: 13px;"
              + "-fx-font-weight: bold;");

      dayBox.getChildren().addAll(
          dayName,
          dayNumber);

      headerRow.getChildren()
          .add(dayBox);
    }

    // =====================================================
    // CARGA DE DATOS
    // =====================================================

    List<Room> rooms = roomDAO.listActive();

    List<Reservation> reservations = reservationRepo.getReservations();

    List<ReservationRoom> reservationRooms = reservationRoomRepo.getAll();

    // =====================================================
    // RESERVA -> HABITACIONES
    // =====================================================

    Map<Integer, Set<Integer>> roomsByReservation = new HashMap<>();

    for (ReservationRoom rr : reservationRooms) {

      roomsByReservation
          .computeIfAbsent(
              rr.getIdReservation(),
              k -> new HashSet<>())
          .add(
              rr.getIdRoom());
    }

    // =====================================================
    // HABITACIÓN -> RESERVAS
    // =====================================================

    Map<Integer, List<Reservation>> reservationsByRoom = new HashMap<>();

    for (Reservation reservation : reservations) {

      // No mostrar canceladas

      if (reservation
          .getIdReservationStatus() == 3) {
        continue;
      }

      Set<Integer> roomIds = roomsByReservation.get(
          reservation
              .getIdReservation());

      if (roomIds == null) {
        continue;
      }

      for (Integer roomId : roomIds) {

        reservationsByRoom
            .computeIfAbsent(
                roomId,
                k -> new ArrayList<>())
            .add(reservation);
      }
    }

    // =====================================================
    // FILAS DE HABITACIONES
    // =====================================================

    for (Room room : rooms) {

      // -------------------------------------------------
      // COLUMNA FIJA DE HABITACIONES
      // -------------------------------------------------

      Label roomLabel = new Label(
          String.valueOf(
              room.getNumber()));

      roomLabel.setPrefWidth(
          roomColumnWidth);

      roomLabel.setMinWidth(
          roomColumnWidth);

      roomLabel.setMaxWidth(
          roomColumnWidth);

      roomLabel.setPrefHeight(34);
      roomLabel.setMinHeight(34);
      roomLabel.setMaxHeight(34);

      roomLabel.getStyleClass()
          .add("room-header");

      roomColumn.getChildren()
          .add(roomLabel);

      // -------------------------------------------------
      // FILA DEL CALENDARIO
      // -------------------------------------------------

      HBox roomRow = new HBox();

      roomRow.setUserData(
          room.getNumber());

      roomRow.setPrefWidth(gridWidth);
      roomRow.setMinWidth(gridWidth);
      roomRow.setMaxWidth(gridWidth);

      roomRow.getStyleClass()
          .add("room-row");

      roomRow.setPrefHeight(34);
      roomRow.setMinHeight(34);
      roomRow.setMaxHeight(34);

      // -------------------------------------------------
      // RESERVAS DE ESTA HABITACIÓN
      // -------------------------------------------------

      List<Reservation> roomReservations = reservationsByRoom.getOrDefault(
          room.getIdRoom(),
          Collections.emptyList());

      // -------------------------------------------------
      // RECORRER DÍAS
      // -------------------------------------------------

      int dateIndex = 0;

      while (dateIndex < visibleDates.size()) {

        LocalDate currentDate = visibleDates.get(
            dateIndex);

        Reservation currentReservation = null;

        // ---------------------------------------------
        // BUSCAR RESERVA + SEGMENTO
        // ---------------------------------------------

        currentReservation = null;
        LocalDate segFrom = null;
        LocalDate segTo = null;

        for (Reservation reservation : roomReservations) {

          // Calcular los tramos efectivos de esta habitación dentro del rango
          List<LocalDate[]> segments = computeSegments(
              reservation, room.getIdRoom(), reservationsByRoom);

          for (LocalDate[] seg : segments) {
            LocalDate from = seg[0];
            LocalDate to = seg[1];

            boolean occupied = !currentDate.isBefore(from) && !currentDate.isAfter(to);

            if (occupied) {
              currentReservation = reservation;
              segFrom = from;
              segTo = to;
              break;
            }
          }

          if (currentReservation != null)
            break;
        }

        // =================================================
        // RESERVA
        // =================================================

        if (currentReservation != null) {

          int reservationDays = 0;

          for (int i = dateIndex; i < visibleDates.size(); i++) {

            LocalDate date = visibleDates.get(i);

            boolean occupied = !date.isBefore(segFrom) && !date.isAfter(segTo); // 👈 usa el segmento

            if (!occupied)
              break;

            reservationDays++;
          }

          if (reservationDays <= 0) {
            reservationDays = 1;
          }

          double reservationWidth = dayWidth * reservationDays;

          // =================================================
          // CONTENEDOR QUE REPRESENTA TODA LA RESERVA
          // =================================================

          HBox reservationContainer = new HBox();

          reservationContainer.setPrefWidth(
              reservationWidth);

          reservationContainer.setMinWidth(
              reservationWidth);

          reservationContainer.setMaxWidth(
              reservationWidth);

          reservationContainer.setPrefHeight(34);
          reservationContainer.setMinHeight(34);
          reservationContainer.setMaxHeight(34);

          reservationContainer.setAlignment(
              javafx.geometry.Pos.CENTER);

          reservationContainer.setPadding(
              javafx.geometry.Insets.EMPTY);

          reservationContainer.setSpacing(0);

          reservationContainer.getStyleClass()
              .add("reservation-container");

          // =================================================
          // BLOQUE COLOREADO
          // =================================================

          VBox bookingBlock = new VBox();

          /*
           * Dejamos espacio a los costados para que
           * se vea la línea exterior de la reserva.
           */
          double sideMargin = 4;

          double blockWidth = Math.max(
              reservationWidth - (sideMargin * 2),
              1);

          bookingBlock.setPrefWidth(
              blockWidth);

          bookingBlock.setMinWidth(
              blockWidth);

          bookingBlock.setMaxWidth(
              blockWidth);

          bookingBlock.setPrefHeight(22);
          bookingBlock.setMinHeight(22);
          bookingBlock.setMaxHeight(22);

          bookingBlock.setPadding(
              javafx.geometry.Insets.EMPTY);

          bookingBlock.setSpacing(0);

          bookingBlock.setAlignment(
              javafx.geometry.Pos.CENTER);

          bookingBlock.getStyleClass()
              .add("booking-block");

          // =================================================
          // RESERVA SELECCIONABLE
          // =================================================

          Reservation reservationToSelect = currentReservation;

          // =================================================
          // TEXTO DEL ESTADO
          // =================================================

          String statusText = switch (reservationToSelect
              .getIdReservationStatus()) {

            case 1 -> "Pendiente";
            case 2 -> "Confirmada";
            case 3 -> "Cancelada";
            case 4 -> "Finalizada";
            default -> "Desconocido";
          };

          // =================================================
          // TOOLTIP
          // =================================================

          String tooltipText = "Reserva #"
              + reservationToSelect
                  .getIdReservation()
              + "\n"
              + "Habitación: "
              + room.getNumber()
              + "\n"
              + "Check-in: "
              + reservationToSelect
                  .getCheckIn()
              + "\n"
              + "Check-out: "
              + reservationToSelect
                  .getCheckOut()
              + "\n"
              + "Huéspedes: "
              + reservationToSelect
                  .getNumberOfGuests()
              + "\n"
              + "Tarifa: $"
              + reservationToSelect
                  .getTotalRate()
              + "\n"
              + "Estado: "
              + statusText;

          Tooltip tooltip = new Tooltip(tooltipText);

          tooltip.setShowDelay(
              javafx.util.Duration.millis(350));

          Tooltip.install(
              bookingBlock,
              tooltip);

          // =================================================
          // COLOR SEGÚN ESTADO
          // =================================================

          switch (reservationToSelect
              .getIdReservationStatus()) {

            case 1 ->
              bookingBlock
                  .getStyleClass()
                  .add("pending-block");

            case 2 ->
              bookingBlock
                  .getStyleClass()
                  .add("confirmed-block");

            case 4 ->
              bookingBlock
                  .getStyleClass()
                  .add("finished-block");
          }

          // =================================================
          // DETECTAR HOTEL TOUR (reserva con múltiples habitaciones)
          // =================================================

          Set<Integer> reservationRoomIds = roomsByReservation.get(
              reservationToSelect.getIdReservation());

          boolean isHotelTour = reservationRoomIds != null
              && reservationRoomIds.size() > 1;

          if (isHotelTour) {
            bookingBlock
                .getStyleClass()
                .add("hotel-tour-block");
          }

          // =================================================
          // CLICK PARA SELECCIONAR
          // =================================================

          bookingBlock.setOnMouseClicked(
              event -> {

                /*
                 * Si había otra reserva seleccionada,
                 * quitamos solamente su selección.
                 */
                if (selectedBookingBlock != null) {

                  selectedBookingBlock
                      .getStyleClass()
                      .remove(
                          "selected-block");
                }

                /*
                 * Si hacemos click nuevamente
                 * sobre la misma reserva,
                 * la deseleccionamos.
                 */
                if (selectedReservation == reservationToSelect) {

                  selectedReservation = null;

                  selectedBookingBlock = null;

                  return;
                }

                /*
                 * Nueva selección.
                 */
                selectedReservation = reservationToSelect;

                selectedBookingBlock = bookingBlock;

                bookingBlock
                    .getStyleClass()
                    .add(
                        "selected-block");
              });

          // =================================================
          // AGREGAR BLOQUE AL CONTENEDOR
          // =================================================

          reservationContainer
              .getChildren()
              .add(
                  bookingBlock);

          // =================================================
          // AGREGAR RESERVA A LA FILA
          // =================================================

          roomRow
              .getChildren()
              .add(
                  reservationContainer);

          // =================================================
          // AVANZAR TODOS LOS DÍAS DE LA RESERVA
          // =================================================

          dateIndex += reservationDays;
        }

        // ====================
        // DÍA LIBRE
        // =================================================

        else {

          VBox emptyCell = new VBox();

          emptyCell.setPrefWidth(
              dayWidth);

          emptyCell.setMinWidth(
              dayWidth);

          emptyCell.setMaxWidth(
              dayWidth);

          emptyCell.setPrefHeight(34);
          emptyCell.setMinHeight(34);
          emptyCell.setMaxHeight(34);

          emptyCell.getStyleClass()
              .add("booking-cell");

          roomRow.getChildren()
              .add(emptyCell);

          dateIndex++;
        }

      }

      gridContainer.getChildren()
          .add(roomRow);
    }
  }

  // =========================================================
  // FORMATEAR MES
  // =========================================================

  private String formatMonth(
      YearMonth yearMonth) {

    String month = switch (yearMonth.getMonth()) {

      case JANUARY -> "ENERO";
      case FEBRUARY -> "FEBRERO";
      case MARCH -> "MARZO";
      case APRIL -> "ABRIL";
      case MAY -> "MAYO";
      case JUNE -> "JUNIO";
      case JULY -> "JULIO";
      case AUGUST -> "AGOSTO";
      case SEPTEMBER -> "SEPTIEMBRE";
      case OCTOBER -> "OCTUBRE";
      case NOVEMBER -> "NOVIEMBRE";
      case DECEMBER -> "DICIEMBRE";
    };

    return month + " " + yearMonth.getYear();
  }

  /**
   * Calcula los tramos libres (segmentos) de una habitación dentro del rango
   * de una reserva, restando la ocupación de OTRAS reservas sobre esa misma
   * habitación.
   *
   * Si la reserva ocupa solo esa habitación en todo el rango, devuelve 1
   * segmento = el rango completo (comportamiento normal).
   * Si la reserva es un Hotel Tour (varias habitaciones), devuelve el tramo
   * efectivo que ocupa cada una.
   */
  private List<LocalDate[]> computeSegments(
      Reservation tourRes,
      int roomId,
      Map<Integer, List<Reservation>> reservationsByRoom) {

    List<LocalDate[]> segments = new ArrayList<>();
    LocalDate start = tourRes.getCheckIn();
    LocalDate end = tourRes.getCheckOut();

    // Reunir bloqueos (otras reservas de la misma habitación que se solapan)
    List<Reservation> blockers = new ArrayList<>();
    for (Reservation r : reservationsByRoom.getOrDefault(roomId, Collections.emptyList())) {
      if (r.getIdReservation() == tourRes.getIdReservation())
        continue;
      if (r.getCheckIn().isBefore(end) && r.getCheckOut().isAfter(start)) {
        blockers.add(r);
      }
    }
    blockers.sort(Comparator.comparing(Reservation::getCheckIn));

    // Restar bloqueos del rango
    LocalDate cursor = start;
    for (Reservation b : blockers) {
      if (!b.getCheckIn().isBefore(end))
        break;

      if (cursor.isBefore(b.getCheckIn())) {
        LocalDate gapEnd = b.getCheckIn().isBefore(end) ? b.getCheckIn() : end;
        if (cursor.isBefore(gapEnd)) {
          segments.add(new LocalDate[] { cursor, gapEnd });
        }
      }
      if (b.getCheckOut().isAfter(cursor))
        cursor = b.getCheckOut();
      if (!cursor.isBefore(end))
        break;
    }
    if (cursor.isBefore(end)) {
      segments.add(new LocalDate[] { cursor, end });
    }

    return segments;
  }
}
