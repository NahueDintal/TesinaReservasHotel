package controllers;

import models.Reservation;
import repositories.ReservationRepo;

import java.util.List;

public class ReservationController {

    private ReservationRepo reservationRepo;

    public ReservationController() {
        reservationRepo = new ReservationRepo();
    }

    public int createReservation(Reservation reservation) {
        return reservationRepo.createReservation(reservation);
    }

    public List<Reservation> getReservations() {
        return reservationRepo.getReservations();
    }

    public Reservation getReservationById(int idReservation) {
        return reservationRepo.getReservationById(idReservation);
    }

    public boolean updateReservation(Reservation reservation) {
        return reservationRepo.updateReservation(reservation);
    }

    public boolean updateReservationStatus(
            int idReservation,
            int idReservationStatus) {

        return reservationRepo.updateReservationStatus(
                idReservation,
                idReservationStatus
        );
    }
}
