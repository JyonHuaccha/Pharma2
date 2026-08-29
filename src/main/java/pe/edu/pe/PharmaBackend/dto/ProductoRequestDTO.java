package pe.edu.pe.PharmaBackend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductoRequestDTO {
    @NotBlank(message = "El nombre del producto es obligatoria")
    @Size(
            min = 3,
            max = 50,
            message = "El nombre debe tener entre 3 y 50 caracteres"
    )
    private String nombre;
    @Size(
            max = 200,
            message = "La descripcion no debe superar los 200 caracteres"
    )
    private String descripcion;
    @Positive(message = "Debe ser positivo")
    private BigDecimal precio;
    @PositiveOrZero(message = "Debe ser positivo")
    private int stock;

    @NotNull(message = "La categoria es obligatoria")
    @Positive(message = "Debe ser positivo")
    private Long categoriaId;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
