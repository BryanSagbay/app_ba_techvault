package baustro.fin.ec.repository;

import baustro.fin.ec.model.Comando;
import baustro.fin.ec.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComandoRepository {

    private static ComandoRepository instance;
    public static ComandoRepository getInstance() {
        if (instance == null) instance = new ComandoRepository();
        return instance;
    }

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<Comando> findAll() throws SQLException {
        List<Comando> list = new ArrayList<>();
        String sql = "SELECT * FROM comandos ORDER BY favorito DESC, categoria, titulo";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Comando> search(String texto, String categoria) throws SQLException {
        List<Comando> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT * FROM comandos WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            sb.append("AND (titulo LIKE ? OR comando LIKE ? OR descripcion LIKE ? OR tags LIKE ?) ");
            String like = "%" + texto.trim() + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }
        if (categoria != null && !categoria.equals("TODAS")) {
            sb.append("AND categoria = ? ");
            params.add(categoria);
        }
        sb.append("ORDER BY favorito DESC, categoria, titulo");

        try (PreparedStatement ps = conn().prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void save(Comando c) throws SQLException {
        if (c.getId() == 0) insert(c); else update(c);
    }

    private void insert(Comando c) throws SQLException {
        String sql = "INSERT INTO comandos(titulo,comando,descripcion,categoria,so,tags,favorito) " +
                     "VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, c);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) c.setId(keys.getInt(1));
        }
    }

    private void update(Comando c) throws SQLException {
        String sql = "UPDATE comandos SET titulo=?,comando=?,descripcion=?,categoria=?,so=?," +
                     "tags=?,favorito=?,actualizado_en=datetime('now') WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            setParams(ps, c);
            ps.setInt(8, c.getId());
            ps.executeUpdate();
        }
    }

    private void setParams(PreparedStatement ps, Comando c) throws SQLException {
        ps.setString(1, c.getTitulo());
        ps.setString(2, c.getComando());
        ps.setString(3, c.getDescripcion());
        ps.setString(4, c.getCategoria());
        ps.setString(5, c.getSo());
        ps.setString(6, c.getTags());
        ps.setInt(7, c.isFavorito() ? 1 : 0);
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM comandos WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Comando map(ResultSet rs) throws SQLException {
        Comando c = new Comando();
        c.setId(rs.getInt("id"));
        c.setTitulo(rs.getString("titulo"));
        c.setComando(rs.getString("comando"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setCategoria(rs.getString("categoria"));
        c.setSo(rs.getString("so"));
        c.setTags(rs.getString("tags"));
        c.setFavorito(rs.getInt("favorito") == 1);
        c.setCreadoEn(rs.getString("creado_en"));
        c.setActualizadoEn(rs.getString("actualizado_en"));
        return c;
    }
}
