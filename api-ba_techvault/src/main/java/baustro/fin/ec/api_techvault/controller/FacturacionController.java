package baustro.fin.ec.api_techvault.controller;

import baustro.fin.ec.api_techvault.service.CajaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturacion")
public class FacturacionController {

    private final CajaService service;

    public FacturacionController(CajaService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    public List<Map<String, Object>> resumen() {
        return service.resumenFE();
    }
}