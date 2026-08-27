package baustro.fin.ec.model;

public class Caso {
    private int    id;
    private String numeroCaso;
    private String descripcion;
    private String solucion;
    private String script;
    private String area;
    private String createdAt;

    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public String getNumeroCaso()             { return numeroCaso; }
    public void   setNumeroCaso(String v)     { this.numeroCaso = v; }

    public String getDescripcion()            { return descripcion; }
    public void   setDescripcion(String v)    { this.descripcion = v; }

    public String getSolucion()               { return solucion; }
    public void   setSolucion(String v)       { this.solucion = v; }

    public String getScript()                 { return script; }
    public void   setScript(String v)         { this.script = v; }

    public String getArea()                   { return area; }
    public void   setArea(String v)           { this.area = v; }

    public String getCreatedAt()              { return createdAt; }
    public void   setCreatedAt(String v)      { this.createdAt = v; }
}
