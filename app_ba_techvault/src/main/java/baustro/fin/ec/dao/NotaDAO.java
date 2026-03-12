package baustro.fin.ec.dao;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.model.Nota;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotaDAO {

    private Connection getConn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Nota> findAll() throws SQLException {
        List<Nota> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM notas ORDER BY updated_at DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Nota> search(String query) throws SQLException {
        List<Nota> list = new ArrayList<>();
        String sql = "SELECT * FROM notas WHERE titulo LIKE ? OR contenido LIKE ? OR etiquetas LIKE ? ORDER BY updated_at DESC";
        String q = "%" + query + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(Nota n) throws SQLException {
        String sql = "INSERT INTO notas (titulo, contenido, etiquetas, color) VALUES (?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, n.getTitulo()); ps.setString(2, n.getContenido());
            ps.setString(3, n.getEtiquetas()); ps.setString(4, n.getColor());
            ps.executeUpdate();
        }
    }

    public void update(Nota n) throws SQLException {
        String sql = "UPDATE notas SET titulo=?, contenido=?, etiquetas=?, color=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, n.getTitulo()); ps.setString(2, n.getContenido());
            ps.setString(3, n.getEtiquetas()); ps.setString(4, n.getColor());
            ps.setInt(5, n.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM notas WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    private Nota map(ResultSet rs) throws SQLException {
        Nota n = new Nota();
        n.setId(rs.getInt("id")); n.setTitulo(rs.getString("titulo"));
        n.setContenido(rs.getString("contenido")); n.setEtiquetas(rs.getString("etiquetas"));
        n.setColor(rs.getString("color"));
        return n;
    }
}
