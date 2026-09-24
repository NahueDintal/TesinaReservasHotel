package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Product;
import repositories.ProductRepo;
import utils.StyleManager;

import java.math.BigDecimal;

public class ProductFormController {

    // ========== FORM COMPONENTS ==========

    @FXML private Label lblFormTitle;

    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private TextArea txtDescription;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;


    // ========== REPOSITORY ==========

    private final ProductRepo productRepo = new ProductRepo();


    // ========== EDITING ==========

    private Product editingProduct;


    // ========== INITIALIZATION ==========

    @FXML
    public void initialize() {

        setupButtonActions();
        setupValidations();

        btnSave.setDisable(true);
    }


    // ========== VALIDATIONS ==========

    private void setupValidations() {

        // Nombre
        txtName.focusedProperty().addListener((obs, oldValue, newValue) -> {

            if (!newValue) {
                validateName();
            }

        });

        // Precio
        txtPrice.focusedProperty().addListener((obs, oldValue, newValue) -> {

            if (!newValue) {
                validatePrice();
            }

        });


        // Limitar nombre a 50 caracteres
        txtName.textProperty().addListener((obs, oldValue, newValue) -> {

            if (newValue != null && newValue.length() > 50) {
                txtName.setText(oldValue);
            }

            updateSaveButtonState();
        });


        // Limitar descripción a 255 caracteres
        txtDescription.textProperty().addListener((obs, oldValue, newValue) -> {

            if (newValue != null && newValue.length() > 255) {
                txtDescription.setText(oldValue);
            }

            updateSaveButtonState();
        });


        // Precio
        txtPrice.textProperty().addListener((obs, oldValue, newValue) -> {

            if (newValue != null && newValue.length() > 12) {
                txtPrice.setText(oldValue);
            }

            updateSaveButtonState();
        });
    }


    // ========== VALIDATE NAME ==========

    private boolean validateName() {

        String name = txtName.getText();

        boolean valid =
                name != null &&
                        !name.trim().isEmpty() &&
                        name.trim().length() <= 50;

        setFieldValid(
                txtName,
                valid,
                "El nombre es obligatorio y debe tener hasta 50 caracteres."
        );

        return valid;
    }


    // ========== VALIDATE PRICE ==========

    private boolean validatePrice() {

        String text = txtPrice.getText();

        boolean valid = false;

        try {

            if (text != null && !text.trim().isEmpty()) {

                BigDecimal price =
                        new BigDecimal(text.trim());

                valid =
                        price.compareTo(BigDecimal.ZERO) > 0 &&
                                price.compareTo(
                                        new BigDecimal("999999999.99")
                                ) <= 0;
            }

        } catch (NumberFormatException e) {
            valid = false;
        }

        setFieldValid(
                txtPrice,
                valid,
                "El precio debe ser mayor a 0 y no superar $999.999.999,99."
        );

        return valid;
    }


    // ========== FIELD FEEDBACK ==========

    private void setFieldValid(
            Control field,
            boolean valid,
            String errorMessage
    ) {

        if (valid) {

            field.setStyle("");
            field.setTooltip(null);

        } else {

            field.setStyle(
                    "-fx-border-color: #e74c3c;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 5;"
            );

            Tooltip tooltip =
                    new Tooltip(errorMessage);

            field.setTooltip(tooltip);
        }
    }


    // ========== SAVE BUTTON ==========

    private void updateSaveButtonState() {

        boolean nameValid =
                txtName.getText() != null &&
                        !txtName.getText().trim().isEmpty();

        boolean priceValid =
                isValidPrice(txtPrice.getText());

        btnSave.setDisable(
                !(nameValid && priceValid)
        );
    }


    private boolean isValidPrice(String text) {

        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        try {

            BigDecimal price =
                    new BigDecimal(text.trim());

            return price.compareTo(BigDecimal.ZERO) > 0 &&
                    price.compareTo(
                            new BigDecimal("999999999.99")
                    ) <= 0;

        } catch (NumberFormatException e) {

            return false;
        }
    }


    // ========== BUTTON ACTIONS ==========

    private void setupButtonActions() {

        btnCancel.setOnAction(e -> {

            boolean confirmed =
                    StyleManager.showConfirmation(
                            "Cancelar",
                            "¿Descartar cambios?",
                            "Perderá todo lo que haya modificado o agregado en el formulario."
                    );

            if (confirmed) {
                closeWindow();
            }
        });


        btnSave.setOnAction(e -> saveProduct());
    }


    // ========== SET PRODUCT ==========

    public void setProduct(Product product) {

        this.editingProduct = product;

        lblFormTitle.setText("Modificar Producto");

        btnSave.setText("Actualizar");

        txtName.setText(product.getName());

        txtDescription.setText(
                product.getDescription() != null
                        ? product.getDescription()
                        : ""
        );

        txtPrice.setText(
                product.getPrice() != null
                        ? product.getPrice().toString()
                        : ""
        );

        updateSaveButtonState();
    }


    // ========== SAVE ==========

    private void saveProduct() {

        if (!validateAllFields()) {
            return;
        }

        String name =
                txtName.getText().trim();

        String description =
                txtDescription.getText() != null
                        ? txtDescription.getText().trim()
                        : "";

        BigDecimal price;

        try {

            price =
                    new BigDecimal(
                            txtPrice.getText().trim()
                    );

        } catch (NumberFormatException e) {

            showAlert(
                    "Error",
                    "Precio inválido",
                    "Ingrese un precio numérico válido."
            );

            return;
        }


        // Crear o editar
        Product product =
                editingProduct != null
                        ? editingProduct
                        : new Product();

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);


        // Verificar nombre duplicado
        int excludeId =
                editingProduct != null
                        ? editingProduct.getIdProduct()
                        : 0;

        if (productRepo.existsByName(name, excludeId)) {

            showAlert(
                    "Producto duplicado",
                    "Ya existe un producto con ese nombre.",
                    "Ingrese un nombre diferente."
            );

            return;
        }


        // Insertar / actualizar
        boolean success;

        if (editingProduct != null) {

            success =
                    productRepo.update(product);

        } else {

            success =
                    productRepo.insert(product);
        }


        if (success) {

            showAlert(
                    "Éxito",
                    editingProduct != null
                            ? "Producto actualizado"
                            : "Producto creado",
                    editingProduct != null
                            ? "El producto se actualizó correctamente."
                            : "El producto se creó correctamente."
            );

            closeWindow();

        } else {

            showAlert(
                    "Error",
                    "No se pudo guardar el producto",
                    "Ocurrió un error al guardar los datos."
            );
        }
    }


    // ========== VALIDATE ALL ==========

    private boolean validateAllFields() {

        boolean nameValid =
                validateName();

        boolean priceValid =
                validatePrice();

        return nameValid && priceValid;
    }


    // ========== CLOSE ==========

    private void closeWindow() {

        Stage stage =
                (Stage) btnCancel
                        .getScene()
                        .getWindow();

        stage.close();
    }


    // ========== ALERT ==========

    private void showAlert(
            String title,
            String header,
            String content
    ) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        StyleManager.applyStyles(
                alert.getDialogPane()
        );

        alert.showAndWait();
    }
}
