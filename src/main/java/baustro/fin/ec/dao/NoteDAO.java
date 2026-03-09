package baustro.fin.ec.dao;

import baustro.fin.ec.database.DBConnection;
import baustro.fin.ec.model.Note;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {

    public List<Note> findAll() {
        List<Note> list = new ArrayList<Note>();
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM notes ORDER BY updated_at DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
            rs.close(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Note> search(String query) {
        List<Note> list = new ArrayList<Note>();
        String sql = "SELECT * FROM notes WHERE title LIKE ? OR content LIKE ? OR tags LIKE ? ORDER BY updated_at DESC";
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

    public void save(Note n) {
        String sql = "INSERT INTO notes (title, content, category, tags) VALUES (?,?,?,?)";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, n.getTitle()); ps.setString(2, n.getContent());
            ps.setString(3, n.getCategory()); ps.setString(4, n.getTags());
            ps.executeUpdate(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update(Note n) {
        String sql = "UPDATE notes SET title=?, content=?, category=?, tags=?, updated_at=datetime('now','localtime') WHERE id=?";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, n.getTitle()); ps.setString(2, n.getContent());
            ps.setString(3, n.getCategory()); ps.setString(4, n.getTags());
            ps.setInt(5, n.getId());
            ps.executeUpdate(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement("DELETE FROM notes WHERE id=?");
            ps.setInt(1, id); ps.executeUpdate(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Note map(ResultSet rs) throws SQLException {
        Note n = new Note();
        n.setId(rs.getInt("id")); n.setTitle(rs.getString("title"));
        n.setContent(rs.getString("content")); n.setCategory(rs.getString("category"));
        n.setTags(rs.getString("tags")); n.setCreatedAt(rs.getString("created_at"));
        n.setUpdatedAt(rs.getString("updated_at"));
        return n;
    }
}
