package baustro.fin.ec.dao;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.model.Servidor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServidorDAO {

    private Connection getConn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Servidor> findAll() throws SQLException {
        List<Servidor> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM servidores ORDER BY nombre")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Servidor> search(String query) throws SQLException {
        List<Servidor> list = new ArrayList<>();
        String sql = "SELECT * FROM servidores WHERE nombre LIKE ? OR ip LIKE ? OR tipo LIKE ? OR ambiente LIKE ? ORDER BY nombre";
        String q = "%" + query + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, q); ps.setString(2, q);
            ps.setString(3, q); ps.setString(4, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(Servidor s) throws SQLException {
        String sql = "INSERT INTO servidores (nombre, ip, tipo, ambiente, sistema_operativo, descripcion, usuario_acceso, puerto, estado, notas) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, s.getNombre()); ps.setString(2, s.getIp());
            ps.setString(3, s.getTipo()); ps.setString(4, s.getAmbiente());
            ps.setString(5, s.getSistemaOperativo()); ps.setString(6, s.getDescripcion());
            ps.setString(7, s.getUsuarioAcceso()); ps.setString(8, s.getPuerto());
            ps.setString(9, s.getEstado()); ps.setString(10, s.getNotas());
            ps.executeUpdate();
        }
    }

    public void update(Servidor s) throws SQLException {
        String sql = "UPDATE servidores SET nombre=?, ip=?, tipo=?, ambiente=?, sistema_operativo=?, descripcion=?, usuario_acceso=?, puerto=?, estado=?, notas=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, s.getNombre()); ps.setString(2, s.getIp());
            ps.setString(3, s.getTipo()); ps.setString(4, s.getAmbiente());
            ps.setString(5, s.getSistemaOperativo()); ps.setString(6, s.getDescripcion());
            ps.setString(7, s.getUsuarioAcceso()); ps.setString(8, s.getPuerto());
            ps.setString(9, s.getEstado()); ps.setString(10, s.getNotas());
            ps.setInt(11, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM servidores WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    private Servidor map(ResultSet rs) throws SQLException {
        Servidor s = new Servidor();
        s.setId(rs.getInt("id")); s.setNombre(rs.getString("nombre"));
        s.setIp(rs.getString("ip")); s.setTipo(rs.getString("tipo"));
        s.setAmbiente(rs.getString("ambiente")); s.setSistemaOperativo(rs.getString("sistema_operativo"));
        s.setDescripcion(rs.getString("descripcion")); s.setUsuarioAcceso(rs.getString("usuario_acceso"));
        s.setPuerto(rs.getString("puerto")); s.setEstado(rs.getString("estado"));
        s.setNotas(rs.getString("notas"));
        return s;
    }
}
