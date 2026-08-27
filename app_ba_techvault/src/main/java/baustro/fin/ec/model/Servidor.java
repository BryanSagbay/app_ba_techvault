package baustro.fin.ec.model;

public class Servidor {
    private int id;
    private String host;
    private String ip;
    private String tipo;
    private String ambiente;
    private String sistemaOperativo;
    private String descripcion;
    private String usuarioAcceso;
    private String contrasenaEncriptada;   // ← nuevo campo
    private String puerto;
    private String estado;
    private String notas;

    public Servidor() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getHost() { return host; }
    public void setHost(String nombre) { this.host = nombre; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getAmbiente() { return ambiente; }
    public void setAmbiente(String ambiente) { this.ambiente = ambiente; }
    public String getSistemaOperativo() { return sistemaOperativo; }
    public void setSistemaOperativo(String so) { this.sistemaOperativo = so; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getUsuarioAcceso() { return usuarioAcceso; }
    public void setUsuarioAcceso(String u) { this.usuarioAcceso = u; }
    public String getContrasenaEncriptada() { return contrasenaEncriptada; }
    public void setContrasenaEncriptada(String c) { this.contrasenaEncriptada = c; }
    public String getPuerto() { return puerto; }
    public void setPuerto(String puerto) { this.puerto = puerto; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    @Override
    public String toString() { return host + " (" + ip + ")"; }
}
