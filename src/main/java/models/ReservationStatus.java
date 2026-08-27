package models;

public class ReservationStatus {

    private int idReservationStatus;
    private String name;

    public ReservationStatus() {
    }

    public ReservationStatus(int idReservationStatus, String name) {
        this.idReservationStatus = idReservationStatus;
        this.name = name;
    }

    public int getIdReservationStatus() {
        return idReservationStatus;
    }

    public void setIdReservationStatus(int idReservationStatus) {
        this.idReservationStatus = idReservationStatus;
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