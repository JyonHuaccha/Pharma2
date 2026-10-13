package pe.edu.pe.PharmaBackend.service.impl;

import org.junit.jupiter.api.Test;
import pe.edu.pe.PharmaBackend.dto.reporte.VentaPorCategoriaDTO;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ReporteContratoTest {
    @Test void elJsonDelReporteCoincideConElModeloAngularDeLaGuia() {
        String json = JsonMapper.builder().build().writeValueAsString(
                new VentaPorCategoriaDTO(1L, "Salud", 3L, new BigDecimal("37.50")));
        assertTrue(json.contains("\"categoriaId\":1"));
        assertTrue(json.contains("\"categoriaNombre\":\"Salud\""));
        assertTrue(json.contains("\"cantidadVendida\":3"));
        assertTrue(json.contains("\"montoTotal\":37.50"));
        assertFalse(json.contains("\"categoria\":"));
        assertFalse(json.contains("\"cantidad\":"));
        assertFalse(json.contains("\"total\":"));
    }
}
