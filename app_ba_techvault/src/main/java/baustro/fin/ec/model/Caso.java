package baustro.fin.ec.model;

public class Caso {
    private int id;
    private String numeroTarea;
    private String titulo;
    private String descripcion;
    private String ambiente;
    private String servicio;
    private String errorPresentado;
    private String solucion;
    private String estado;
    private String prioridad;
    private String fechaReporte;
    private String fechaSolucion;
    private String responsable;
    private String observaciones;

    public Caso() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumeroTarea() { return numeroTarea; }
    public void setNumeroTarea(String numeroTarea) { this.numeroTarea = numeroTarea; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getAmbiente() { return ambiente; }
    public void setAmbiente(String ambiente) { this.ambiente = ambiente; }
    public String getServicio() { return servicio; }
    public void setServicio(String servicio) { this.servicio = servicio; }
    public String getErrorPresentado() { return errorPresentado; }
    public void setErrorPresentado(String e) { this.errorPresentado = e; }
    public String getSolucion() { return solucion; }
    public void setSolucion(String solucion) { this.solucion = solucion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public String getFechaReporte() { return fechaReporte; }
    public void setFechaReporte(String f) { this.fechaReporte = f; }
    public String getFechaSolucion() { return fechaSolucion; }
    public void setFechaSolucion(String f) { this.fechaSolucion = f; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String o) { this.observaciones = o; }

    @Override
    public String toString() { return "[" + numeroTarea + "] " + titulo; }
}
