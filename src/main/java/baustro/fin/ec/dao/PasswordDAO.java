package baustro.fin.ec.dao;

import baustro.fin.ec.database.DBConnection;
import baustro.fin.ec.model.Password;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PasswordDAO {

    public List<Password> findAll() {
        List<Password> list = new ArrayList<Password>();
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM passwords ORDER BY service_name");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
            rs.close(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Password> search(String query) {
        List<Password> list = new ArrayList<Password>();
        String sql = "SELECT * FROM passwords WHERE service_name LIKE ? OR username LIKE ? OR category LIKE ? ORDER BY service_name";
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

    public void save(Password p) {
        String sql = "INSERT INTO passwords (service_name, username, password_encrypted, url, notes, category) VALUES (?,?,?,?,?,?)";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, p.getServiceName()); ps.setString(2, p.getUsername());
            ps.setString(3, p.getPasswordEncrypted()); ps.setString(4, p.getUrl());
            ps.setString(5, p.getNotes()); ps.setString(6, p.getCategory());
            ps.executeUpdate(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update(Password p) {
        String sql = "UPDATE passwords SET service_name=?, username=?, password_encrypted=?, url=?, notes=?, category=?, updated_at=datetime('now','localtime') WHERE id=?";
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, p.getServiceName()); ps.setString(2, p.getUsername());
            ps.setString(3, p.getPasswordEncrypted()); ps.setString(4, p.getUrl());
            ps.setString(5, p.getNotes()); ps.setString(6, p.getCategory());
            ps.setInt(7, p.getId());
            ps.executeUpdate(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        try {
            Connection c = DBConnection.connect();
            PreparedStatement ps = c.prepareStatement("DELETE FROM passwords WHERE id=?");
            ps.setInt(1, id); ps.executeUpdate(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Password map(ResultSet rs) throws SQLException {
        Password p = new Password();
        p.setId(rs.getInt("id")); p.setServiceName(rs.getString("service_name"));
        p.setUsername(rs.getString("username")); p.setPasswordEncrypted(rs.getString("password_encrypted"));
        p.setUrl(rs.getString("url")); p.setNotes(rs.getString("notes"));
        p.setCategory(rs.getString("category")); p.setCreatedAt(rs.getString("created_at"));
        return p;
    }
}
