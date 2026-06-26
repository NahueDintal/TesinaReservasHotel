public class Usuario {
  private int idUsuario;
  private String nombre;
  private String clave;
  private boolean habilitado;
  private String permisos; // escala de permisos, usuario, admin, o persimisos a seleccionar??
  private String area;

  public Usuario(int idUsuario, String nombre, String clave, boolean habilitado, String permisos, String area) {
    this.nombre = nombre;
    this.clave = clave;
    this.habilitado = true;
    this.permisos = permisos;
    this.area = area;
  }

  public int getIdUsuario() {
    return idUsuario;
  }

  public String getNombre() {
    return nombre;
  }

  public boolean getHabilitado() {
    return habilitado;
  }

  public String getPermisos() {
    return permisos;
  }

  public String getArea() {
    return area;
  }
}
