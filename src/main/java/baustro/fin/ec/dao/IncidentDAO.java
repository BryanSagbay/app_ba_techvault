package baustro.fin.ec.dao;

import baustro.fin.ec.database.DBConnection;
import baustro.fin.ec.model.Incident;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncidentDAO {

    public List<Incident> findAll() {
        List<Incident> list = new ArrayList<Incident>();
        String sql = "SELECT i.*, s.name as server_name FROM incidents i LEFT JOIN servers s ON i.server_id = s.id ORDER BY i.created_at DESC";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
            rs.close(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Incident> search(String query) {
        List<Incident> list = new ArrayList<Incident>();
        String sql = "SELECT i.*, s.name as server_name FROM incidents i LEFT JOIN servers s ON i.server_id = s.id WHERE i.title LIKE ? OR i.ticket_number LIKE ? OR i.affected_service LIKE ? ORDER BY i.created_at DESC";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            String q = "%" + query + "%";
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
            rs.close(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void save(Incident inc) {
        String sql = "INSERT INTO incidents (ticket_number, title, description, solution, severity, status, server_id, affected_service, root_cause, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, inc.getTicketNumber());
            ps.setString(2, inc.getTitle());
            ps.setString(3, inc.getDescription());
            ps.setString(4, inc.getSolution());
            ps.setString(5, inc.getSeverity());
            ps.setString(6, inc.getStatus());
            ps.setInt(7, inc.getServerId());
            ps.setString(8, inc.getAffectedService());
            ps.setString(9, inc.getRootCause());
            ps.setString(10, inc.getStartDate());
            ps.setString(11, inc.getEndDate());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update(Incident inc) {
        String sql = "UPDATE incidents SET ticket_number=?, title=?, description=?, solution=?, severity=?, status=?, server_id=?, affected_service=?, root_cause=?, start_date=?, end_date=? WHERE id=?";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, inc.getTicketNumber());
            ps.setString(2, inc.getTitle());
            ps.setString(3, inc.getDescription());
            ps.setString(4, inc.getSolution());
            ps.setString(5, inc.getSeverity());
            ps.setString(6, inc.getStatus());
            ps.setInt(7, inc.getServerId());
            ps.setString(8, inc.getAffectedService());
            ps.setString(9, inc.getRootCause());
            ps.setString(10, inc.getStartDate());
            ps.setString(11, inc.getEndDate());
            ps.setInt(12, inc.getId());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement("DELETE FROM incidents WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public int countByStatus(String status) {
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM incidents WHERE status=?");
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            int n = rs.getInt(1);
            rs.close(); ps.close();
            return n;
        } catch (Exception e) { return 0; }
    }

    private Incident map(ResultSet rs) throws SQLException {
        Incident i = new Incident();
        i.setId(rs.getInt("id"));
        i.setTicketNumber(rs.getString("ticket_number"));
        i.setTitle(rs.getString("title"));
        i.setDescription(rs.getString("description"));
        i.setSolution(rs.getString("solution"));
        i.setSeverity(rs.getString("severity"));
        i.setStatus(rs.getString("status"));
        i.setServerId(rs.getInt("server_id"));
        i.setServerName(rs.getString("server_name"));
        i.setAffectedService(rs.getString("affected_service"));
        i.setRootCause(rs.getString("root_cause"));
        i.setStartDate(rs.getString("start_date"));
        i.setEndDate(rs.getString("end_date"));
        i.setCreatedAt(rs.getString("created_at"));
        return i;
    }
}
