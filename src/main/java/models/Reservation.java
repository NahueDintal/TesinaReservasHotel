package models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {

        private int idReservation;
        private int idCustomer;
        private LocalDateTime creationDate;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private int idReservationStatus;
        private int idReservationType;
        private int numberOfGuests;
        private BigDecimal totalRate;
        private String observations;

        public Reservation() {
        }

        public Reservation(int idCustomer,
                           LocalDateTime creationDate,
                           LocalDate checkIn,
                           LocalDate checkOut,
                           int idReservationStatus,
                           int idReservationType,
                           int numberOfGuests,
                           BigDecimal totalRate,
                           String observations) {

            this.idCustomer = idCustomer;
            this.creationDate = creationDate;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.idReservationStatus = idReservationStatus;
            this.idReservationType = idReservationType;
            this.numberOfGuests = numberOfGuests;
            this.totalRate = totalRate;
            this.observations = observations;
        }

        public int getIdReservation() {
            return idReservation;
        }

        public void setIdReservation(int idReservation) {
            this.idReservation = idReservation;
        }

        public int getIdCustomer() {
            return idCustomer;
        }

        public void setIdCustomer(int idCustomer) {
            this.idCustomer = idCustomer;
        }

        public LocalDateTime getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
        }

        public LocalDate getCheckIn() {
            return checkIn;
        }

        public void setCheckIn(LocalDate checkIn) {
            this.checkIn = checkIn;
        }

        public LocalDate getCheckOut() {
            return checkOut;
        }

        public void setCheckOut(LocalDate checkOut) {
            this.checkOut = checkOut;
        }

        public int getIdReservationStatus() {
            return idReservationStatus;
        }

        public void setIdReservationStatus(int idReservationStatus) {
            this.idReservationStatus = idReservationStatus;
        }

        public int getIdReservationType() {
        return idReservationType;
    }

        public void setIdReservationType(int idReservationType) {
        this.idReservationType = idReservationType;
    }

        public int getNumberOfGuests() {
            return numberOfGuests;
        }

        public void setNumberOfGuests(int numberOfGuests) {
            this.numberOfGuests = numberOfGuests;
        }

        public BigDecimal getTotalRate() {
            return totalRate;
        }

        public void setTotalRate(BigDecimal totalRate) {
            this.totalRate = totalRate;
        }

        public String getObservations() {
            return observations;
        }

        public void setObservations(String observations) {
            this.observations = observations;
        }
    }

