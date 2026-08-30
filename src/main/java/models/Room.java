package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Room {

  private static final Logger logger = LoggerFactory.getLogger(Room.class);

  private int number;
  private int floor;
  private String type;
  private int capacity;
  private String view;
  private boolean available = true; // disponibilidad directa
  private boolean outOfService = false; // mantenimiento o fuera de servicio
  private String features;
  private double price;
  private String description;

  // ---------- CONSTRUCTOR VACÍO ----------
  public Room() {
    // Se usa para crear una habitación sin datos iniciales y luego setearlos
  }

  // ---------- CONSTRUCTOR CON PARÁMETROS STRING ----------
  public Room(String numberStr, String floorStr, String type, String capacityStr,
      String view, String features, String priceStr, String description) {
    setNumber(numberStr);
    setFloor(floorStr);
    setType(type);
    setCapacity(capacityStr);
    setView(view);
    setFeatures(features);
    setPrice(priceStr);
    setDescription(description);
  }

  // ========== GETTERS ==========
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

  public String getFeatures() {
    return features;
  }

  public double getPrice() {
    return price;
  }

  public String getDescription() {
    return description;
  }

  public boolean getOutOfService() {
    return outOfService;
  }

  /**
   * Disponibilidad efectiva: solo true si no está fuera de servicio y available
   * es true.
   */
  public boolean isAvailable() {
    return !outOfService && available;
  }

  // ========== SETTERS (lanza IllegalArgumentException si el dato es inválido)
  // ==========

  public void setNumber(String numberStr) {
    try {
      int parsed = Integer.parseInt(numberStr);
      if (parsed < 0) {
        throw new NumberFormatException("El número debe ser un entero positivo.");
      }
      this.number = parsed;
    } catch (NumberFormatException e) {
      logger.error("Número de habitación inválido: '{}'", numberStr, e);
      throw new IllegalArgumentException("Número de habitación inválido", e);
    }
  }

  public void setFloor(String floorStr) {
    try {
      int parsed = Integer.parseInt(floorStr);
      if (parsed < 0) {
        throw new NumberFormatException("El piso debe ser un entero positivo.");
      }
      this.floor = parsed;
    } catch (NumberFormatException e) {
      logger.error("Piso inválido: '{}'", floorStr, e);
      throw new IllegalArgumentException("Piso inválido", e);
    }
  }

  public void setType(String type) {
    if (type == null || type.isBlank()) {
      logger.error("Tipo de habitación vacío");
      throw new IllegalArgumentException("El tipo no puede estar vacío.");
    }
    this.type = type.trim().toLowerCase();
  }

  public void setCapacity(String capacityStr) {
    try {
      int parsed = Integer.parseInt(capacityStr);
      if (parsed <= 0) {
        throw new NumberFormatException("La capacidad debe ser mayor a cero.");
      }
      this.capacity = parsed;
    } catch (NumberFormatException e) {
      logger.error("Capacidad inválida: '{}'", capacityStr, e);
      throw new IllegalArgumentException("Capacidad inválida", e);
    }
  }

  public void setView(String view) {
    if (view == null || view.isBlank()) {
      logger.error("Vista vacía");
      throw new IllegalArgumentException("La vista no puede estar vacía.");
    }
    this.view = view.trim();
  }

  public void setFeatures(String features) {
    if (features == null || features.isBlank()) {
      logger.error("Características vacías");
      throw new IllegalArgumentException("Las características no pueden estar vacías.");
    }
    this.features = features.trim();
  }

  public void setPrice(String priceStr) {
    try {
      double parsed = Double.parseDouble(priceStr);
      if (parsed < 0) {
        throw new NumberFormatException("El precio no puede ser negativo.");
      }
      this.price = parsed;
    } catch (NumberFormatException e) {
      logger.error("Precio inválido: '{}'", priceStr, e);
      throw new IllegalArgumentException("Precio inválido", e);
    }
  }

  public void setDescription(String description) {
    if (description == null || description.isBlank()) {
      logger.warn("Descripción vacía, se asigna cadena vacía");
      this.description = "";
    } else {
      this.description = description.trim();
    }
  }

  /**
   * Establece la disponibilidad directa.
   * No permite marcar como disponible si la habitación está fuera de servicio.
   */
  public void setAvailable(boolean available) {
    if (available && this.outOfService) {
      logger.warn("Intento de marcar como disponible una habitación fuera de servicio (número: {})", this.number);
      throw new IllegalArgumentException("Habitación fuera de servicio, no puede estar disponible.");
    }
    this.available = available;
  }

  /**
   * Marca la habitación como fuera de servicio o no.
   * Si se marca fuera de servicio, automáticamente queda no disponible.
   */
  public void setOutOfService(boolean outOfService) {
    this.outOfService = outOfService;
    if (outOfService) {
      this.available = false;
    }
  }
}
