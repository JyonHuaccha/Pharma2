package pe.edu.pe.PharmaBackend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.pe.PharmaBackend.auditoria.EventoAuditoria;
import pe.edu.pe.PharmaBackend.dto.AuditoriaEventoDTO;
import pe.edu.pe.PharmaBackend.dto.PaginaResponseDTO;
import pe.edu.pe.PharmaBackend.entity.AuditoriaEvento;
import pe.edu.pe.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.pe.PharmaBackend.repository.AuditoriaEventoRepository;
import pe.edu.pe.PharmaBackend.service.service.AuditoriaService;
import pe.edu.pe.PharmaBackend.util.PaginacionUtil;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {
    private final AuditoriaEventoRepository repository;

    public AuditoriaServiceImpl(AuditoriaEventoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(EventoAuditoria evento) {
        AuditoriaEvento a = new AuditoriaEvento();
        a.setFecha(LocalDateTime.now());
        a.setUsuario(evento.usuario());
        a.setAccion(evento.accion());
        a.setEntidad(evento.entidad());
        a.setEntidadId(evento.entidadId());
        a.setDetalle(evento.detalle());
        a.setRequestId(evento.requestId());
        repository.save(a);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponseDTO<AuditoriaEventoDTO> buscar(String usuario, String accion, LocalDate desde,
            LocalDate hasta, int pagina, int tamanio) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("El rango de fechas es inválido");
        }
        var pageable = PaginacionUtil.construir(pagina, tamanio, "fecha", "desc",
                Set.of("fecha", "usuario", "accion"), "fecha");
        return PaginaResponseDTO.de(repository.buscar(normalizar(usuario), normalizar(accion),
                desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay(), pageable).map(AuditoriaEventoDTO::de));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
