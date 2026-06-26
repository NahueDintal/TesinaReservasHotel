public class User {
  private int idUser;
  private String name;
  private String password;
  private boolean enable;
  private String permits; // escala de permits, usuario, admin, o persimisos a seleccionar??
  private String area;

  public User(int idUser, String name, String password, boolean enable, String permits, String area) {
    this.name = name;
    this.password = password;
    this.enable = true;
    this.permits = permits;
    this.area = area;
  }

  public int getIdUser() {
    return idUser;
  }

  public String getName() {
    return name;
  }

  public boolean getEnable() {
    return enable;
  }

  public String getPermits() {
    return permits;
  }

  public String getArea() {
    return area;
  }
}
