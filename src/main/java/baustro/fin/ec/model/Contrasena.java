package baustro.fin.ec.model;

public class Contrasena {
    private int    id;
    private String titulo;
    private String usuario;
    private String passwordCifrada; // Guardado cifrado en DB
    private String url;
    private String categoria;  // DB, SSH, APP, WEB, SISTEMA
    private Integer servidorId;
    private String notas;
    private String creadoEn;
    private String actualizadoEn;

    public Contrasena() {}

    // ---- Getters & Setters ----
    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }
    public String getTitulo()                { return titulo; }
    public void setTitulo(String v)          { this.titulo = v; }
    public String getUsuario()               { return usuario; }
    public void setUsuario(String v)         { this.usuario = v; }
    public String getPasswordCifrada()       { return passwordCifrada; }
    public void setPasswordCifrada(String v) { this.passwordCifrada = v; }
    public String getUrl()                   { return url; }
    public void setUrl(String v)             { this.url = v; }
    public String getCategoria()             { return categoria; }
    public void setCategoria(String v)       { this.categoria = v; }
    public Integer getServidorId()           { return servidorId; }
    public void setServidorId(Integer v)     { this.servidorId = v; }
    public String getNotas()                 { return notas; }
    public void setNotas(String v)           { this.notas = v; }
    public String getCreadoEn()              { return creadoEn; }
    public void setCreadoEn(String v)        { this.creadoEn = v; }
    public String getActualizadoEn()         { return actualizadoEn; }
    public void setActualizadoEn(String v)   { this.actualizadoEn = v; }

    @Override
    public String toString() { return titulo + (usuario != null ? " (" + usuario + ")" : ""); }
}
