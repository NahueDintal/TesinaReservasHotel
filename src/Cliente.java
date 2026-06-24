public class Cliente{
  private int idCliente;
  private String nombre;
  private String apellido;
  private String dni;
  private int telefono;

  public Cliente(int idCliente, String nombre, String apellido, String dni, int telefono) {
    this.nombre = nombre;
    this.apellido = apellido;
    this.dni = dni;
    this.telefono = telefono;
  }
  
  public int getIdCliente() {
    return idCliente;
  }

  public String getNombre() {
    return nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public String getDni() {
    return dni;
  }

  public int getTelefono() {
    return telefono;
  }


}
