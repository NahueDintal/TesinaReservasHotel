package models;

public class ReservationType {

    private int idReservationType;
    private String name;

    public ReservationType() {
    }

    public ReservationType(String name) {
        this.name = name;
    }

    public int getIdReservationType() {
        return idReservationType;
    }

    public void setIdReservationType(int idReservationType) {
        this.idReservationType = idReservationType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}