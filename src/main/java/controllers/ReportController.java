package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ReportController {

  @FXML private AnchorPane centerPane;

  // Tarjetas
  @FXML private VBox tileCancellationProbability;
  @FXML private VBox tileCancellationByLeadTime;
  @FXML private VBox tileCancellationByMonth;
  @FXML private VBox tileCancellationImpact;
  @FXML private VBox tileCancellationByGuests;
  @FXML private VBox tileCancellationByCustomerOrigin;
  @FXML private VBox tileDailySchedule;

  private static final String VIEW_PROBABILITY      = "/views/CancellationProbability.fxml";
  private static final String VIEW_LEAD_TIME        = "/views/CancellationByLeadTime.fxml";
  private static final String VIEW_BY_MONTH         = "/views/CancellationByMonth.fxml";
  private static final String VIEW_IMPACT           = "/views/CancellationImpact.fxml";
  private static final String VIEW_BY_GUESTS        = "/views/CancellationByGuests.fxml";
  private static final String VIEW_BY_ORIGIN        = "/views/CancellationByCustomerOrigin.fxml";
  private static final String VIEW_DAILY_SCHEDULE   = "/views/DailySchedule.fxml";

  private VBox selectedTile;

  @FXML
  public void initialize() {
    // Click handlers de cada tarjeta
    tileCancellationProbability.setOnMouseClicked(e -> selectTile(tileCancellationProbability, VIEW_PROBABILITY));
    tileCancellationByLeadTime.setOnMouseClicked(e -> selectTile(tileCancellationByLeadTime, VIEW_LEAD_TIME));
    tileCancellationByMonth.setOnMouseClicked(e -> selectTile(tileCancellationByMonth, VIEW_BY_MONTH));
    tileCancellationImpact.setOnMouseClicked(e -> selectTile(tileCancellationImpact, VIEW_IMPACT));
    tileCancellationByGuests.setOnMouseClicked(e -> selectTile(tileCancellationByGuests, VIEW_BY_GUESTS));
    tileCancellationByCustomerOrigin.setOnMouseClicked(e -> selectTile(tileCancellationByCustomerOrigin, VIEW_BY_ORIGIN));
    tileDailySchedule.setOnMouseClicked(e -> selectTile(tileDailySchedule, VIEW_DAILY_SCHEDULE));

    // Estado inicial
    selectTile(tileCancellationProbability, VIEW_PROBABILITY);
  }

  private void selectTile(VBox tile, String fxmlPath) {
    // Quitar selección previa
    if (selectedTile != null) {
      selectedTile.getStyleClass().remove("report-tile-selected");
    }
    selectedTile = tile;

    // Aplicar selección
    if (!tile.getStyleClass().contains("report-tile-selected")) {
      tile.getStyleClass().add("report-tile-selected");
    }

    // Cargar reporte
    loadReport(fxmlPath);
  }

  private void loadReport(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent view = loader.load();

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
}