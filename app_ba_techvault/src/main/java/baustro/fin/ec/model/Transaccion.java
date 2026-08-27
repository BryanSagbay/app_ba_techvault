package baustro.fin.ec.model;

public class Transaccion {
    private int id;
    private String trx;
    private String subsistema;
    private String subtransaccion;
    private String descripcion;
    private String tipo;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTrx() { return trx; }
    public void setTrx(String trx) { this.trx = trx; }
    public String getSubsistema() { return subsistema; }
    public void setSubsistema(String subsistema) { this.subsistema = subsistema; }
    public String getSubtransaccion() { return subtransaccion; }
    public void setSubtransaccion(String subtransaccion) { this.subtransaccion = subtransaccion; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
