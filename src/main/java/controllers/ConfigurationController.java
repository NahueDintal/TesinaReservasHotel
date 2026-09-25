package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import utils.StyleManager;

import java.io.IOException;

public class ConfigurationController {

    @FXML
    private Button btnProducts;

    @FXML
    private Button btnServices;

    @FXML
    public void initialize() {

        btnProducts.setOnAction(e -> openProducts());

        btnServices.setOnAction(e -> openServices());
    }

    private void openProducts() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/views/Product.fxml"
                            )
                    );

            Stage stage = new Stage();

            stage.setScene(
                    new Scene(loader.load())
            );

            StyleManager.applyStyles(stage);

            stage.setTitle("Productos");

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private void openServices() {

        System.out.println("CLICK EN SERVICIOS");

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/views/Service.fxml"
                            )
                    );

            System.out.println("FXML encontrado, intentando cargar...");

            Stage stage = new Stage();

            stage.setScene(
                    new Scene(loader.load())
            );

            System.out.println("FXML cargado correctamente.");

            StyleManager.applyStyles(stage);

            stage.setTitle("Servicios");

            stage.show();

            System.out.println("Ventana de servicios abierta.");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}


