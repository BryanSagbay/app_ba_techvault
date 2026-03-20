package baustro.fin.ec.dao;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.model.Emergente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmergenteDAO {

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<Emergente> findAll() throws SQLException {
        List<Emergente> list = new ArrayList<>();
        String sql = "SELECT * FROM emergentes ORDER BY fecha DESC, numero_emergente ASC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void insert(Emergente e) throws SQLException {
        String sql = "INSERT INTO emergentes (numero_emergente,fecha,tipo,descripcion,subsistema,transacciones) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, e.getNumeroEmergente());
            ps.setString(2, e.getFecha());
            ps.setString(3, e.getTipo());
            ps.setString(4, e.getDescripcion());
            ps.setString(5, e.getSubsistema());
            ps.setString(6, e.getTransacciones());
            ps.executeUpdate();
        }
    }

    public void update(Emergente e) throws SQLException {
        String sql = "UPDATE emergentes SET numero_emergente=?,fecha=?,tipo=?,descripcion=?,subsistema=?,transacciones=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, e.getNumeroEmergente());
            ps.setString(2, e.getFecha());
            ps.setString(3, e.getTipo());
            ps.setString(4, e.getDescripcion());
            ps.setString(5, e.getSubsistema());
            ps.setString(6, e.getTransacciones());
            ps.setInt(7, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM emergentes WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Emergente map(ResultSet rs) throws SQLException {
        Emergente e = new Emergente();
        e.setId(rs.getInt("id"));
        e.setNumeroEmergente(rs.getString("numero_emergente"));
        e.setFecha(rs.getString("fecha"));
        e.setTipo(rs.getString("tipo"));
        e.setDescripcion(rs.getString("descripcion"));
        e.setSubsistema(rs.getString("subsistema"));
        e.setTransacciones(rs.getString("transacciones"));
        return e;
    }
}
