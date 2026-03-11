package baustro.fin.ec.repository;

import baustro.fin.ec.model.Incidencia;
import baustro.fin.ec.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncidenciaRepository {

    private static IncidenciaRepository instance;
    public static IncidenciaRepository getInstance() {
        if (instance == null) instance = new IncidenciaRepository();
        return instance;
    }

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<Incidencia> findAll() throws SQLException {
        List<Incidencia> list = new ArrayList<>();
        String sql = "SELECT * FROM incidencias ORDER BY creado_en DESC";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Incidencia> search(String texto, String estado, String prioridad) throws SQLException {
        List<Incidencia> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(
            "SELECT * FROM incidencias WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            sb.append("AND (titulo LIKE ? OR numero LIKE ? OR descripcion LIKE ? OR servicio LIKE ?) ");
            String like = "%" + texto.trim() + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }
        if (estado != null && !estado.equals("TODOS")) {
            sb.append("AND estado = ? ");
            params.add(estado);
        }
        if (prioridad != null && !prioridad.equals("TODAS")) {
            sb.append("AND prioridad = ? ");
            params.add(prioridad);
        }
        sb.append("ORDER BY creado_en DESC");

        try (PreparedStatement ps = conn().prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Incidencia findById(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM incidencias WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public void save(Incidencia i) throws SQLException {
        if (i.getId() == 0) insert(i); else update(i);
    }

    private void insert(Incidencia i) throws SQLException {
        String sql = "INSERT INTO incidencias(numero,titulo,descripcion,servicio,solucion," +
                     "estado,prioridad,fecha_inicio,fecha_cierre,tags) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, i.getNumero());
            ps.setString(2, i.getTitulo());
            ps.setString(3, i.getDescripcion());
            ps.setString(4, i.getServicio());
            ps.setString(5, i.getSolucion());
            ps.setString(6, i.getEstado());
            ps.setString(7, i.getPrioridad());
            ps.setString(8, i.getFechaInicio());
            ps.setString(9, i.getFechaCierre());
            ps.setString(10, i.getTags());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) i.setId(keys.getInt(1));
        }
    }

    private void update(Incidencia i) throws SQLException {
        String sql = "UPDATE incidencias SET numero=?,titulo=?,descripcion=?,servicio=?," +
                     "solucion=?,estado=?,prioridad=?,fecha_inicio=?,fecha_cierre=?,tags=?," +
                     "actualizado_en=datetime('now') WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, i.getNumero());
            ps.setString(2, i.getTitulo());
            ps.setString(3, i.getDescripcion());
            ps.setString(4, i.getServicio());
            ps.setString(5, i.getSolucion());
            ps.setString(6, i.getEstado());
            ps.setString(7, i.getPrioridad());
            ps.setString(8, i.getFechaInicio());
            ps.setString(9, i.getFechaCierre());
            ps.setString(10, i.getTags());
            ps.setInt(11, i.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM incidencias WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Incidencia map(ResultSet rs) throws SQLException {
        Incidencia i = new Incidencia();
        i.setId(rs.getInt("id"));
        i.setNumero(rs.getString("numero"));
        i.setTitulo(rs.getString("titulo"));
        i.setDescripcion(rs.getString("descripcion"));
        i.setServicio(rs.getString("servicio"));
        i.setSolucion(rs.getString("solucion"));
        i.setEstado(rs.getString("estado"));
        i.setPrioridad(rs.getString("prioridad"));
        i.setFechaInicio(rs.getString("fecha_inicio"));
        i.setFechaCierre(rs.getString("fecha_cierre"));
        i.setTags(rs.getString("tags"));
        i.setCreadoEn(rs.getString("creado_en"));
        i.setActualizadoEn(rs.getString("actualizado_en"));
        return i;
    }
}
