package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Consumption {

    private int idConsumption;
    private int idReservation;
    private int idConsumptionType;
    private int idProduct;
    private int idService;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;
    private LocalDateTime consumptionDate;
    private int idPaymentStatus;
    private String observations;

    public Consumption () {
    }

    public Consumption (int idReservation,
                        int idConsumptionType,
                        int idProduct,
                        int idService,
                        int quantity,
                        BigDecimal unitPrice,
                        BigDecimal total,
                        LocalDateTime consumptionDate,
                        int idPaymentStatus,
                        String observations) {

        this.idReservation = idReservation;
        this.idConsumptionType = idConsumptionType;
        this.idProduct = idProduct;
        this.idService = idService;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = total;
        this.consumptionDate = consumptionDate;
        this.idPaymentStatus = idPaymentStatus;
        this.observations = observations;
    }

    public int getIdConsumption() {
        return idConsumption;
    }

    public void setIdConsumption(int idConsumption) {
        this.idConsumption = idConsumption;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public int getIdConsumptionType() {
        return idConsumptionType;
    }

    public void setIdConsumptionType(int idConsumptionType) {
        this.idConsumptionType = idConsumptionType;
    }

    public int getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(int idProduct) {
        this.idProduct = idProduct;
    }

    public int getIdService() {
        return idService;
    }

    public void setIdService(int idService) {
        this.idService = idService;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getConsumptionDate() {
        return consumptionDate;
    }

    public void setConsumptionDate(LocalDateTime consumptionDate) {
        this.consumptionDate = consumptionDate;
    }

    public int getIdPaymentStatus() {
        return idPaymentStatus;
    }

    public void setIdPaymentStatus(int idPaymentStatus) {
        this.idPaymentStatus = idPaymentStatus;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
    
}
