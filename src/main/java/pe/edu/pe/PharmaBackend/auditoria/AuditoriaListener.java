package pe.edu.pe.PharmaBackend.auditoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.edu.pe.PharmaBackend.service.service.AuditoriaService;

@Component
public class AuditoriaListener {
    private static final Logger log = LoggerFactory.getLogger(AuditoriaListener.class);
    private final AuditoriaService auditoria;

    public AuditoriaListener(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alConfirmar(EventoAuditoria evento) {
        try {
            auditoria.registrar(evento);
        } catch (RuntimeException ex) {
            log.error("No se pudo auditar {} {} | tipo={}", evento.accion(), evento.entidadId(),
                    ex.getClass().getSimpleName());
        }
    }
}
