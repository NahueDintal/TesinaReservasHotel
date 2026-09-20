package models;

public class ReservationRoom {

    private int idReservationRoom;
    private int idReservation;
    private int roomNumber;

    public ReservationRoom() {
    }

    public ReservationRoom(int idReservation, int roomNumber) {
        this.idReservation = idReservation;
        this.roomNumber = roomNumber;
    }

    public int getIdReservationRoom() {return idReservationRoom;}
    public void setIdReservationRoom(int idReservationRoom) {this.idReservationRoom = idReservationRoom;}
    public int getIdReservation() {return idReservation;}
    public void setIdReservation(int idReservation) {this.idReservation = idReservation;}
    public int getRoomNumber() {return roomNumber;}
    public void setRoomNumber(int roomNumber) {this.roomNumber = roomNumber;}
}
