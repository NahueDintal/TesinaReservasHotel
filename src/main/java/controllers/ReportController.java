package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ReportController {

  @FXML
  private VBox leftMenu;
  @FXML
  private AnchorPane centerPane;

  @FXML
  private ToggleButton btnCancellationProbability;
  @FXML
  private ToggleButton btnCancellationByLeadTime;
  @FXML
  private ToggleButton btnCancellationByMonth;
  @FXML
  private ToggleButton btnCancellationImpact;
  @FXML
  private ToggleButton btnCancellationByGuests;
  @FXML
  private ToggleButton btnCancellationByCustomerOrigin;
  @FXML
  private ToggleButton btnDailySchedule;

  private ToggleGroup reportGroup;

  private static final String VIEW_PROBABILITY = "/views/CancellationProbability.fxml";
  private static final String VIEW_LEAD_TIME = "/views/CancellationByLeadTime.fxml";
  private static final String VIEW_BY_MONTH = "/views/CancellationByMonth.fxml";
  private static final String VIEW_IMPACT = "/views/CancellationImpact.fxml";
  private static final String VIEW_BY_GUESTS = "/views/CancellationByGuests.fxml";
  private static final String VIEW_BY_ORIGIN = "/views/CancellationByCustomerOrigin.fxml";
  private static final String VIEW_DAILY_SCHEDULE = "/views/DailySchedule.fxml";

  private DashboardController dashboardController;

  public void setDashboardController(DashboardController dashboardController) {
    this.dashboardController = dashboardController;
  }

  @FXML
  public void initialize() {
    reportGroup = new ToggleGroup();

    btnCancellationProbability.setToggleGroup(reportGroup);
    btnCancellationByLeadTime.setToggleGroup(reportGroup);
    btnCancellationByMonth.setToggleGroup(reportGroup);
    btnCancellationImpact.setToggleGroup(reportGroup);
    btnCancellationByGuests.setToggleGroup(reportGroup);
    btnCancellationByCustomerOrigin.setToggleGroup(reportGroup);

    btnCancellationProbability.setOnAction(e -> {
      selectButton(btnCancellationProbability);
      loadReport(VIEW_PROBABILITY);
    });
    btnCancellationByLeadTime.setOnAction(e -> {
      selectButton(btnCancellationByLeadTime);
      loadReport(VIEW_LEAD_TIME);
    });
    btnCancellationByMonth.setOnAction(e -> {
      selectButton(btnCancellationByMonth);
      loadReport(VIEW_BY_MONTH);
    });
    btnCancellationImpact.setOnAction(e -> {
      selectButton(btnCancellationImpact);
      loadReport(VIEW_IMPACT);
    });
    btnCancellationByGuests.setOnAction(e -> {
      selectButton(btnCancellationByGuests);
      loadReport(VIEW_BY_GUESTS);
    });
    btnCancellationByCustomerOrigin.setOnAction(e -> {
      selectButton(btnCancellationByCustomerOrigin);
      loadReport(VIEW_BY_ORIGIN);
    });
    btnDailySchedule.setToggleGroup(reportGroup);
    btnDailySchedule.setOnAction(e -> {
      selectButton(btnDailySchedule);
      loadReport(VIEW_DAILY_SCHEDULE);
    });

    // Default report
    btnCancellationProbability.setSelected(true);
    loadReport(VIEW_PROBABILITY);
  }

  private void selectButton(ToggleButton button) {
    button.setSelected(true);
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
