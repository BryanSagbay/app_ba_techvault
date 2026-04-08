package baustro.fin.ec.dao;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.model.Production;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductionDAO {

    private Connection getConn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Production> findAll() throws SQLException {
        List<Production> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM produccion ORDER BY CASE prioridad WHEN 'Alta' THEN 1 WHEN 'Media' THEN 2 ELSE 3 END")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Production> findByEstado(String estado) throws SQLException {
        List<Production> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement("SELECT * FROM produccion WHERE estado=? ORDER BY fecha_limite")) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(Production t) throws SQLException {
        String sql = "INSERT INTO produccion (titulo, descripcion, prioridad, estado, fecha_limite, categoria) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, t.getTitulo()); ps.setString(2, t.getDescripcion());
            ps.setString(3, t.getPrioridad()); ps.setString(4, t.getEstado());
            ps.setString(5, t.getFechaLimite()); ps.setString(6, t.getCategoria());
            ps.executeUpdate();
        }
    }

    public void update(Production t) throws SQLException {
        String sql = "UPDATE produccion SET titulo=?, descripcion=?, prioridad=?, estado=?, fecha_limite=?, categoria=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, t.getTitulo()); ps.setString(2, t.getDescripcion());
            ps.setString(3, t.getPrioridad()); ps.setString(4, t.getEstado());
            ps.setString(5, t.getFechaLimite()); ps.setString(6, t.getCategoria());
            ps.setInt(7, t.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM produccion WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    private Production map(ResultSet rs) throws SQLException {
        Production t = new Production();
        t.setId(rs.getInt("id")); t.setTitulo(rs.getString("titulo"));
        t.setDescripcion(rs.getString("descripcion")); t.setPrioridad(rs.getString("prioridad"));
        t.setEstado(rs.getString("estado")); t.setFechaLimite(rs.getString("fecha_limite"));
        t.setCategoria(rs.getString("categoria"));
        return t;
    }
}