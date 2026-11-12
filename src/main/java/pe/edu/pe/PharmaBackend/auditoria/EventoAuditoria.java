package pe.edu.pe.PharmaBackend.auditoria;

import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public record EventoAuditoria(String usuario, String accion, String entidad, Long entidadId,
        String detalle, String requestId) {
    public static EventoAuditoria de(String accion, String entidad, Long id, String detalle) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken
                ? "sistema" : auth.getName();
        return new EventoAuditoria(recortar(usuario, 50), accion, entidad, id,
                recortar(detalle, 500), MDC.get("requestId"));
    }

    public static EventoAuditoria login(String accion, String usuario) {
        return new EventoAuditoria(recortar(usuario, 50), accion, "Usuario", null, null, MDC.get("requestId"));
    }

    private static String recortar(String valor, int maximo) {
        return valor == null || valor.length() <= maximo ? valor : valor.substring(0, maximo);
    }
}
