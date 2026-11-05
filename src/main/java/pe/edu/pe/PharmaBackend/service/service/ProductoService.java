package pe.edu.pe.PharmaBackend.service.service;

import pe.edu.pe.PharmaBackend.dto.ProductoRequestDTO;
import pe.edu.pe.PharmaBackend.dto.ProductoResponseDTO;
import pe.edu.pe.PharmaBackend.dto.PaginaResponseDTO;
import pe.edu.pe.PharmaBackend.service.generic.CrudService;
public interface ProductoService extends CrudService<ProductoRequestDTO, ProductoResponseDTO, Long> {
    PaginaResponseDTO<ProductoResponseDTO> listar(int pagina, int tamanio, String ordenarPor,
                                                String direccion, Long categoriaId);
}
