package baustro.fin.ec.dao;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.model.Correctivo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CorrectivoDAO {

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<Correctivo> findAll() throws SQLException {
        List<Correctivo> list = new ArrayList<>();
        String sql = "SELECT * FROM correctivos ORDER BY created_at DESC";
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Correctivo> search(String query) throws SQLException {
        List<Correctivo> list = new ArrayList<>();
        String sql = "SELECT * FROM correctivos WHERE titulo LIKE ? OR numero_tarea LIKE ? OR servicio LIKE ? OR error_presentado LIKE ? ORDER BY created_at DESC";
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

    public void insert(Correctivo c) throws SQLException {
        String sql = """
            INSERT INTO correctivos (numero_tarea, titulo, descripcion, ambiente, servicio,
            error_presentado, solucion, estado, prioridad, fecha_reporte, fecha_solucion,
            responsable, observaciones) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, c.getNumeroTarea());
            ps.setString(2, c.getTitulo());
            ps.setString(3, c.getDescripcion());
            ps.setString(4, c.getAmbiente());
            ps.setString(5, c.getServicio());
            ps.setString(6, c.getErrorPresentado());
            ps.setString(7, c.getSolucion());
            ps.setString(8, c.getEstado());
            ps.setString(9, c.getPrioridad());
            ps.setString(10, c.getFechaReporte());
            ps.setString(11, c.getFechaSolucion());
            ps.setString(12, c.getResponsable());
            ps.setString(13, c.getObservaciones());
            ps.executeUpdate();
        }
    }

    public void update(Correctivo c) throws SQLException {
        String sql = """
            UPDATE correctivos SET numero_tarea=?, titulo=?, descripcion=?, ambiente=?, servicio=?,
            error_presentado=?, solucion=?, estado=?, prioridad=?, fecha_reporte=?, fecha_solucion=?,
            responsable=?, observaciones=?, updated_at=CURRENT_TIMESTAMP WHERE id=?
        """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, c.getNumeroTarea());
            ps.setString(2, c.getTitulo());
            ps.setString(3, c.getDescripcion());
            ps.setString(4, c.getAmbiente());
            ps.setString(5, c.getServicio());
            ps.setString(6, c.getErrorPresentado());
            ps.setString(7, c.getSolucion());
            ps.setString(8, c.getEstado());
            ps.setString(9, c.getPrioridad());
            ps.setString(10, c.getFechaReporte());
            ps.setString(11, c.getFechaSolucion());
            ps.setString(12, c.getResponsable());
            ps.setString(13, c.getObservaciones());
            ps.setInt(14, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM correctivos WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Correctivo map(ResultSet rs) throws SQLException {
        Correctivo c = new Correctivo();
        c.setId(rs.getInt("id"));
        c.setNumeroTarea(rs.getString("numero_tarea"));
        c.setTitulo(rs.getString("titulo"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setAmbiente(rs.getString("ambiente"));
        c.setServicio(rs.getString("servicio"));
        c.setErrorPresentado(rs.getString("error_presentado"));
        c.setSolucion(rs.getString("solucion"));
        c.setEstado(rs.getString("estado"));
        c.setPrioridad(rs.getString("prioridad"));
        c.setFechaReporte(rs.getString("fecha_reporte"));
        c.setFechaSolucion(rs.getString("fecha_solucion"));
        c.setResponsable(rs.getString("responsable"));
        c.setObservaciones(rs.getString("observaciones"));
        return c;
    }
}
