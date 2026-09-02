package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Room {

  private static final Logger logger = LoggerFactory.getLogger(Room.class);

  private int idRoom;
  private int number;
  private int floor;
  private int idRoomType;
  private String typeName;
  private int capacity;
  private int idRoomView;
  private String viewName;
  private boolean available = true;
  private boolean outOfService = false;
  private boolean active = true;
  private List<String> features = new ArrayList<>();
  private double price;
  private String description;

  public Room() {
  }

  public Room(String numberStr, String floorStr, String idRoomTypeStr, String capacityStr,
      String idRoomViewStr, List<String> features, String priceStr, String description) {
    logger.debug("Creando Room desde Strings: number={}, floor={}, type={}, capacity={}, view={}, price={}",
        numberStr, floorStr, idRoomTypeStr, capacityStr, idRoomViewStr, priceStr);
    setNumber(numberStr);
    setFloor(floorStr);
    setIdRoomType(Integer.parseInt(idRoomTypeStr));
    setCapacity(capacityStr);
    setIdRoomView(Integer.parseInt(idRoomViewStr));
    setFeatures(features);
    setPrice(priceStr);
    setDescription(description);
    logger.debug("Room creado exitosamente con idRoom temporal {}", idRoom);
  }

  public int getIdRoom() {
    return idRoom;
  }

  public void setIdRoom(int idRoom) {
    this.idRoom = idRoom;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    if (number < 0) {
      logger.warn("Intento de asignar un número negativo {}", number);
      throw new IllegalArgumentException("El número de habitación debe ser un entero positivo.");
    }
    this.number = number;
    logger.warn("Número de habitación establecido {}", number);
  }

  public void setNumber(String numberStr) {
    try {
      setNumber(Integer.parseInt(numberStr));
    } catch (NumberFormatException e) {
      logger.error("Número de habitación inválido: '{}'", numberStr, e);
      throw new IllegalArgumentException("Número de habitación inválido", e);
    }
  }

  public int getFloor() {
    return floor;
  }

  public void setFloor(int floor) {
    if (floor < 0) {
      logger.warn("Intento de asignar un número negativo {}", floor);
      throw new IllegalArgumentException("El piso debe ser un entero positivo.");
    }
    this.floor = floor;
    logger.warn("Número de piso establecido {}", floor);
  }

  public void setFloor(String floorStr) {
    try {
      setFloor(Integer.parseInt(floorStr));
    } catch (NumberFormatException e) {
      logger.error("Piso inválido: '{}'", floorStr, e);
      throw new IllegalArgumentException("Piso inválido", e);
    }
  }

  public int getIdRoomType() {
    return idRoomType;
  }

  public void setIdRoomType(int idRoomType) {
    this.idRoomType = idRoomType;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int capacity) {
    if (capacity <= 0) {
      logger.warn("Intento de asignar un número negativo o nulo {}", floor);
      throw new IllegalArgumentException("La capacidad debe ser mayor a cero.");
    }
    this.capacity = capacity;
    logger.warn("Número de capacidad establecido {}", capacity);
  }

  public void setCapacity(String capacityStr) {
    try {
      setCapacity(Integer.parseInt(capacityStr));
    } catch (NumberFormatException e) {
      logger.error("Capacidad inválida: '{}'", capacityStr, e);
      throw new IllegalArgumentException("Capacidad inválida", e);
    }
  }

  public int getIdRoomView() {
    return idRoomView;
  }

  public void setIdRoomView(int idRoomView) {
    this.idRoomView = idRoomView;
  }

  public String getViewName() {
    return viewName;
  }

  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    if (available && outOfService) {
      logger.error("Imposibilidad para cambiar a modo disponible '{}'", available);
      throw new IllegalArgumentException("Habitación fuera de servicio, no puede estar disponible.");
    }
    this.available = available;
    logger.warn("Cambiar de estado disponibilidad {}", available);
  }

  public boolean isOutOfService() {
    return outOfService;
  }

  public void setOutOfService(boolean outOfService) {
    this.outOfService = outOfService;
    logger.warn("Cambiar de estado a fuera de servicio {}", outOfService);
    if (outOfService) {
      this.available = false;
      logger.warn("Cambia de estado disponibilidad {}", outOfService);
    }
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public List<String> getFeatures() {
    return features;
  }

  public void setFeatures(List<String> features) {
    this.features = (features != null) ? features : new ArrayList<>();
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    if (price < 0) {
      logger.error("Intento de poner un precio con valor negarivo {}", price);
      throw new IllegalArgumentException("El precio no puede ser negativo.");
    }
    this.price = price;
    logger.warn("Precio establecido {}", price);
  }

  public void setPrice(String priceStr) {
    try {
      setPrice(Double.parseDouble(priceStr));
    } catch (NumberFormatException e) {
      logger.error("Precio inválido: '{}'", priceStr, e);
      throw new IllegalArgumentException("Precio inválido", e);
    }
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = (description == null || description.isBlank()) ? "" : description.trim();
  }
}
