package pe.edu.pe.PharmaBackend.service.service;
import pe.edu.pe.PharmaBackend.dto.VentaRequestDTO;
import pe.edu.pe.PharmaBackend.dto.VentaResponseDTO;
import pe.edu.pe.PharmaBackend.enums.EstadoVenta;

import java.time.LocalDate;

import java.util.List;

public interface VentaService {
    pe.edu.pe.PharmaBackend.dto.PaginaResponseDTO<VentaResponseDTO> buscarPagina(
            Long clienteId, EstadoVenta estado, LocalDate desde, LocalDate hasta,
            String ordenarPor, String direccion, int pagina, int tamanio);
    VentaResponseDTO registrar(VentaRequestDTO request);
    VentaResponseDTO buscar(Long id);
    List<VentaResponseDTO> listar();
    List<VentaResponseDTO> buscar(
            Long clienteId,
            EstadoVenta estado,
            LocalDate desde,
            LocalDate hasta,
            String ordenarPor,
            String direccion);
}