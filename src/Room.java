public class Room {
  private int number;
  private String type; // type de camas dispobibles, acá usariamos la nomenclatura del hotel, si es
                       // matrimonial 'm', and so on...
  private double pricePerNight;
  private boolean available;
  private boolean outOfOrder; // habitacion fuera de servicio.

  public Room(int number, String type, double pricePerNight) {
    this.number = number;
    this.type = type;
    this.pricePerNight = pricePerNight;
    this.available = true;
  }

  public int getNumber() {
    return number;
  }

  public String getType() {
    return type;
  }

  public double getPricePerNight() {
    return pricePerNight;
  }

  public boolean getAvailable() {
    return available;
  }
}
