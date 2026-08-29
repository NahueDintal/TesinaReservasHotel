package repositories;

import models.Consumption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConsumptionRepo {


    // =========================================================
    // CREATE
    // =========================================================

    public boolean createConsumption(
            Connection conn,
            Consumption consumption
    ) {

        String sql =
                "INSERT INTO Consumption (" +
                        "idReservation, " +
                        "idConsumptionType, " +
                        "idProduct, " +
                        "idService, " +
                        "quantity, " +
                        "unitPrice, " +
                        "total, " +
                        "consumptionDate, " +
                        "idPaymentStatus, " +
                        "idConsumptionStatus, " +
                        "observations" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    consumption.getIdReservation()
            );

            ps.setInt(
                    2,
                    consumption.getIdConsumptionType()
            );


            // PRODUCTO

            if (consumption.getIdProduct() > 0) {

                ps.setInt(
                        3,
                        consumption.getIdProduct()
                );

            } else {

                ps.setNull(
                        3,
                        java.sql.Types.INTEGER
                );
            }


            // SERVICIO

            if (consumption.getIdService() > 0) {

                ps.setInt(
                        4,
                        consumption.getIdService()
                );

            } else {

                ps.setNull(
                        4,
                        java.sql.Types.INTEGER
                );
            }


            ps.setInt(
                    5,
                    consumption.getQuantity()
            );

            ps.setBigDecimal(
                    6,
                    consumption.getUnitPrice()
            );

            ps.setBigDecimal(
                    7,
                    consumption.getTotal()
            );

            ps.setObject(
                    8,
                    consumption.getConsumptionDate()
            );

            ps.setInt(
                    9,
                    consumption.getIdPaymentStatus()
            );

            ps.setInt(
                    10,
                    consumption.getIdConsumptionStatus()
            );

            ps.setString(
                    11,
                    consumption.getObservations()
            );


            return ps.executeUpdate() > 0;


        } catch (SQLException e) {

            System.err.println(
                    "Error al crear el consumo: "
                            + e.getMessage()
            );

            return false;
        }
    }


    // =========================================================
    // GET CONSUMPTIONS BY RESERVATION
    // =========================================================

    public List<Consumption> getConsumptionsByReservation(
            int idReservation
    ) throws SQLException {

        List<Consumption> consumptions =
                new ArrayList<>();

        String sql =
                "SELECT idConsumption, idReservation, " +
                        "idConsumptionType, idProduct, idService, " +
                        "quantity, unitPrice, total, " +
                        "consumptionDate, idPaymentStatus, " +
                        "idConsumptionStatus, observations " +
                        "FROM Consumption " +
                        "WHERE idReservation = ? " +
                        "AND idConsumptionStatus = 1 " +
                        "ORDER BY idConsumption";


        try (Connection conn =
                     ConexionDB.getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    idReservation
            );


            try (ResultSet rs =
                         ps.executeQuery()) {


                while (rs.next()) {

                    Consumption consumption =
                            new Consumption();


                    consumption.setIdConsumption(
                            rs.getInt(
                                    "idConsumption"
                            )
                    );


                    consumption.setIdReservation(
                            rs.getInt(
                                    "idReservation"
                            )
                    );


                    consumption.setIdConsumptionType(
                            rs.getInt(
                                    "idConsumptionType"
                            )
                    );


                    consumption.setIdProduct(
                            rs.getInt(
                                    "idProduct"
                            )
                    );


                    consumption.setIdService(
                            rs.getInt(
                                    "idService"
                            )
                    );


                    consumption.setQuantity(
                            rs.getInt(
                                    "quantity"
                            )
                    );


                    consumption.setUnitPrice(
                            rs.getBigDecimal(
                                    "unitPrice"
                            )
                    );


                    consumption.setTotal(
                            rs.getBigDecimal(
                                    "total"
                            )
                    );


                    if (
                            rs.getTimestamp(
                                    "consumptionDate"
                            ) != null
                    ) {

                        consumption.setConsumptionDate(
                                rs.getTimestamp(
                                        "consumptionDate"
                                ).toLocalDateTime()
                        );
                    }


                    consumption.setIdPaymentStatus(
                            rs.getInt(
                                    "idPaymentStatus"
                            )
                    );


                    consumption.setIdConsumptionStatus(
                            rs.getInt(
                                    "idConsumptionStatus"
                            )
                    );


                    consumption.setObservations(
                            rs.getString(
                                    "observations"
                            )
                    );


                    consumptions.add(
                            consumption
                    );
                }
            }
        }


        return consumptions;
    }


    // =========================================================
    // UPDATE CONSUMPTION
    // =========================================================

    public boolean updateConsumption(
            Connection conn,
            Consumption consumption
    ) {

        String sql =
                "UPDATE Consumption SET " +
                        "idConsumptionType = ?, " +
                        "idProduct = ?, " +
                        "idService = ?, " +
                        "quantity = ?, " +
                        "unitPrice = ?, " +
                        "total = ?, " +
                        "observations = ? " +
                        "WHERE idConsumption = ?";


        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    consumption.getIdConsumptionType()
            );


            if (consumption.getIdProduct() > 0) {

                ps.setInt(
                        2,
                        consumption.getIdProduct()
                );

            } else {

                ps.setNull(
                        2,
                        java.sql.Types.INTEGER
                );
            }


            if (consumption.getIdService() > 0) {

                ps.setInt(
                        3,
                        consumption.getIdService()
                );

            } else {

                ps.setNull(
                        3,
                        java.sql.Types.INTEGER
                );
            }


            ps.setInt(
                    4,
                    consumption.getQuantity()
            );


            ps.setBigDecimal(
                    5,
                    consumption.getUnitPrice()
            );


            ps.setBigDecimal(
                    6,
                    consumption.getTotal()
            );


            ps.setString(
                    7,
                    consumption.getObservations()
            );


            ps.setInt(
                    8,
                    consumption.getIdConsumption()
            );


            int rowsAffected =
                    ps.executeUpdate();


            if (rowsAffected > 0) {

                System.out.println(
                        "Consumo actualizado correctamente."
                );

                return true;
            }


            return false;


        } catch (SQLException e) {

            System.err.println(
                    "Error al actualizar el consumo: "
                            + e.getMessage()
            );

            return false;
        }
    }


    // =========================================================
    // SOFT DELETE / ANULAR CONSUMPTION
    // =========================================================


    public boolean anularConsumption(
            Connection conn,
            int idConsumption
    ) {

        if (idConsumption <= 0) {
            System.err.println(
                    "El ID del consumo no es válido."
            );
            return false;
        }

        String sql =
                "UPDATE Consumption " +
                        "SET idConsumptionStatus = 2 " +
                        "WHERE idConsumption = ?";

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, idConsumption);

            int rowsAffected =
                    ps.executeUpdate();

            if (rowsAffected > 0) {

                System.out.println(
                        "Consumo anulado correctamente."
                );

                return true;
            }

            System.out.println(
                    "No se encontró el consumo con ID: "
                            + idConsumption
            );

            return false;

        } catch (SQLException e) {

            System.err.println(
                    "Error al anular el consumo: "
                            + e.getMessage()
            );

            return false;
        }
    }



