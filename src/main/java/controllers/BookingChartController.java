package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import models.Reservation;
import models.ReservationRoom;
import models.Room;
import repositories.ReservationRepo;
import repositories.ReservationRoomRepo;
import repositories.RoomDAO;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class BookingChartController {

    @FXML private Button btnPreviousMonth;
    @FXML private Button btnNextMonth;
    @FXML private ToggleButton btnFullMonth;
    @FXML private ToggleButton btnFirstFortnight;

    @FXML
    private ToggleButton btnSecondFortnight;
    @FXML private Label lblMonth;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox gridContainer;

    private int currentYear = LocalDate.now().getYear();
    private int currentMonth = LocalDate.now().getMonthValue();
    private enum ViewMode {
        FULL_MONTH,
        FIRST_FORTNIGHT,
        SECOND_FORTNIGHT
    }

    private ViewMode currentViewMode = ViewMode.FULL_MONTH;
    private final RoomDAO roomDAO = new RoomDAO();

    private final ReservationRepo reservationRepo = new ReservationRepo();
    private final ReservationRoomRepo reservationRoomRepo = new ReservationRoomRepo();

    @FXML
    public void initialize() {

        btnPreviousMonth.setOnAction(event -> {

            currentMonth--;

            if (currentMonth < 1) {
                currentMonth = 12;
                currentYear--;
            }

            updateBookingChart();
        });

        btnNextMonth.setOnAction(event -> {

            currentMonth++;

            if (currentMonth > 12) {
                currentMonth = 1;
                currentYear++;
            }

            updateBookingChart();
        });

        ToggleGroup viewGroup = new ToggleGroup();

        btnFullMonth.setToggleGroup(viewGroup);
        btnFirstFortnight.setToggleGroup(viewGroup);
        btnSecondFortnight.setToggleGroup(viewGroup);

        btnFullMonth.setSelected(true);

        btnFullMonth.setOnAction(event -> {
            currentViewMode = ViewMode.FULL_MONTH;
            createBookingGrid();
        });

        btnFirstFortnight.setOnAction(event -> {
            currentViewMode = ViewMode.FIRST_FORTNIGHT;
            createBookingGrid();
        });

        btnSecondFortnight.setOnAction(event -> {
            currentViewMode = ViewMode.SECOND_FORTNIGHT;
            createBookingGrid();

            scrollPane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                if (currentViewMode != ViewMode.FULL_MONTH) {
                    createBookingGrid();
                }
            });
        });

        createBookingGrid();
    }

    private void updateBookingChart() {
        createBookingGrid();
    }

    private void createBookingGrid() {

        gridContainer.getChildren().clear();

        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);

        int daysInMonth = yearMonth.lengthOfMonth();

        int startDay;
        int endDay;

        switch (currentViewMode) {

            case FIRST_FORTNIGHT -> {
                startDay = 1;
                endDay = 15;
            }

            case SECOND_FORTNIGHT -> {
                startDay = 16;
                endDay = daysInMonth;
            }

            default -> {
                startDay = 1;
                endDay = daysInMonth;
            }
        }

        int visibleDays = endDay - startDay + 1;

        lblMonth.setText(yearMonth.getMonth().toString() + " " + currentYear
        );

        HBox headerRow = new HBox();

        boolean isFortnight = currentViewMode != ViewMode.FULL_MONTH;

        double availableWidth = scrollPane.getWidth();
        double roomColumnWidth = 55;

        double dayWidth;

        if (isFortnight && availableWidth > 0) {
            int numberOfDays = endDay - startDay + 1;
            dayWidth = (availableWidth - roomColumnWidth) / numberOfDays;
        } else {
            dayWidth = 35;
        }

        Label roomHeader = new Label("ROOM");

        roomHeader.setPrefWidth(55);
        roomHeader.setPrefHeight(45);
        roomHeader.getStyleClass().add("grid-header");

        headerRow.getChildren().add(roomHeader);

        for (int day = startDay; day <= endDay; day++) {

            LocalDate date = yearMonth.atDay(day);

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
            dayBox.setAlignment(javafx.geometry.Pos.CENTER);
            dayBox.getStyleClass().add("grid-header");

            Label dayName = new Label(dayOfWeek);
            dayName.setStyle("-fx-font-size: 9px;");

            Label dayNumber = new Label(String.valueOf(day));
            dayNumber.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

            dayBox.getChildren().addAll(dayName, dayNumber);

            headerRow.getChildren().add(dayBox);
        }

        gridContainer.getChildren().add(headerRow);

        List<Room> rooms = roomDAO.listActive();

        List<Reservation> reservations = reservationRepo.getReservations();
        List<ReservationRoom> reservationRooms = reservationRoomRepo.getAll();

        for (Room room : rooms) {

            HBox roomRow = new HBox();
            roomRow.setMaxWidth(Double.MAX_VALUE);

            Label roomLabel =
                    new Label(String.valueOf(room.getNumber()));

            roomLabel.setPrefWidth(55);
            roomLabel.setPrefHeight(45);
            roomLabel.getStyleClass().add("room-label");

            roomRow.getChildren().add(roomLabel);

            for (int day = startDay; day <= endDay; day++) {

                LocalDate currentDate = yearMonth.atDay(day);

                Label cell = new Label();

                cell.setPrefWidth(dayWidth);
                cell.setMinWidth(dayWidth);
                cell.setMaxWidth(dayWidth);
                cell.setPrefHeight(45);
                cell.getStyleClass().add("booking-cell");

                for (Reservation reservation : reservations) {

                    boolean reservationUsesRoom = reservationRooms.stream()
                            .anyMatch(rr ->
                                    rr.getIdReservation() == reservation.getIdReservation()
                                            && rr.getRoomNumber() == room.getNumber()
                            );

                    if (!reservationUsesRoom) {
                        continue;
                    }

                    boolean occupied =
                            !currentDate.isBefore(reservation.getCheckIn())
                                    && currentDate.isBefore(reservation.getCheckOut());

                    if (occupied) {
                        cell.getStyleClass().add("reserved-cell");
                        break;
                    }
                }

                roomRow.getChildren().add(cell);
            }

            gridContainer.getChildren().add(roomRow);
        }
    }
}