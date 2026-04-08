package baustro.fin.ec.dao;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.model.Caso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CasoDAO {

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    /** Siempre más reciente primero (created_at DESC) */
    public List<Caso> findAll() throws SQLException {
        List<Caso> list = new ArrayList<>();
        String sql = "SELECT * FROM casos ORDER BY created_at DESC";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void insert(Caso c) throws SQLException {
        String sql = "INSERT INTO casos (numero_caso, descripcion, solucion, script, area) " +
                     "VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getNumeroCaso());
            ps.setString(2, c.getDescripcion());
            ps.setString(3, c.getSolucion());
            ps.setString(4, c.getScript());
            ps.setString(5, c.getArea());
            ps.executeUpdate();
        }
    }

    public void update(Caso c) throws SQLException {
        String sql = "UPDATE casos SET numero_caso=?, descripcion=?, solucion=?, script=?, area=? " +
                     "WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getNumeroCaso());
            ps.setString(2, c.getDescripcion());
            ps.setString(3, c.getSolucion());
            ps.setString(4, c.getScript());
            ps.setString(5, c.getArea());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps =
                conn().prepareStatement("DELETE FROM casos WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Caso map(ResultSet rs) throws SQLException {
        Caso c = new Caso();
        c.setId(rs.getInt("id"));
        c.setNumeroCaso(rs.getString("numero_caso"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setSolucion(rs.getString("solucion"));
        c.setScript(rs.getString("script"));
        c.setArea(rs.getString("area"));
        c.setCreatedAt(rs.getString("created_at"));
        return c;
    }
}