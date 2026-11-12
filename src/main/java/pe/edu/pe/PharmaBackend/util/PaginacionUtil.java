package pe.edu.pe.PharmaBackend.util;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import pe.edu.pe.PharmaBackend.exception.ReglaNegocioException;

public final class PaginacionUtil {
    private PaginacionUtil() { }

    public static PageRequest construir(int pagina, int tamanio, String campo, String direccion,
            Set<String> permitidos, String defecto) {
        if (pagina < 0 || tamanio < 1 || tamanio > 100) {
            throw new ReglaNegocioException("Página desde 0 y tamaño entre 1 y 100");
        }
        String orden = campo == null || campo.isBlank() ? defecto : campo.trim();
        if (!permitidos.contains(orden)) {
            throw new ReglaNegocioException("Campo de ordenamiento no permitido");
        }
        String sentido = direccion == null ? "asc" : direccion.trim();
        if (!Set.of("asc", "desc").contains(sentido.toLowerCase(java.util.Locale.ROOT))) {
            throw new ReglaNegocioException("La dirección debe ser asc o desc");
        }
        Sort sort = Sort.by(Sort.Direction.fromString(sentido), orden);
        if (!"id".equals(orden)) sort = sort.and(Sort.by("id"));
        return PageRequest.of(pagina, tamanio, sort);
    }
}
