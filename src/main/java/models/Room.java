package main.java.models;

public class Room {
  private int number;
  private int floor;
  private String type; // solo estas opciones por el momento, simple, doble, suite, familiar
  private int capacity;
  private String view;
  private boolean isAvailable = true; // tiene que verificar que no este fuera de servicio antes
  private boolean outOfService = false; // mantenimiento o fuera de servicio
  private String features; // wifi, tv, aire acondicionado, etc.
  private double price;
  private String description;

  public Room(String numberStr, String floorStr, String type, String capacityStr, String view, String features,
      String priceStr, String description) {
    setNumber(numberStr);
    setFloor(floorStr);
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

  public String getView() {
    return view;
  }

  public boolean getOutOfService() {
    return outOfService;
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

  public boolean getIsAvailable() {
    if (!getOutOfService()) {
      return isAvailable;
    }
    return false;
  }

  public void setNumber(String numberStr) {
    try {
      int parsed = Integer.parseInt(numberStr);
      if (parsed < 0) {
        throw new NumberFormatException("El Número debe un valor entero positivo.");
      }
      this.number = parsed;
    } catch (NumberFormatException e) {
      System.out.println("Valor de 'Numero de habitación' inválido!." + e.getMessage());
    }
  }

  public void setFloor(String floorStr) {
    try {
      int parsed = Integer.parseInt(floorStr);
      if (parsed < 0) {
        System.out.println("El piso debe ser un valor entero positivo.");
      }
      this.floor = parsed;
    } catch (NumberFormatException e) {
      System.out.println("Valor de 'Piso' inválido!" + e.getMessage());
    }
  }

  public void setIsAvailable(boolean isAvailable) {
    this.isAvailable = isAvailable;
  }

  public void setOutOfService(boolean outOfService) {
    this.outOfService = outOfService;
  }

}
