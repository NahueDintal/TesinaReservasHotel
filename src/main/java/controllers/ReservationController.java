package controllers;

import models.Reservation;
import repositories.ReservationRepo;

import java.util.List;

public class ReservationController {

    private ReservationRepo reservationRepo;

    public ReservationController() {
        reservationRepo = new ReservationRepo();
    }

    // =========================================================
    // CREAR RESERVA
    // =========================================================

    public int createReservation(Reservation reservation) {

        return reservationRepo.createReservation(
                reservation
        );
    }

    // =========================================================
    // OBTENER TODAS LAS RESERVAS
    // =========================================================

    public List<Reservation> getReservations() {

        return reservationRepo.getReservations();
    }

    // =========================================================
    // OBTENER RESERVA POR ID
    // =========================================================

    public Reservation getReservationById(
            int idReservation) {

        return reservationRepo.getReservationById(
                idReservation
        );
    }

    // =========================================================
    // ACTUALIZAR RESERVA
    // =========================================================

    public boolean updateReservation(
            Reservation reservation) {

        return reservationRepo.updateReservation(
                reservation
        );
    }

    // =========================================================
    // ACTUALIZAR ESTADO
    // =========================================================

    public boolean updateReservationStatus(
            int idReservation,
            int idReservationStatus) {

        return reservationRepo.updateReservationStatus(
                idReservation,
                idReservationStatus
        );
    }
}


