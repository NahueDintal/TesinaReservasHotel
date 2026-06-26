public class Client{
  private int idClient;
  private String name;
  private String surName;
  private String dni;
  private int phone;

  public Client(int idClient, String name, String surName, String dni, int phone) {
    this.name = name;
    this.surName = surName;
    this.dni = dni;
    this.phone = phone;
  }
  
  public int getIdClient() {
    return idClient;
  }

  public String getName() {
    return name;
  }

  public String getSurName() {
    return surName;
  }

  public String getDni() {
    return dni;
  }

  public int getPhone() {
    return phone;
  }
}
