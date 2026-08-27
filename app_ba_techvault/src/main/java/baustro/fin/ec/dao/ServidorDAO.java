package baustro.fin.ec.dao;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.model.Servidor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServidorDAO {

    private Connection getConn() { return DatabaseManager.getInstance().getConnection(); }

    /**
     * Garantiza que la columna contrasena_enc exista en la tabla servidores.
     * Se llama una vez al inicio para migración segura sin romper instalaciones existentes.
     */
    public void ensurePasswordColumn() {
        try (Statement st = getConn().createStatement()) {
            st.executeUpdate(
                "ALTER TABLE servidores ADD COLUMN contrasena_enc TEXT"
            );
        } catch (SQLException e) {
            // Columna ya existe — ignorar el error "duplicate column"
        }
    }

    public List<Servidor> findAll() throws SQLException {
        ensurePasswordColumn();
        List<Servidor> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM servidores ORDER BY host")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void insert(Servidor s) throws SQLException {
        String sql = "INSERT INTO servidores (host, ip, tipo, ambiente, sistema_operativo, descripcion, usuario_acceso, contrasena_enc, puerto, estado, notas) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1,  s.getHost());
            ps.setString(2,  s.getIp());
            ps.setString(3,  s.getTipo());
            ps.setString(4,  s.getAmbiente());
            ps.setString(5,  s.getSistemaOperativo());
            ps.setString(6,  s.getDescripcion());
            ps.setString(7,  s.getUsuarioAcceso());
            ps.setString(8,  s.getContrasenaEncriptada());
            ps.setString(9,  s.getPuerto());
            ps.setString(10, s.getEstado());
            ps.setString(11, s.getNotas());
            ps.executeUpdate();
        }
    }

    public void update(Servidor s) throws SQLException {
        String sql = "UPDATE servidores SET host=?, ip=?, tipo=?, ambiente=?, sistema_operativo=?, descripcion=?, usuario_acceso=?, contrasena_enc=?, puerto=?, estado=?, notas=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1,  s.getHost());
            ps.setString(2,  s.getIp());
            ps.setString(3,  s.getTipo());
            ps.setString(4,  s.getAmbiente());
            ps.setString(5,  s.getSistemaOperativo());
            ps.setString(6,  s.getDescripcion());
            ps.setString(7,  s.getUsuarioAcceso());
            ps.setString(8,  s.getContrasenaEncriptada());
            ps.setString(9,  s.getPuerto());
            ps.setString(10, s.getEstado());
            ps.setString(11, s.getNotas());
            ps.setInt(12,    s.getId());
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
        s.setId(rs.getInt("id"));
        s.setHost(rs.getString("host"));
        s.setIp(rs.getString("ip"));
        s.setTipo(rs.getString("tipo"));
        s.setAmbiente(rs.getString("ambiente"));
        s.setSistemaOperativo(rs.getString("sistema_operativo"));
        s.setDescripcion(rs.getString("descripcion"));
        s.setUsuarioAcceso(rs.getString("usuario_acceso"));
        s.setPuerto(rs.getString("puerto"));
        s.setEstado(rs.getString("estado"));
        s.setNotas(rs.getString("notas"));
        // contrasena_enc puede no existir en DBs viejas — capturar con try
        try { s.setContrasenaEncriptada(rs.getString("contrasena_enc")); }
        catch (SQLException ignored) {}
        return s;
    }
}
