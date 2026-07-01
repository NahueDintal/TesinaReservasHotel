import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardController {

  private List<Reservation> reservations; // Simulación de datos
  private YearMonth currentMonth;

  public DashboardController() {
    // Cargar datos de ejemplo
    loadSampleData();
    currentMonth = YearMonth.now();
  }

  private void loadSampleData() {
    reservations = new ArrayList<>();
    // Crear clientes, habitaciones y reservas de ejemplo
    Client c1 = new Client(1, "Juan", "Pérez", "12345678A", 600123456);
    Client c2 = new Client(2, "María", "García", "87654321B", 600654321);
    Room r1 = new Room(101, "Doble", 80.0);
    Room r2 = new Room(102, "Individual", 50.0);
    Room r3 = new Room(201, "Suite", 150.0);

    LocalDate today = LocalDate.now();
    // Reserva 1: del 5 al 10 del mes actual
    reservations.add(new Reservation(c1, r1, today.withDayOfMonth(5), today.withDayOfMonth(10)));
    // Reserva 2: del 12 al 15
    reservations.add(new Reservation(c2, r2, today.withDayOfMonth(12), today.withDayOfMonth(15)));
    // Reserva 3: del 20 al 25
    reservations.add(new Reservation(c1, r3, today.withDayOfMonth(20), today.withDayOfMonth(25)));
  }

  /**
   * Panel superior con estadísticas
   */
  public HBox getStatsPanel() {
    HBox stats = new HBox(20);
    stats.setPadding(new Insets(15));
    stats.setStyle("-fx-background-color: #2c3e50;");

    // Calcular estadísticas
    int totalReservas = reservations.size();
    long ocupadas = reservations.stream()
        .filter(r -> !r.getInitDate().isAfter(LocalDate.now()) && !r.getEndDate().isBefore(LocalDate.now()))
        .count();
    double ingresos = reservations.stream().mapToDouble(Reservation::getTotal).sum();

    stats.getChildren().addAll(
        createStatCard("Total Reservas", String.valueOf(totalReservas)),
        createStatCard("Ocupación Actual", ocupadas + " hab."),
        createStatCard("Ingresos del Mes", String.format("$%.2f", ingresos)));
    return stats;
  }

  private VBox createStatCard(String title, String value) {
    VBox card = new VBox(5);
    card.setPadding(new Insets(15));
    card.setStyle("-fx-background-color: #34495e; -fx-background-radius: 10;");
    card.setPrefWidth(180);

    Label titleLabel = new Label(title);
    titleLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 14px;");
    Label valueLabel = new Label(value);
    valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

    card.getChildren().addAll(titleLabel, valueLabel);
    return card;
  }

  /**
   * Planilla mensual con división en 3 franjas por día
   */
  public GridPane getMonthlyGrid() {
    GridPane grid = new GridPane();
    grid.setPadding(new Insets(15));
    grid.setHgap(5);
    grid.setVgap(5);
    grid.setStyle("-fx-background-color: #ecf0f1;");

    // Días de la semana (cabecera)
    String[] days = { "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom" };
    for (int i = 0; i < 7; i++) {
      Label header = new Label(days[i]);
      header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
      GridPane.setHalignment(header, javafx.geometry.HPos.CENTER);
      grid.add(header, i, 0);
    }

    // Obtener primer día del mes y número de días
    LocalDate firstDay = currentMonth.atDay(1);
    int offset = firstDay.getDayOfWeek().getValue() - 1; // Lunes=1
    int daysInMonth = currentMonth.lengthOfMonth();

    // Celda para cada día
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      int row = (day + offset - 1) / 7 + 1;
      int col = (day + offset - 1) % 7;

      VBox dayCell = createDayCell(date);
      grid.add(dayCell, col, row);
    }

    return grid;
  }

  /**
   * Crea una celda para un día específico, dividida en 3 franjas
   */
  private VBox createDayCell(LocalDate date) {
    VBox cell = new VBox(2);
    cell.setPadding(new Insets(5));
    cell.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1;");
    cell.setPrefWidth(110);
    cell.setPrefHeight(80);

    // Número del día
    Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
    dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
    cell.getChildren().add(dayLabel);

    // Franjas: mañana, tarde, noche
    HBox slots = new HBox(2);
    slots.setPrefHeight(55);

    // Determinar si hay reservas que ocupen cada franja
    boolean morning = isCheckIn(date);
    boolean afternoon = isCheckOut(date);
    boolean night = isOccupied(date); // Ocupación completa

    // Crear 3 rectángulos (o paneles) coloreados
    Region morningSlot = createSlot(morning ? Color.LIGHTGREEN : Color.LIGHTGRAY);
    Region afternoonSlot = createSlot(afternoon ? Color.LIGHTBLUE : Color.LIGHTGRAY);
    Region nightSlot = createSlot(night ? Color.ORANGE : Color.LIGHTGRAY);

    slots.getChildren().addAll(morningSlot, afternoonSlot, nightSlot);
    cell.getChildren().add(slots);

    return cell;
  }

  private Region createSlot(Color color) {
    Region slot = new Region();
    slot.setPrefWidth(30);
    slot.setPrefHeight(50);
    slot.setStyle("-fx-background-color: " + toRgb(color) + "; -fx-background-radius: 3;");
    // Tooltip opcional
    return slot;
  }

  private String toRgb(Color color) {
    return String.format("rgba(%d, %d, %d, 0.8)",
        (int) (color.getRed() * 255),
        (int) (color.getGreen() * 255),
        (int) (color.getBlue() * 255));
  }

  // Lógica de ejemplo para determinar franjas
  private boolean isCheckIn(LocalDate date) {
    return reservations.stream().anyMatch(r -> r.getInitDate().equals(date));
  }

  private boolean isCheckOut(LocalDate date) {
    return reservations.stream().anyMatch(r -> r.getEndDate().equals(date));
  }

  private boolean isOccupied(LocalDate date) {
    return reservations.stream().anyMatch(r -> !date.isBefore(r.getInitDate()) && !date.isAfter(r.getEndDate()));
  }
}
