package models;

public class Customer {
  private int idCustomer;
  private String name;
  private String surname;
  private int idDocumentType;
  private String documentNumber;
  private String phoneNumber;
  private String email;
  private int idCountry;
  private int idCustomerStatus;
  private int idCustomerOrigin;

  // atributos de tablas catalogo
  private String documentTypeName;
  private String countryName;
  private String statusName;
  private String originName;



  //constructor
  public Customer() {}
  public Customer(int idClient, String name, String surname,int idDocumentType, String documentNumber,
                  String phoneNumber, String email,int idCountry, int idCustomerStatus, int idCustomerOrigin) {
    this.name = name;
    this.surname = surname;
    this.idDocumentType = idDocumentType;
    this.documentNumber = documentNumber;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.idCountry = idCountry;
    this.idCustomerStatus = idCustomerStatus;
    this.idCustomerOrigin = idCustomerOrigin;

  }
  // GETTERS
  public int getIdCustomer() {return idCustomer;}
  public String getName() {
    return name;
  }
  public String getSurname() {
    return surname;
  }
  public int getIdDocumentType() {
    return idDocumentType;
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
  public int getIdCountry() {return idCountry; }
  public int getIdCustomerStatus() {
    return idCustomerStatus;
  }
  public int getIdCustomerOrigin() {return idCustomerOrigin; }
  //Atributos Catalogo
  public String getDocumentTypeName() {
    return documentTypeName;
  }
  public String getCountryName() {
    return countryName;
  }
  public String getStatusName() {
    return statusName;
  }
  public String getOriginName() {
    return originName;
  }


  // SETTERS
  public void setIdCustomer(int idCustomer) { this.idCustomer = idCustomer; }
  public void setName(String name) { this.name = name; }
  public void setSurname(String surname) { this.surname = surname; }
  public void setIdDocumentType(int idDocumentType) { this.idDocumentType = idDocumentType; }
  public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
  public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
  public void setEmail(String email) { this.email = email; }
  public void setIdCountry(int idCountry) { this.idCountry = idCountry; }
  public void setIdCustomerStatus(int idCustomerStatus) { this.idCustomerStatus = idCustomerStatus; }
  public void setIdCustomerOrigin(int idCustomerOrigin) { this.idCustomerOrigin = idCustomerOrigin; }
  //Atributos Catalogo
  public void setDocumentTypeName(String documentTypeName) { this.documentNumber = documentTypeName; }
  public void setCountryName(String countryName) { this.countryName = countryName; }
  public void setStatusName(String statusName) { this.statusName = statusName; }
  public void setOriginName(String originName) { this.originName = originName; }


  @Override
  public String toString() {
    return name + " " + surname + " (" + documentNumber + ")";
  }
}
