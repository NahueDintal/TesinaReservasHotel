package models;

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
    setType(type);
    setCapacity(capacityStr);
    setView(view);
    setFeatures(features);
    setPrice(priceStr);
    setDescription(description);
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

  public void setType(String type) {
    try {
      if (type == null || type.isBlank()) {
        throw new IllegalArgumentException("El tipo no puede estar vacío.");
      }
      this.type = type;
    } catch (IllegalArgumentException e) {
      System.out.println("Valor de 'Tipo' vacío! " + e.getMessage());
    }
  }

  public void setCapacity(String capacityStr) {
    try {
      int parsed = Integer.parseInt(capacityStr);
      if (parsed <= 0) {
        throw new NumberFormatException("La capacidad de la habitación no puede quedar vacío.");
      }
      this.capacity = parsed;
    } catch (NumberFormatException e) {
      System.out.println("Valor de 'capacidad' inválido!" + e.getMessage());
    }
  }

  public void setView(String view) {
    try {
      if (view == null || view.isBlank()) {
        throw new IllegalArgumentException("La vista no puede estar vacío.");
      }
      this.view = view;
    } catch (IllegalArgumentException e) {
      System.out.println("Valor de 'Vista' inválido! " + e.getMessage());
    }
  }

  public void setFeatures(String features) {
    try {
      if (features == null || features.isBlank()) {
        throw new IllegalArgumentException("La vista no puede estar vacío.");
      }
      this.features = features;
    } catch (IllegalArgumentException e) {
      System.out.println("Valor de 'Características' inválido! " + e.getMessage());
    }
  }

  public void setPrice(String priceStr) {
    try {
      double parsed = Double.parseDouble(priceStr);
      if (parsed < 0) {
        throw new NumberFormatException("El precio por noche de la habitación no puede quedar vacío.");
      }
      this.price = parsed;
    } catch (NumberFormatException e) {
      System.out.println("Valor de 'Precio' inválido! " + e.getMessage() + " Se asigna $ 0.0.");
    }
  }

  public void setDescription(String description) {
    try {
      if (description == null || description.isBlank()) {
        throw new IllegalArgumentException("La vista no puede estar vacío.");
      }
      this.description = description;
    } catch (IllegalArgumentException e) {
      System.out.println("Valor de 'Características' inválido! " + e.getMessage());
    }
  }

  public void setIsAvailable(boolean isAvailable) {
    if (isAvailable && this.outOfService) {
      throw new IllegalArgumentException("Habitación fuera de servicio, no está dispobible para reservar.");
    }
    this.isAvailable = isAvailable;
  }

  public void setOutOfService(boolean outOfService) {
    this.outOfService = outOfService;
    if (outOfService) {
      this.isAvailable = false;
    }
  }

}
