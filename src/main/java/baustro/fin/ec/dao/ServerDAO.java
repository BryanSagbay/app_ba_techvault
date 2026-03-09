package baustro.fin.ec.dao;

import baustro.fin.ec.database.DBConnection;
import baustro.fin.ec.model.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServerDAO {

    public List<Server> findAll() {
        List<Server> list = new ArrayList<Server>();
        String sql = "SELECT * FROM servers ORDER BY name";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
            rs.close(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Server> search(String query) {
        List<Server> list = new ArrayList<Server>();
        String sql = "SELECT * FROM servers WHERE name LIKE ? OR ip LIKE ? OR tags LIKE ? ORDER BY name";
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

    public void save(Server s) {
        String sql = "INSERT INTO servers (name, ip, environment, os, description, ssh_user, ssh_port, tags, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, s.getName());
            ps.setString(2, s.getIp());
            ps.setString(3, s.getEnvironment());
            ps.setString(4, s.getOs());
            ps.setString(5, s.getDescription());
            ps.setString(6, s.getSshUser());
            ps.setInt(7, s.getSshPort());
            ps.setString(8, s.getTags());
            ps.setString(9, s.getStatus());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update(Server s) {
        String sql = "UPDATE servers SET name=?, ip=?, environment=?, os=?, description=?, ssh_user=?, ssh_port=?, tags=?, status=?, updated_at=datetime('now','localtime') WHERE id=?";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, s.getName());
            ps.setString(2, s.getIp());
            ps.setString(3, s.getEnvironment());
            ps.setString(4, s.getOs());
            ps.setString(5, s.getDescription());
            ps.setString(6, s.getSshUser());
            ps.setInt(7, s.getSshPort());
            ps.setString(8, s.getTags());
            ps.setString(9, s.getStatus());
            ps.setInt(10, s.getId());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement("DELETE FROM servers WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public int count() {
        try {
            Connection c = DBConnection.connect();
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM servers");
            int n = rs.getInt(1);
            rs.close(); s.close();
            return n;
        } catch (Exception e) { return 0; }
    }

    private Server map(ResultSet rs) throws SQLException {
        return new Server(
            rs.getInt("id"), rs.getString("name"), rs.getString("ip"),
            rs.getString("environment"), rs.getString("os"), rs.getString("description"),
            rs.getString("ssh_user"), rs.getInt("ssh_port"), rs.getString("tags"),
            rs.getString("status"), rs.getString("created_at")
        );
    }
}
