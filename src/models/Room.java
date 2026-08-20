package models;

public class Room {
  private int number;
  private int floor;
  private String type; // simple, doble, suite, familiar
  private int capacity;
  private String view;
  private boolean available; // disponible, ocupada
  private boolean outOfService; // mantenimiento o fuera de servicio
  private String features; // wifi, tv, aire acondicionado
  private double price;
  private String description;

  public Room(int number, int floor, String type, int capacity, String view, Boolean available, String features,
      double price, String description) {
    try {
      this.number = number;
    } catch (NumberFormatException e) {
      System.out.println("Valor de 'Numero de habitación' inválido! Ingrese un número entero positivo.");
    }
    try {
      this.floor = floor;
    } catch (NumberFormatException e) {
      System.out.println("Valor de 'Piso' inválido! Ingrese un número entero positivo.");
    }
    try {
      this.type = type;
      if (type.isEmpty()) {
        System.out.println("El campo no puede quedar vacío.");
      }
    } catch (IllegalArgumentException e) {
      System.out.println("Valor de 'Tipo' vacío! Ingrese una de las opciones");
    }
    try {
      this.capacity = capacity;
    } catch (NumberFormatException e) {
      System.out.println("Valor de 'capacidad' inválido! Ingrese un número entero positivo");
    }
    this.view = view;
    this.available = available;
    this.features = features;
    this.price = price;
    this.description = description;
  }

  public int getNumber() {
    return number;
  }

  public int getFloor() {
    return floor;
  }

  public String getType() {
    return type;
  }

  public int getCapacity() {
    return capacity;
  }

  public String view() {
    return view;
  }

  public boolean getAvailable() {
    return available;
  }

  public boolean getOutOfService() {
    return outOfService;
  }

  public void setOutOfService(boolean outOfService) {
    this.outOfService = outOfService;
  }

  public String getFeatures() {
    return features;
  }

  public double getPrice() {
    return price;
  }

  public String getDescription() {
    return description;
  }

}
