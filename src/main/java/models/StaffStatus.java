package models;

/**
 * Enum StaffStatus
 * Representa el estado de un miembro del personal.
 * Se usa Enum en vez de String o boolean para evitar valores inválidos
 * y hacer el código más legible y seguro en tiempo de compilación.
 */
public enum StaffStatus {
    ACTIVE,   // Activo
    INACTIVE  // Inactivo
}