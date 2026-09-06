package models;

import java.time.LocalDateTime;

public class StaffHistory {

    private int idHistory;
    private String idStaff;
    private String changeType;    // "CREADO", "EDITADO", "INACTIVADO"
    private String description;
    private LocalDateTime changedAt;

    // CONSTRUCTOR
    public StaffHistory() {}

    public StaffHistory(String idStaff, String changeType, String description) {
        this.idStaff = idStaff;
        this.changeType = changeType;
        this.description = description;
    }

    // GETTERS
    public int getIdHistory() { return idHistory; }
    public String getIdStaff() { return idStaff; }
    public String getChangeType() { return changeType; }
    public String getDescription() { return description; }
    public LocalDateTime getChangedAt() { return changedAt; }

    // SETTERS
    public void setIdHistory(int idHistory) { this.idHistory = idHistory; }
    public void setIdStaff(String idStaff) { this.idStaff = idStaff; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public void setDescription(String description) { this.description = description; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    @Override
    public String toString() {
        return changedAt + " - " + changeType + ": " + description;
    }
}