package baustro.fin.ec.model;

public class Emergente {
    private int id;
    private String numeroEmergente;
    private String fecha;
    private String tipo;
    private String descripcion;
    private String subsistema;
    private String transacciones;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumeroEmergente() { return numeroEmergente; }
    public void setNumeroEmergente(String numeroEmergente) { this.numeroEmergente = numeroEmergente; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getSubsistema() { return subsistema; }
    public void setSubsistema(String subsistema) { this.subsistema = subsistema; }
    public String getTransacciones() { return transacciones; }
    public void setTransacciones(String transacciones) { this.transacciones = transacciones; }
}
