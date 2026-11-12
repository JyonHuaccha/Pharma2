package pe.edu.pe.PharmaBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductoResponseDTO implements Serializable {
    private static final long serialVersionUID = 2L;
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int stock;
    private Long categoriaId;
    private String categoriaNombre;
    private boolean estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    private String creadoPor;
    private String modificadoPor;
}
