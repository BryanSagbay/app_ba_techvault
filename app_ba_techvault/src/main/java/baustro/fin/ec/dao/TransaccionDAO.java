package baustro.fin.ec.dao;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.model.Transaccion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransaccionDAO {

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<Transaccion> findAll() throws SQLException {
        List<Transaccion> list = new ArrayList<>();
        String sql = "SELECT * FROM transacciones ORDER BY trx ASC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void insert(Transaccion t) throws SQLException {
        String sql = "INSERT INTO transacciones (trx,subsistema,subtransaccion,descripcion,tipo) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, t.getTrx());
            ps.setString(2, t.getSubsistema());
            ps.setString(3, t.getSubtransaccion());
            ps.setString(4, t.getDescripcion());
            ps.setString(5, t.getTipo());
            ps.executeUpdate();
        }
    }

    public void update(Transaccion t) throws SQLException {
        String sql = "UPDATE transacciones SET trx=?,subsistema=?,subtransaccion=?,descripcion=?,tipo=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, t.getTrx());
            ps.setString(2, t.getSubsistema());
            ps.setString(3, t.getSubtransaccion());
            ps.setString(4, t.getDescripcion());
            ps.setString(5, t.getTipo());
            ps.setInt(6, t.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM transacciones WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Transaccion map(ResultSet rs) throws SQLException {
        Transaccion t = new Transaccion();
        t.setId(rs.getInt("id"));
        t.setTrx(rs.getString("trx"));
        t.setSubsistema(rs.getString("subsistema"));
        t.setSubtransaccion(rs.getString("subtransaccion"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setTipo(rs.getString("tipo"));
        return t;
    }
}
