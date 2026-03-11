package baustro.fin.ec.repository;

import baustro.fin.ec.model.Contrasena;
import baustro.fin.ec.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContrasenaRepository {

    private static ContrasenaRepository instance;
    public static ContrasenaRepository getInstance() {
        if (instance == null) instance = new ContrasenaRepository();
        return instance;
    }

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<Contrasena> findAll() throws SQLException {
        List<Contrasena> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM contrasenas ORDER BY categoria, titulo")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Contrasena> search(String texto, String categoria) throws SQLException {
        List<Contrasena> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT * FROM contrasenas WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            sb.append("AND (titulo LIKE ? OR usuario LIKE ? OR url LIKE ?) ");
            String like = "%" + texto.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (categoria != null && !categoria.equals("TODAS")) {
            sb.append("AND categoria = ? ");
            params.add(categoria);
        }
        sb.append("ORDER BY categoria, titulo");

        try (PreparedStatement ps = conn().prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void save(Contrasena c) throws SQLException {
        if (c.getId() == 0) insert(c); else update(c);
    }

    private void insert(Contrasena c) throws SQLException {
        String sql = "INSERT INTO contrasenas(titulo,usuario,password,url,categoria,servidor_id,notas) " +
                     "VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, c);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) c.setId(keys.getInt(1));
        }
    }

    private void update(Contrasena c) throws SQLException {
        String sql = "UPDATE contrasenas SET titulo=?,usuario=?,password=?,url=?,categoria=?," +
                     "servidor_id=?,notas=?,actualizado_en=datetime('now') WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            setParams(ps, c);
            ps.setInt(8, c.getId());
            ps.executeUpdate();
        }
    }

    private void setParams(PreparedStatement ps, Contrasena c) throws SQLException {
        ps.setString(1, c.getTitulo());
        ps.setString(2, c.getUsuario());
        ps.setString(3, c.getPasswordCifrada());
        ps.setString(4, c.getUrl());
        ps.setString(5, c.getCategoria());
        if (c.getServidorId() != null) ps.setInt(6, c.getServidorId());
        else ps.setNull(6, Types.INTEGER);
        ps.setString(7, c.getNotas());
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM contrasenas WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Contrasena map(ResultSet rs) throws SQLException {
        Contrasena c = new Contrasena();
        c.setId(rs.getInt("id"));
        c.setTitulo(rs.getString("titulo"));
        c.setUsuario(rs.getString("usuario"));
        c.setPasswordCifrada(rs.getString("password"));
        c.setUrl(rs.getString("url"));
        c.setCategoria(rs.getString("categoria"));
        int sid = rs.getInt("servidor_id");
        if (!rs.wasNull()) c.setServidorId(sid);
        c.setNotas(rs.getString("notas"));
        c.setCreadoEn(rs.getString("creado_en"));
        c.setActualizadoEn(rs.getString("actualizado_en"));
        return c;
    }
}
