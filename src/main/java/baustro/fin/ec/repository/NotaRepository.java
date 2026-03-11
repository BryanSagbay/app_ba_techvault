package baustro.fin.ec.repository;

import baustro.fin.ec.model.Nota;
import baustro.fin.ec.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotaRepository {

    private static NotaRepository instance;
    public static NotaRepository getInstance() {
        if (instance == null) instance = new NotaRepository();
        return instance;
    }

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<Nota> findAll() throws SQLException {
        List<Nota> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM notas ORDER BY actualizado_en DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Nota> search(String texto) throws SQLException {
        List<Nota> list = new ArrayList<>();
        String sql = "SELECT * FROM notas WHERE titulo LIKE ? OR contenido LIKE ? OR tags LIKE ? " +
                     "ORDER BY actualizado_en DESC";
        String like = "%" + (texto == null ? "" : texto.trim()) + "%";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void save(Nota n) throws SQLException {
        if (n.getId() == 0) insert(n); else update(n);
    }

    private void insert(Nota n) throws SQLException {
        String sql = "INSERT INTO notas(titulo,contenido,categoria,tags) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n.getTitulo());
            ps.setString(2, n.getContenido());
            ps.setString(3, n.getCategoria());
            ps.setString(4, n.getTags());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) n.setId(keys.getInt(1));
        }
    }

    private void update(Nota n) throws SQLException {
        String sql = "UPDATE notas SET titulo=?,contenido=?,categoria=?,tags=?," +
                     "actualizado_en=datetime('now') WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, n.getTitulo());
            ps.setString(2, n.getContenido());
            ps.setString(3, n.getCategoria());
            ps.setString(4, n.getTags());
            ps.setInt(5, n.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM notas WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Nota map(ResultSet rs) throws SQLException {
        Nota n = new Nota();
        n.setId(rs.getInt("id"));
        n.setTitulo(rs.getString("titulo"));
        n.setContenido(rs.getString("contenido"));
        n.setCategoria(rs.getString("categoria"));
        n.setTags(rs.getString("tags"));
        n.setCreadoEn(rs.getString("creado_en"));
        n.setActualizadoEn(rs.getString("actualizado_en"));
        return n;
    }
}
