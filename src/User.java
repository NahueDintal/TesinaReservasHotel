public class User {
  private int idUser;
  private String name;
  private String password;
  private boolean status; // estado de perfil, si está habilitado o no.
  private String permissions; // escala de permits, usuario, admin, o persimisos a seleccionar??
  private String role;

  public User(int idUser, String name, String password, boolean enable, String permissions, String role) {
    this.name = name;
    this.password = password;
    this.status = true;
    this.permissions = permissions;
    this.role = role;
  }

  public int getIdUser() {
    return idUser;
  }

  public String getName() {
    return name;
  }

  public boolean getStatus() {
    return status;
  }

  public String getPermissions() {
    return permissions;
  }

  public String getRole() {
    return role;
  }

  // funcion de logging, mepa que hay que hacer una clase para el logging...como
  // un servicio
  public boolean Loggin(String user, String password) {
    return true;
  }
  // funcion de logout
}
