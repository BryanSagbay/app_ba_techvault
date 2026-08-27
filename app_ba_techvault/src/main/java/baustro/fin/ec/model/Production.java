package baustro.fin.ec.model;

public class Production {
    private int id;
    private String titulo;
    private String descripcion;
    private String prioridad;
    private String estado;
    private String fechaLimite;
    private String categoria;

    public Production() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(String f) { this.fechaLimite = f; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    @Override
    public String toString() { return titulo; }
}
