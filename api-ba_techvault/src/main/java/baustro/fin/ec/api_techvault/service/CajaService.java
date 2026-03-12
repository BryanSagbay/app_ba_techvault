package baustro.fin.ec.api_techvault.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CajaService {

    private final JdbcTemplate jdbc;

    public CajaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // SELECT 1 → cajas abiertas
    public List<Map<String,Object>> cajasAbiertas(){

        String sql = """
        consulta1
        """;

        return jdbc.queryForList(sql);
    }

    // SELECT 2 → cajas cerradas
    public List<Map<String,Object>> cajasCerradas(){

        String sql = """
        consulta2
        """;

        return jdbc.queryForList(sql);
    }
}
