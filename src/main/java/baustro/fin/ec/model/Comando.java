package baustro.fin.ec.model;

public class Comando {
    private int     id;
    private String  titulo;
    private String  comando;
    private String  descripcion;
    private String  categoria; // LINUX, SQL, GIT, DOCKER, WINDOWS, ORACLE
    private String  so;        // Linux, Windows, Ambos
    private String  tags;
    private boolean favorito;
    private String  creadoEn;
    private String  actualizadoEn;

    public Comando() {}

    public int getId()                     { return id; }
    public void setId(int id)              { this.id = id; }
    public String getTitulo()              { return titulo; }
    public void setTitulo(String v)        { this.titulo = v; }
    public String getComando()             { return comando; }
    public void setComando(String v)       { this.comando = v; }
    public String getDescripcion()         { return descripcion; }
    public void setDescripcion(String v)   { this.descripcion = v; }
    public String getCategoria()           { return categoria; }
    public void setCategoria(String v)     { this.categoria = v; }
    public String getSo()                  { return so; }
    public void setSo(String v)            { this.so = v; }
    public String getTags()                { return tags; }
    public void setTags(String v)          { this.tags = v; }
    public boolean isFavorito()            { return favorito; }
    public void setFavorito(boolean v)     { this.favorito = v; }
    public String getCreadoEn()            { return creadoEn; }
    public void setCreadoEn(String v)      { this.creadoEn = v; }
    public String getActualizadoEn()       { return actualizadoEn; }
    public void setActualizadoEn(String v) { this.actualizadoEn = v; }

    @Override
    public String toString() { return titulo; }
}
