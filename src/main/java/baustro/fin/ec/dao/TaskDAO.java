package baustro.fin.ec.dao;

import baustro.fin.ec.database.DBConnection;
import baustro.fin.ec.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public List<Task> findAll() {
        List<Task> list = new ArrayList<Task>();
        String sql = "SELECT * FROM tasks ORDER BY CASE priority WHEN 'ALTA' THEN 1 WHEN 'MEDIA' THEN 2 ELSE 3 END, created_at DESC";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
            rs.close(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void save(Task t) {
        String sql = "INSERT INTO tasks (title, description, priority, status, due_date, tags) VALUES (?,?,?,?,?,?)";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, t.getTitle()); ps.setString(2, t.getDescription());
            ps.setString(3, t.getPriority()); ps.setString(4, t.getStatus());
            ps.setString(5, t.getDueDate()); ps.setString(6, t.getTags());
            ps.executeUpdate(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update(Task t) {
        String sql = "UPDATE tasks SET title=?, description=?, priority=?, status=?, due_date=?, tags=?, updated_at=datetime('now','localtime') WHERE id=?";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, t.getTitle()); ps.setString(2, t.getDescription());
            ps.setString(3, t.getPriority()); ps.setString(4, t.getStatus());
            ps.setString(5, t.getDueDate()); ps.setString(6, t.getTags());
            ps.setInt(7, t.getId());
            ps.executeUpdate(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement("DELETE FROM tasks WHERE id=?");
            ps.setInt(1, id); ps.executeUpdate(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public int countByStatus(String status) {
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM tasks WHERE status=?");
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            int n = rs.getInt(1);
            rs.close(); ps.close();
            return n;
        } catch (Exception e) { return 0; }
    }

    private Task map(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id")); t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description")); t.setPriority(rs.getString("priority"));
        t.setStatus(rs.getString("status")); t.setDueDate(rs.getString("due_date"));
        t.setTags(rs.getString("tags")); t.setCreatedAt(rs.getString("created_at"));
        return t;
    }
}
