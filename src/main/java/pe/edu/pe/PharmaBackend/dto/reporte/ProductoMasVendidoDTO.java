package pe.edu.pe.PharmaBackend.dto.reporte;

import java.math.BigDecimal;

public record ProductoMasVendidoDTO( Long id,
                                     String name,
                                     String categoria,
                                     Long cantidad,
                                     BigDecimal total) {
}
