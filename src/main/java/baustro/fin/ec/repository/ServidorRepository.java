package baustro.fin.ec.repository;

import baustro.fin.ec.model.Servidor;
import baustro.fin.ec.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServidorRepository {

    private static ServidorRepository instance;
    public static ServidorRepository getInstance() {
        if (instance == null) instance = new ServidorRepository();
        return instance;
    }

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<Servidor> findAll() throws SQLException {
        List<Servidor> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM servidores ORDER BY ambiente, nombre")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Servidor> search(String texto, String ambiente) throws SQLException {
        List<Servidor> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT * FROM servidores WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            sb.append("AND (nombre LIKE ? OR ip LIKE ? OR hostname LIKE ? OR rol LIKE ?) ");
            String like = "%" + texto.trim() + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }
        if (ambiente != null && !ambiente.equals("TODOS")) {
            sb.append("AND ambiente = ? ");
            params.add(ambiente);
        }
        sb.append("ORDER BY ambiente, nombre");

        try (PreparedStatement ps = conn().prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Servidor findById(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM servidores WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public void save(Servidor s) throws SQLException {
        if (s.getId() == 0) insert(s); else update(s);
    }

    private void insert(Servidor s) throws SQLException {
        String sql = "INSERT INTO servidores(nombre,ip,hostname,so,rol,ambiente," +
                     "descripcion,puerto_ssh,usuario,notas,activo) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, s);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) s.setId(keys.getInt(1));
        }
    }

    private void update(Servidor s) throws SQLException {
        String sql = "UPDATE servidores SET nombre=?,ip=?,hostname=?,so=?,rol=?,ambiente=?," +
                     "descripcion=?,puerto_ssh=?,usuario=?,notas=?,activo=?," +
                     "actualizado_en=datetime('now') WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            setParams(ps, s);
            ps.setInt(12, s.getId());
            ps.executeUpdate();
        }
    }

    private void setParams(PreparedStatement ps, Servidor s) throws SQLException {
        ps.setString(1, s.getNombre());
        ps.setString(2, s.getIp());
        ps.setString(3, s.getHostname());
        ps.setString(4, s.getSo());
        ps.setString(5, s.getRol());
        ps.setString(6, s.getAmbiente());
        ps.setString(7, s.getDescripcion());
        ps.setString(8, s.getPuertoSsh());
        ps.setString(9, s.getUsuario());
        ps.setString(10, s.getNotas());
        ps.setInt(11, s.isActivo() ? 1 : 0);
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM servidores WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Servidor map(ResultSet rs) throws SQLException {
        Servidor s = new Servidor();
        s.setId(rs.getInt("id"));
        s.setNombre(rs.getString("nombre"));
        s.setIp(rs.getString("ip"));
        s.setHostname(rs.getString("hostname"));
        s.setSo(rs.getString("so"));
        s.setRol(rs.getString("rol"));
        s.setAmbiente(rs.getString("ambiente"));
        s.setDescripcion(rs.getString("descripcion"));
        s.setPuertoSsh(rs.getString("puerto_ssh"));
        s.setUsuario(rs.getString("usuario"));
        s.setNotas(rs.getString("notas"));
        s.setActivo(rs.getInt("activo") == 1);
        s.setCreadoEn(rs.getString("creado_en"));
        s.setActualizadoEn(rs.getString("actualizado_en"));
        return s;
    }
}
