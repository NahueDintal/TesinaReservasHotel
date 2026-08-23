package models;

public class PaymentStatus {

    private int idPaymentStatus;
    private String name;

    public PaymentStatus() {
    }

    public PaymentStatus(String name) {
        this.name = name;
    }

    public int getIdPaymentStatus() {
        return idPaymentStatus;
    }

    public void setIdPaymentStatus(int idPaymentStatus) {
        this.idPaymentStatus = idPaymentStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
