package pe.edu.pe.PharmaBackend.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoUsuarioRequestDTO {
    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
