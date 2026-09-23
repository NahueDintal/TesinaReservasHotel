package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Room;
import repositories.RoomDAO;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class BookingChartController {

    @FXML private Button btnPreviousMonth;
    @FXML private Button btnNextMonth;
    @FXML private Label lblMonth;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox gridContainer;

    private int currentYear = LocalDate.now().getYear();
    private int currentMonth = LocalDate.now().getMonthValue();

    private final RoomDAO roomDAO = new RoomDAO();

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

        createBookingGrid();
    }

    private void updateBookingChart() {
        createBookingGrid();
    }

    private void createBookingGrid() {

        gridContainer.getChildren().clear();

        YearMonth yearMonth =
                YearMonth.of(currentYear, currentMonth);

        int daysInMonth = yearMonth.lengthOfMonth();

        lblMonth.setText(
                yearMonth.getMonth().toString() + " " + currentYear
        );

        HBox headerRow = new HBox();

        Label roomHeader = new Label("ROOM");

        roomHeader.setPrefWidth(75);
        roomHeader.setPrefHeight(45);
        roomHeader.getStyleClass().add("grid-header");

        headerRow.getChildren().add(roomHeader);

        for (int day = 1; day <= daysInMonth; day++) {

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

            dayBox.setPrefWidth(35);
            dayBox.setPrefHeight(45);
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

        for (Room room : rooms) {

            HBox roomRow = new HBox();

            Label roomLabel =
                    new Label(String.valueOf(room.getNumber()));

            roomLabel.setPrefWidth(75);
            roomLabel.setPrefHeight(45);
            roomLabel.getStyleClass().add("room-label");

            roomRow.getChildren().add(roomLabel);

            for (int day = 1; day <= daysInMonth; day++) {

                Label cell = new Label();

                cell.setPrefWidth(35);
                cell.setPrefHeight(45);
                cell.getStyleClass().add("booking-cell");

                roomRow.getChildren().add(cell);
            }

            gridContainer.getChildren().add(roomRow);
        }
    }
}