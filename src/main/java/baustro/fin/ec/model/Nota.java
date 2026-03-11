package baustro.fin.ec.model;

public class Nota {
    private int    id;
    private String titulo;
    private String contenido;
    private String categoria;
    private String tags;
    private String creadoEn;
    private String actualizadoEn;

    public Nota() {}

    public int getId()                     { return id; }
    public void setId(int id)              { this.id = id; }
    public String getTitulo()              { return titulo; }
    public void setTitulo(String v)        { this.titulo = v; }
    public String getContenido()           { return contenido; }
    public void setContenido(String v)     { this.contenido = v; }
    public String getCategoria()           { return categoria; }
    public void setCategoria(String v)     { this.categoria = v; }
    public String getTags()                { return tags; }
    public void setTags(String v)          { this.tags = v; }
    public String getCreadoEn()            { return creadoEn; }
    public void setCreadoEn(String v)      { this.creadoEn = v; }
    public String getActualizadoEn()       { return actualizadoEn; }
    public void setActualizadoEn(String v) { this.actualizadoEn = v; }

    @Override
    public String toString() { return titulo; }
}
