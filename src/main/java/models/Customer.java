package models;

public class Customer {
  private int idCustomer;
  private String names;
  private String surnames;
  private String documentNumber;
  private String phoneNumber;
  private String email;
  private String address;

  //constructor
  public Customer() {}
  public Customer(int idClient, String names, String surnames, String documentNumber,
                  String phoneNumber, String email, String address) {
    this.names = names;
    this.surnames = surnames;
    this.documentNumber = documentNumber;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.address = address;
  }
  // GETTERS
  public int getIdCustomer() {return idCustomer;}
  public String getNames() {
    return names;
  }
  public String getSurnames() {
    return surnames;
  }
  public String getDocumentNumber() {
    return documentNumber;
  }
  public String getPhoneNumber() {
    return phoneNumber;
  }
  public String getEmail() {
    return email;
  }
  public String getAddress() {
    return address;
  }

  // SETTERS
  public void setIdCustomer(int idCustomer) { this.idCustomer = idCustomer; }
  public void setNames(String names) { this.names = names; }
  public void setSurnames(String surnames) { this.surnames = surnames; }
  public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
  public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
  public void setEmail(String email) { this.email = email; }
  public void setAddress(String email) { this.address = address; }
}
