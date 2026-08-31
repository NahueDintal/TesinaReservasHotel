package repositories;

import models.Reservation;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationRepo {

    // =========================================================
    // CREATE para unico
    // =========================================================

    public int createReservation(Reservation reservation) {

        if (!validateReservation(reservation)) {
            return -1;
        }

        String sql = "INSERT INTO Reservation " +
                "(idCustomer, creationDate, checkIn, checkOut, " +
                "idReservationStatus, idReservationType, " +
                "numberOfGuests, totalRate, observations) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(
                    1,
                    reservation.getIdCustomer()
            );

            stmt.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            reservation.getCreationDate()
                    )
            );

            stmt.setDate(
                    3,
                    Date.valueOf(
                            reservation.getCheckIn()
                    )
            );

            stmt.setDate(
                    4,
                    Date.valueOf(
                            reservation.getCheckOut()
                    )
            );

            stmt.setInt(
                    5,
                    reservation.getIdReservationStatus()
            );

            stmt.setInt(
                    6,
                    reservation.getIdReservationType()
            );

            stmt.setInt(
                    7,
                    reservation.getNumberOfGuests()
            );

            stmt.setBigDecimal(
                    8,
                    reservation.getTotalRate()
            );

            stmt.setString(
                    9,
                    reservation.getObservations()
            );

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                return -1;
            }

            try (ResultSet generatedKeys =
                         stmt.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al crear la reserva: "
                            + e.getMessage()
            );
        }

        return -1;
    }

    //para la transaccion
    public int createReservation(Connection conn, Reservation reservation) {

        if (!validateReservation(reservation)) {
            return -1;
        }

        String sql = "INSERT INTO Reservation " +
                "(idCustomer, creationDate, checkIn, checkOut, " +
                "idReservationStatus, idReservationType, " +
                "numberOfGuests, totalRate, observations) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, reservation.getIdCustomer());

            stmt.setTimestamp(
                    2,
                    Timestamp.valueOf(reservation.getCreationDate())
            );

            stmt.setDate(
                    3,
                    Date.valueOf(reservation.getCheckIn())
            );

            stmt.setDate(
                    4,
                    Date.valueOf(reservation.getCheckOut())
            );

            stmt.setInt(
                    5,
                    reservation.getIdReservationStatus()
            );

            stmt.setInt(
                    6,
                    reservation.getIdReservationType()
            );

            stmt.setInt(
                    7,
                    reservation.getNumberOfGuests()
            );

            stmt.setBigDecimal(
                    8,
                    reservation.getTotalRate()
            );

            stmt.setString(
                    9,
                    reservation.getObservations()
            );

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                return -1;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al crear la reserva dentro de la transacción: "
                            + e.getMessage()
            );
        }

        return -1;
    }

    // =========================================================
    // GET ALL
    // =========================================================

    public List<Reservation> getReservations() {

        List<Reservation> reservations =
                new ArrayList<>();

        String sql =
                "SELECT idReservation, idCustomer, creationDate, " +
                        "checkIn, checkOut, idReservationStatus, " +
                        "idReservationType, numberOfGuests, totalRate, " +
                        "observations " +
                        "FROM Reservation";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Reservation reservation =
                        new Reservation();

                reservation.setIdReservation(
                        rs.getInt("idReservation")
                );

                reservation.setIdCustomer(
                        rs.getInt("idCustomer")
                );

                if (rs.getTimestamp("creationDate") != null) {

                    reservation.setCreationDate(
                            rs.getTimestamp("creationDate")
                                    .toLocalDateTime()
                    );
                }

                if (rs.getDate("checkIn") != null) {

                    reservation.setCheckIn(
                            rs.getDate("checkIn")
                                    .toLocalDate()
                    );
                }

                if (rs.getDate("checkOut") != null) {

                    reservation.setCheckOut(
                            rs.getDate("checkOut")
                                    .toLocalDate()
                    );
                }

                reservation.setIdReservationStatus(
                        rs.getInt("idReservationStatus")
                );

                reservation.setIdReservationType(
                        rs.getInt("idReservationType")
                );

                reservation.setNumberOfGuests(
                        rs.getInt("numberOfGuests")
                );

                reservation.setTotalRate(
                        rs.getBigDecimal("totalRate")
                );

                reservation.setObservations(
                        rs.getString("observations")
                );

                reservations.add(reservation);
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al obtener las reservas: "
                            + e.getMessage()
            );
        }

        return reservations;
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Reservation getReservationById(
            int idReservation) {

        if (idReservation <= 0) {
            System.err.println(
                    "El ID de la reserva no es válido."
            );
            return null;
        }

        String sql =
                "SELECT idReservation, idCustomer, creationDate, " +
                        "checkIn, checkOut, idReservationStatus, " +
                        "idReservationType, numberOfGuests, totalRate, " +
                        "observations " +
                        "FROM Reservation " +
                        "WHERE idReservation = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(
                    1,
                    idReservation
            );

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    Reservation reservation =
                            new Reservation();

                    reservation.setIdReservation(
                            rs.getInt("idReservation")
                    );

                    reservation.setIdCustomer(
                            rs.getInt("idCustomer")
                    );

                    if (rs.getTimestamp("creationDate") != null) {

                        reservation.setCreationDate(
                                rs.getTimestamp("creationDate")
                                        .toLocalDateTime()
                        );
                    }

                    if (rs.getDate("checkIn") != null) {

                        reservation.setCheckIn(
                                rs.getDate("checkIn")
                                        .toLocalDate()
                        );
                    }

                    if (rs.getDate("checkOut") != null) {

                        reservation.setCheckOut(
                                rs.getDate("checkOut")
                                        .toLocalDate()
                        );
                    }

                    reservation.setIdReservationStatus(
                            rs.getInt("idReservationStatus")
                    );

                    reservation.setIdReservationType(
                            rs.getInt("idReservationType")
                    );

                    reservation.setNumberOfGuests(
                            rs.getInt("numberOfGuests")
                    );

                    reservation.setTotalRate(
                            rs.getBigDecimal("totalRate")
                    );

                    reservation.setObservations(
                            rs.getString("observations")
                    );

                    return reservation;
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al obtener la reserva: "
                            + e.getMessage()
            );
        }

        return null;
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public boolean updateReservation(
            Reservation reservation) {

        if (!validateReservation(reservation)) {
            return false;
        }

        if (reservation.getIdReservation() <= 0) {

            System.err.println(
                    "El ID de la reserva no es válido."
            );

            return false;
        }

        String sql =
                "UPDATE Reservation SET " +
                        "idCustomer = ?, " +
                        "checkIn = ?, " +
                        "checkOut = ?, " +
                        "idReservationStatus = ?, " +
                        "idReservationType = ?, " +
                        "numberOfGuests = ?, " +
                        "totalRate = ?, " +
                        "observations = ? " +
                        "WHERE idReservation = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(
                    1,
                    reservation.getIdCustomer()
            );

            stmt.setDate(
                    2,
                    Date.valueOf(
                            reservation.getCheckIn()
                    )
            );

            stmt.setDate(
                    3,
                    Date.valueOf(
                            reservation.getCheckOut()
                    )
            );

            stmt.setInt(
                    4,
                    reservation.getIdReservationStatus()
            );

            stmt.setInt(
                    5,
                    reservation.getIdReservationType()
            );

            stmt.setInt(
                    6,
                    reservation.getNumberOfGuests()
            );

            stmt.setBigDecimal(
                    7,
                    reservation.getTotalRate()
            );

            stmt.setString(
                    8,
                    reservation.getObservations()
            );

            stmt.setInt(
                    9,
                    reservation.getIdReservation()
            );

            int rowsAffected =
                    stmt.executeUpdate();

            if (rowsAffected > 0) {

                System.out.println(
                        "Reserva actualizada correctamente."
                );

                return true;
            }

            System.out.println(
                    "No se encontró la reserva con ID: "
                            + reservation.getIdReservation()
            );

            return false;

        } catch (SQLException e) {

            System.err.println(
                    "Error al actualizar la reserva: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // =========================================================
    // UPDATE para transacción
    // =========================================================

    public boolean updateReservation(
            Connection conn,
            Reservation reservation) {

        if (!validateReservation(reservation)) {
            return false;
        }

        if (reservation.getIdReservation() <= 0) {
            System.err.println(
                    "El ID de la reserva no es válido."
            );
            return false;
        }

        String sql =
                "UPDATE Reservation SET " +
                        "idCustomer = ?, " +
                        "checkIn = ?, " +
                        "checkOut = ?, " +
                        "idReservationStatus = ?, " +
                        "idReservationType = ?, " +
                        "numberOfGuests = ?, " +
                        "totalRate = ?, " +
                        "observations = ? " +
                        "WHERE idReservation = ?";

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(
                    1,
                    reservation.getIdCustomer()
            );

            stmt.setDate(
                    2,
                    Date.valueOf(
                            reservation.getCheckIn()
                    )
            );

            stmt.setDate(
                    3,
                    Date.valueOf(
                            reservation.getCheckOut()
                    )
            );

            stmt.setInt(
                    4,
                    reservation.getIdReservationStatus()
            );

            stmt.setInt(
                    5,
                    reservation.getIdReservationType()
            );

            stmt.setInt(
                    6,
                    reservation.getNumberOfGuests()
            );

            stmt.setBigDecimal(
                    7,
                    reservation.getTotalRate()
            );

            stmt.setString(
                    8,
                    reservation.getObservations()
            );

            stmt.setInt(
                    9,
                    reservation.getIdReservation()
            );

            int rowsAffected =
                    stmt.executeUpdate();

            if (rowsAffected > 0) {

                System.out.println(
                        "Reserva actualizada dentro de la transacción."
                );

                return true;
            }

            return false;

        } catch (SQLException e) {

            System.err.println(
                    "Error al actualizar la reserva dentro de la transacción: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // =========================================================
    // UPDATE STATUS
    // =========================================================

    public boolean updateReservationStatus(
            int idReservation,
            int idReservationStatus) {

        if (idReservation <= 0) {

            System.err.println(
                    "El ID de la reserva no es válido."
            );

            return false;
        }

        if (idReservationStatus <= 0) {

            System.err.println(
                    "El estado de la reserva no es válido."
            );

            return false;
        }

        String sql =
                "UPDATE Reservation " +
                        "SET idReservationStatus = ? " +
                        "WHERE idReservation = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(
                    1,
                    idReservationStatus
            );

            stmt.setInt(
                    2,
                    idReservation
            );

            int rowsAffected =
                    stmt.executeUpdate();

            if (rowsAffected > 0) {

                System.out.println(
                        "Estado de la reserva actualizado correctamente."
                );

                return true;
            }

            System.out.println(
                    "No se encontró la reserva con ID: "
                            + idReservation
            );

            return false;

        } catch (SQLException e) {

            System.err.println(
                    "Error al actualizar el estado de la reserva: "
                            + e.getMessage()
            );

            return false;
        }
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private boolean validateReservation(
            Reservation reservation) {

        if (reservation == null) {

            System.err.println(
                    "La reserva no puede ser null."
            );

            return false;
        }

        if (reservation.getIdCustomer() <= 0) {

            System.err.println(
                    "El cliente asociado a la reserva no es válido."
            );

            return false;
        }

        if (reservation.getCreationDate() == null) {

            System.err.println(
                    "La fecha de creación es obligatoria."
            );

            return false;
        }

        if (reservation.getCheckIn() == null) {

            System.err.println(
                    "La fecha de check-in es obligatoria."
            );

            return false;
        }

        if (reservation.getCheckOut() == null) {

            System.err.println(
                    "La fecha de check-out es obligatoria."
            );

            return false;
        }

        if (!reservation.getCheckOut()
                .isAfter(reservation.getCheckIn())) {

            System.err.println(
                    "La fecha de check-out debe ser posterior "
                            + "a la fecha de check-in."
            );

            return false;
        }

        if (reservation.getIdReservationStatus() <= 0) {

            System.err.println(
                    "El estado de la reserva no es válido."
            );

            return false;
        }

        if (reservation.getIdReservationType() <= 0) {

            System.err.println(
                    "El tipo de reserva no es válido."
            );

            return false;
        }

        if (reservation.getNumberOfGuests() <= 0) {

            System.err.println(
                    "La cantidad de huéspedes debe ser mayor que cero."
            );

            return false;
        }

        if (reservation.getTotalRate() == null ||
                reservation.getTotalRate()
                        .compareTo(BigDecimal.ZERO) < 0) {

            System.err.println(
                    "La tarifa total no puede ser negativa."
            );

            return false;
        }

        return true;
    }
}

