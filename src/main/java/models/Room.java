package main.java.models;

public class Room {
  private int number;
  private int floor;
  private String type; // simple, doble, suite, familiar
  private int capacity;
  private String view;
  private boolean isAvailable = true; // disponible, ocupada
  private boolean outOfService = false; // mantenimiento o fuera de servicio
  private String features; // wifi, tv, aire acondicionado
  private double price;
  private String description;

  public Room(String numberStr, String floorStr, String type, String capacityStr, String view, String features,
      String priceStr, String description) {
    try {
      this.number = Integer.parseInt(numberStr);
      if (this.number <= 0) {
        throw new NumberFormatException("El Número debe ser positivo.");
      }
    } catch (NumberFormatException e) {
      System.out.println("Valor de 'Numero de habitación' inválido! Se asigna 0.");
      this.number = 0;
    }
    try {
      this.floor = Integer.parseInt(floorStr);
      if (this.floor < 0) {
        System.out.println("El piso de la habitación no puede quedar vacío.");
      }
    } catch (NumberFormatException e) {
      System.out.println("Valor de 'Piso' inválido!" + e.getMessage() + " Se asigna 0.");
      this.floor = 0;
    }
    try {
      if (type == null || type.isBlank()) {
        throw new IllegalArgumentException("El tipo no puede estar vacío.");
      }
      this.type = type;
    } catch (IllegalArgumentException e) {
      System.out.println("Valor de 'Tipo' vacío! " + e.getMessage() + " Se asigna 'Simple'.");
      this.type = "simple";
    }
    try {
      this.capacity = Integer.parseInt(capacityStr);
      if (this.capacity <= 0) {
        throw new NumberFormatException("La capacidad de la habitación no puede quedar vacío.");
      }
    } catch (NumberFormatException e) {
      System.out.println("Valor de 'capacidad' inválido!" + e.getMessage() + "Se asigna 1.");
      this.capacity = 1;
    }
    this.view = view;
    this.features = features;
    try {
      this.price = Double.parseDouble(priceStr);
      if (this.price < 0) {
        throw new NumberFormatException("El precio por noche de la habitación no puede quedar vacío.");
      }
    } catch (NumberFormatException e) {
      System.out.println("Valor de 'Precio' inválido! " + e.getMessage() + " Se asigna $ 0.0.");
      this.price = 0.0;
    }
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

  public boolean getIsAvailable() {
    if (!getOutOfService()) {
      return isAvailable; 
    }
    return false;
  }

  public void setIsAvailable(boolean isAvailable) {
    this.isAvailable = isAvailable;
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
