package baustro.fin.ec.model;

public class Comando {
    private int id;
    private String titulo;
    private String comando;
    private String descripcion;
    private String categoria;
    private String sistemaOperativo;

    public Comando() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getComando() { return comando; }
    public void setComando(String comando) { this.comando = comando; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getSistemaOperativo() { return sistemaOperativo; }
    public void setSistemaOperativo(String so) { this.sistemaOperativo = so; }

    @Override
    public String toString() { return titulo; }
}
