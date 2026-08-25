package models;

public class PaymentMethod {

    private int idPaymentMethod;
    private String name;

    public PaymentMethod() {
    }

    public PaymentMethod(String name) {
        this.name = name;
    }

    public int getIdPaymentMethod() {
        return idPaymentMethod;
    }

    public void setIdPaymentMethod(int idPaymentMethod) {
        this.idPaymentMethod = idPaymentMethod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String ToString() {
        return name;

    }
}