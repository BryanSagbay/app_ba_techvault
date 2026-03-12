package baustro.fin.ec.api_techvault.controller;

import baustro.fin.ec.api_techvault.service.CajaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cajas")
public class CajasController {

    private final CajaService service;

    public CajasController(CajaService service) {
        this.service = service;
    }

    @GetMapping("/abiertas")
    public List<Map<String,Object>> abiertas(){
        return service.cajasAbiertas();
    }

    @GetMapping("/cerradas")
    public List<Map<String,Object>> cerradas(){
        return service.cajasCerradas();
    }
}