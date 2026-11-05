package pe.edu.pe.PharmaBackend.config;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.*;

class RequestIdFilterTest {
    @Test
    void asignaIdIgualEnRespuestaYMdcYLimpiaAlTerminar() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/v1/categorias");
        request.addHeader("X-Request-Id", "id-no-confiable");
        var response = new MockHttpServletResponse();
        new RequestIdFilter().doFilter(request, response, (req, res) ->
                assertThat(MDC.get("requestId")).isEqualTo(response.getHeader("X-Request-Id")));
        assertThat(response.getHeader("X-Request-Id")).matches("[a-f0-9]{8}");
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void limpiaMdcAunqueLaPeticionFalle() {
        assertThatThrownBy(() -> new RequestIdFilter().doFilter(
                new MockHttpServletRequest("GET", "/fallo"), new MockHttpServletResponse(),
                (req, res) -> { throw new ServletException("fallo controlado"); }))
                .isInstanceOf(ServletException.class);
        assertThat(MDC.get("requestId")).isNull();
    }
}
