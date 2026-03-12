package baustro.fin.ec.dao;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.model.Contrasena;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContrasenaDAO {

    private Connection getConn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Contrasena> findAll() throws SQLException {
        List<Contrasena> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM contrasenas ORDER BY categoria, titulo")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Contrasena> search(String query) throws SQLException {
        List<Contrasena> list = new ArrayList<>();
        String sql = "SELECT * FROM contrasenas WHERE titulo LIKE ? OR usuario LIKE ? OR categoria LIKE ? ORDER BY titulo";
        String q = "%" + query + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(Contrasena c) throws SQLException {
        String sql = "INSERT INTO contrasenas (titulo, usuario, contrasena_enc, url, categoria, notas) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, c.getTitulo()); ps.setString(2, c.getUsuario());
            ps.setString(3, c.getContrasenaCifrada()); ps.setString(4, c.getUrl());
            ps.setString(5, c.getCategoria()); ps.setString(6, c.getNotas());
            ps.executeUpdate();
        }
    }

    public void update(Contrasena c) throws SQLException {
        String sql = "UPDATE contrasenas SET titulo=?, usuario=?, contrasena_enc=?, url=?, categoria=?, notas=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, c.getTitulo()); ps.setString(2, c.getUsuario());
            ps.setString(3, c.getContrasenaCifrada()); ps.setString(4, c.getUrl());
            ps.setString(5, c.getCategoria()); ps.setString(6, c.getNotas());
            ps.setInt(7, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM contrasenas WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    private Contrasena map(ResultSet rs) throws SQLException {
        Contrasena c = new Contrasena();
        c.setId(rs.getInt("id")); c.setTitulo(rs.getString("titulo"));
        c.setUsuario(rs.getString("usuario")); c.setContrasenaCifrada(rs.getString("contrasena_enc"));
        c.setUrl(rs.getString("url")); c.setCategoria(rs.getString("categoria"));
        c.setNotas(rs.getString("notas"));
        return c;
    }
}
