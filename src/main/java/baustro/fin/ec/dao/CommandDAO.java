package baustro.fin.ec.dao;

import baustro.fin.ec.database.DBConnection;
import baustro.fin.ec.model.Command;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandDAO {

    public List<Command> findAll() {
        List<Command> list = new ArrayList<Command>();
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM commands ORDER BY category, title");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
            rs.close(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Command> search(String query) {
        List<Command> list = new ArrayList<Command>();
        String sql = "SELECT * FROM commands WHERE title LIKE ? OR command LIKE ? OR category LIKE ? OR tags LIKE ? ORDER BY category, title";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            String q = "%" + query + "%";
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q); ps.setString(4, q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
            rs.close(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void save(Command cmd) {
        String sql = "INSERT INTO commands (title, command, description, category, os, tags) VALUES (?,?,?,?,?,?)";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, cmd.getTitle()); ps.setString(2, cmd.getCommand());
            ps.setString(3, cmd.getDescription()); ps.setString(4, cmd.getCategory());
            ps.setString(5, cmd.getOs()); ps.setString(6, cmd.getTags());
            ps.executeUpdate(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update(Command cmd) {
        String sql = "UPDATE commands SET title=?, command=?, description=?, category=?, os=?, tags=? WHERE id=?";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, cmd.getTitle()); ps.setString(2, cmd.getCommand());
            ps.setString(3, cmd.getDescription()); ps.setString(4, cmd.getCategory());
            ps.setString(5, cmd.getOs()); ps.setString(6, cmd.getTags());
            ps.setInt(7, cmd.getId());
            ps.executeUpdate(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement("DELETE FROM commands WHERE id=?");
            ps.setInt(1, id); ps.executeUpdate(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Command map(ResultSet rs) throws SQLException {
        Command cmd = new Command();
        cmd.setId(rs.getInt("id")); cmd.setTitle(rs.getString("title"));
        cmd.setCommand(rs.getString("command")); cmd.setDescription(rs.getString("description"));
        cmd.setCategory(rs.getString("category")); cmd.setOs(rs.getString("os"));
        cmd.setTags(rs.getString("tags")); cmd.setCreatedAt(rs.getString("created_at"));
        return cmd;
    }
}
