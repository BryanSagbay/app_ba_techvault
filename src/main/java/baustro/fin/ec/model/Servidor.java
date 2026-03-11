package baustro.fin.ec.model;

public class Servidor {
    private int id;
    private String nombre;
    private String ip;
    private String hostname;
    private String so;
    private String rol;      // DB, APP, WEB, PROXY, BALANCER
    private String ambiente; // PROD, QA, DEV, HOM
    private String descripcion;
    private String puertoSsh;
    private String usuario;
    private String notas;
    private boolean activo;
    private String creadoEn;
    private String actualizadoEn;

    public Servidor() { this.activo = true; this.puertoSsh = "22"; }

    // ---- Getters & Setters ----
    public int getId()                     { return id; }
    public void setId(int id)              { this.id = id; }
    public String getNombre()              { return nombre; }
    public void setNombre(String v)        { this.nombre = v; }
    public String getIp()                  { return ip; }
    public void setIp(String v)            { this.ip = v; }
    public String getHostname()            { return hostname; }
    public void setHostname(String v)      { this.hostname = v; }
    public String getSo()                  { return so; }
    public void setSo(String v)            { this.so = v; }
    public String getRol()                 { return rol; }
    public void setRol(String v)           { this.rol = v; }
    public String getAmbiente()            { return ambiente; }
    public void setAmbiente(String v)      { this.ambiente = v; }
    public String getDescripcion()         { return descripcion; }
    public void setDescripcion(String v)   { this.descripcion = v; }
    public String getPuertoSsh()           { return puertoSsh; }
    public void setPuertoSsh(String v)     { this.puertoSsh = v; }
    public String getUsuario()             { return usuario; }
    public void setUsuario(String v)       { this.usuario = v; }
    public String getNotas()               { return notas; }
    public void setNotas(String v)         { this.notas = v; }
    public boolean isActivo()              { return activo; }
    public void setActivo(boolean v)       { this.activo = v; }
    public String getCreadoEn()            { return creadoEn; }
    public void setCreadoEn(String v)      { this.creadoEn = v; }
    public String getActualizadoEn()       { return actualizadoEn; }
    public void setActualizadoEn(String v) { this.actualizadoEn = v; }

    @Override
    public String toString() { return nombre + " (" + ip + ")"; }
}
