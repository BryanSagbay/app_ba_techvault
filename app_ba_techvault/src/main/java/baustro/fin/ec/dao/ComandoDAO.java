package baustro.fin.ec.dao;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.model.Comando;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComandoDAO {

    private Connection getConn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Comando> findAll() throws SQLException {
        List<Comando> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM comandos ORDER BY categoria, titulo")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Comando> search(String query) throws SQLException {
        List<Comando> list = new ArrayList<>();
        String sql = "SELECT * FROM comandos WHERE titulo LIKE ? OR comando LIKE ? OR descripcion LIKE ? OR categoria LIKE ? ORDER BY categoria, titulo";
        String q = "%" + query + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q); ps.setString(4, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(Comando c) throws SQLException {
        String sql = "INSERT INTO comandos (titulo, comando, descripcion, categoria, sistema_operativo) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, c.getTitulo()); ps.setString(2, c.getComando());
            ps.setString(3, c.getDescripcion()); ps.setString(4, c.getCategoria());
            ps.setString(5, c.getSistemaOperativo());
            ps.executeUpdate();
        }
    }

    public void update(Comando c) throws SQLException {
        String sql = "UPDATE comandos SET titulo=?, comando=?, descripcion=?, categoria=?, sistema_operativo=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, c.getTitulo()); ps.setString(2, c.getComando());
            ps.setString(3, c.getDescripcion()); ps.setString(4, c.getCategoria());
            ps.setString(5, c.getSistemaOperativo()); ps.setInt(6, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM comandos WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    private Comando map(ResultSet rs) throws SQLException {
        Comando c = new Comando();
        c.setId(rs.getInt("id")); c.setTitulo(rs.getString("titulo"));
        c.setComando(rs.getString("comando")); c.setDescripcion(rs.getString("descripcion"));
        c.setCategoria(rs.getString("categoria")); c.setSistemaOperativo(rs.getString("sistema_operativo"));
        return c;
    }
}
