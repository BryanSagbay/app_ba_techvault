package baustro.fin.ec.model;

public class Contrasena {
    private int id;
    private String titulo;
    private String usuario;
    private String contrasenaCifrada;
    private String url;
    private String categoria;
    private String notas;

    public Contrasena() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getContrasenaCifrada() { return contrasenaCifrada; }
    public void setContrasenaCifrada(String c) { this.contrasenaCifrada = c; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    @Override
    public String toString() { return titulo + " (" + usuario + ")"; }
}
