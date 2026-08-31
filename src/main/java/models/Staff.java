package models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

public class Staff {

    private String id;             // PK tipo "001", no autoincremental
    private String firstName;
    private String lastName;
    private String dni;
    private LocalDate birthDate;
    private String phone;
    private String email;
    private String street;
    private String addressNumber;
    private String city;
    private int idPosition;
    private int idDepartment;
    private StaffStatus status;    // Enum ACTIVE/INACTIVE (no es tabla catálogo)
    private LocalDate hireDate;
    private String shiftName;
    private LocalTime shiftStart;
    private LocalTime shiftEnd;
    private BigDecimal salary;

    // Atributos de tablas catálogo (para mostrar, igual que en Customer)
    private String positionName;
    private String departmentName;

    // CONSTRUCTOR
    public Staff() {}

    public Staff(String firstName, String lastName, String dni, LocalDate birthDate,
                 String phone, String email, String street, String addressNumber, String city,
                 int idPosition, int idDepartment, StaffStatus status, LocalDate hireDate,
                 String shiftName, LocalTime shiftStart, LocalTime shiftEnd, BigDecimal salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.birthDate = birthDate;
        this.phone = phone;
        this.email = email;
        this.street = street;
        this.addressNumber = addressNumber;
        this.city = city;
        this.idPosition = idPosition;
        this.idDepartment = idDepartment;
        this.status = status;
        this.hireDate = hireDate;
        this.shiftName = shiftName;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.salary = salary;
    }

    // GETTERS
    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDni() { return dni; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getStreet() { return street; }
    public String getAddressNumber() { return addressNumber; }
    public String getCity() { return city; }
    public int getIdPosition() { return idPosition; }
    public int getIdDepartment() { return idDepartment; }
    public StaffStatus getStatus() { return status; }
    public LocalDate getHireDate() { return hireDate; }
    public String getShiftName() { return shiftName; }
    public LocalTime getShiftStart() { return shiftStart; }
    public LocalTime getShiftEnd() { return shiftEnd; }
    public BigDecimal getSalary() { return salary; }

    // Atributos catálogo
    public String getPositionName() { return positionName; }
    public String getDepartmentName() { return departmentName; }

    // SETTERS
    public void setId(String id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setDni(String dni) { this.dni = dni; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setStreet(String street) { this.street = street; }
    public void setAddressNumber(String addressNumber) { this.addressNumber = addressNumber; }
    public void setCity(String city) { this.city = city; }
    public void setIdPosition(int idPosition) { this.idPosition = idPosition; }
    public void setIdDepartment(int idDepartment) { this.idDepartment = idDepartment; }
    public void setStatus(StaffStatus status) { this.status = status; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public void setShiftStart(LocalTime shiftStart) { this.shiftStart = shiftStart; }
    public void setShiftEnd(LocalTime shiftEnd) { this.shiftEnd = shiftEnd; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    // Atributos catálogo
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    // Método de conveniencia, igual que usás getFullName-style en otras pantallas
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + dni + ")";
    }
}