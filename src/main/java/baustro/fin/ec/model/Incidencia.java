package baustro.fin.ec.model;

public class Incidencia {
    private int id;
    private String numero;
    private String titulo;
    private String descripcion;
    private String servicio;
    private String solucion;
    private String estado;   // ABIERTO, EN_PROCESO, CERRADO
    private String prioridad; // BAJA, MEDIA, ALTA, CRITICA
    private String fechaInicio;
    private String fechaCierre;
    private String tags;
    private String creadoEn;
    private String actualizadoEn;

    public Incidencia() {}

    public Incidencia(String numero, String titulo, String descripcion,
                      String servicio, String estado, String prioridad, String fechaInicio) {
        this.numero      = numero;
        this.titulo      = titulo;
        this.descripcion = descripcion;
        this.servicio    = servicio;
        this.estado      = estado;
        this.prioridad   = prioridad;
        this.fechaInicio = fechaInicio;
    }

    // ---- Getters & Setters ----
    public int getId()                     { return id; }
    public void setId(int id)              { this.id = id; }
    public String getNumero()              { return numero; }
    public void setNumero(String v)        { this.numero = v; }
    public String getTitulo()              { return titulo; }
    public void setTitulo(String v)        { this.titulo = v; }
    public String getDescripcion()         { return descripcion; }
    public void setDescripcion(String v)   { this.descripcion = v; }
    public String getServicio()            { return servicio; }
    public void setServicio(String v)      { this.servicio = v; }
    public String getSolucion()            { return solucion; }
    public void setSolucion(String v)      { this.solucion = v; }
    public String getEstado()              { return estado; }
    public void setEstado(String v)        { this.estado = v; }
    public String getPrioridad()           { return prioridad; }
    public void setPrioridad(String v)     { this.prioridad = v; }
    public String getFechaInicio()         { return fechaInicio; }
    public void setFechaInicio(String v)   { this.fechaInicio = v; }
    public String getFechaCierre()         { return fechaCierre; }
    public void setFechaCierre(String v)   { this.fechaCierre = v; }
    public String getTags()                { return tags; }
    public void setTags(String v)          { this.tags = v; }
    public String getCreadoEn()            { return creadoEn; }
    public void setCreadoEn(String v)      { this.creadoEn = v; }
    public String getActualizadoEn()       { return actualizadoEn; }
    public void setActualizadoEn(String v) { this.actualizadoEn = v; }

    @Override
    public String toString() { return "[" + numero + "] " + titulo; }
}
