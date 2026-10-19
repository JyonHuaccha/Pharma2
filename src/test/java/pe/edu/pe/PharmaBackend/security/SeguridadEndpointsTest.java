package pe.edu.pe.PharmaBackend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import pe.edu.pe.PharmaBackend.config.CorsConfig;
import pe.edu.pe.PharmaBackend.config.SecurityConfig;
import pe.edu.pe.PharmaBackend.controller.AuthController;
import pe.edu.pe.PharmaBackend.controller.UsuarioController;
import pe.edu.pe.PharmaBackend.controller.ReporteController;
import pe.edu.pe.PharmaBackend.exception.GlobalExceptionHandler;
import pe.edu.pe.PharmaBackend.service.service.AuthService;
import pe.edu.pe.PharmaBackend.service.service.UsuarioService;
import pe.edu.pe.PharmaBackend.service.service.ReporteService;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(SeguridadEndpointsTest.Config.class)
@WebAppConfiguration
class SeguridadEndpointsTest {
    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, CorsConfig.class, AuthController.class, UsuarioController.class,
            ReporteController.class, GlobalExceptionHandler.class,
            RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
    static class Config {
        @Bean JwtService jwtService() { return new JwtService(
                "w9Z6dP/dzykNH/uiI3Z/KGjBlP0H4Ji+Tuq4fm2cTNuLD4jWafu1gBL74V2SJEvy", 60); }
        @Bean UserDetailsService userDetailsService() { return mock(UserDetailsService.class); }
        @Bean AuthService authService() { return mock(AuthService.class); }
        @Bean UsuarioService usuarioService() { return mock(UsuarioService.class); }
        @Bean ReporteService reporteService() { return mock(ReporteService.class); }
    }

    @Autowired WebApplicationContext context;
    @Autowired JwtService jwt;
    @Autowired UserDetailsService usuarios;
    @Autowired AuthService auth;
    @Autowired UsuarioService gestion;
    @Autowired ReporteService reportes;
    MockMvc mvc;

    private UserDetails vendedor(boolean activo, String... permisos) {
        return User.withUsername("vendedor").password("hash-de-prueba")
                .authorities(permisos).disabled(!activo).build();
    }

    private String tokenVendedor() {
        return jwt.generarToken(vendedor(true, "ROLE_VENDEDOR", "VENTAS_REGISTRAR"));
    }

    private String tokenAdmin() {
        UserDetails admin = User.withUsername("admin").password("hash-de-prueba")
                .authorities("ROLE_ADMIN", "USUARIOS_ADMINISTRAR", "REPORTES_VER").build();
        when(usuarios.loadUserByUsername("admin")).thenReturn(admin);
        return jwt.generarToken(admin);
    }

    @BeforeEach
    void preparar() {
        reset(usuarios, auth, gestion, reportes);
        when(usuarios.loadUserByUsername("vendedor")).thenReturn(vendedor(true, "ROLE_VENDEDOR"));
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void cambiarPasswordSinTokenEs401() throws Exception {
        mvc.perform(put("/api/v1/auth/password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"passwordActual\":\"Vendedor2026*\",\"passwordNueva\":\"NuevaClave2026\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(auth);
    }

    @Test
    void usuarioDePasswordSeObtieneDelTokenAunqueElCuerpoEnviaOtro() throws Exception {
        mvc.perform(put("/api/v1/auth/password").header("Authorization", "Bearer " + tokenVendedor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"id\":1,\"passwordActual\":\"Vendedor2026*\",\"passwordNueva\":\"NuevaClave2026\"}"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(auth).cambiarPassword(eq("vendedor"), any());
    }

    @Test
    void passwordSinNumeroEs400AntesDeLlamarAlServicio() throws Exception {
        mvc.perform(put("/api/v1/auth/password").header("Authorization", "Bearer " + tokenVendedor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"passwordActual\":\"Vendedor2026*\",\"passwordNueva\":\"abcdefgh\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.validationErrors.passwordNueva").exists());
        verifyNoInteractions(auth);
    }

    @Test
    void vendedorNoPuedeAsignarRolesNiCambiarEstado() throws Exception {
        String token = tokenVendedor();
        mvc.perform(put("/api/v1/usuarios/2/roles").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"rolIds\":[2,3]}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/usuarios/2/estado").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":false}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(gestion);
    }

    @Test
    void adminPuedeInvocarAmbasOperaciones() throws Exception {
        String token = tokenAdmin();
        mvc.perform(put("/api/v1/usuarios/2/roles").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"rolIds\":[2,3]}"))
                .andExpect(status().isOk());
        verify(gestion).asignarRoles(eq(2L), any(), eq("admin"));
        mvc.perform(patch("/api/v1/usuarios/2/estado").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":false}"))
                .andExpect(status().isOk());
        verify(gestion).cambiarEstado(eq(2L), any(), eq("admin"));
    }

    @Test
    void rolesVaciosYNulosSon400() throws Exception {
        String token = tokenAdmin();
        for (String cuerpo : new String[]{"{\"rolIds\":[]}", "{\"rolIds\":[null]}"}) {
            mvc.perform(put("/api/v1/usuarios/2/roles").header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(gestion);
    }

    @Test
    void estadoAusenteONuloEs400() throws Exception {
        String token = tokenAdmin();
        for (String cuerpo : new String[]{"{}", "{\"estado\":null}"}) {
            mvc.perform(patch("/api/v1/usuarios/2/estado").header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.validationErrors.estado").exists());
        }
        verifyNoInteractions(gestion);
    }

    @Test
    void elMismoTokenUsaLosPermisosActualizadosEnLaSiguientePeticion() throws Exception {
        String token = tokenVendedor();
        mvc.perform(get("/api/v1/reportes/productos-mas-vendidos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        when(usuarios.loadUserByUsername("vendedor")).thenReturn(vendedor(true, "ROLE_ADMIN", "REPORTES_VER"));
        mvc.perform(get("/api/v1/reportes/productos-mas-vendidos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        when(usuarios.loadUserByUsername("vendedor")).thenReturn(vendedor(true, "ROLE_VENDEDOR"));
        mvc.perform(get("/api/v1/reportes/productos-mas-vendidos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenDeUsuarioDesactivadoDejaDeServirDeInmediato() throws Exception {
        String token = tokenVendedor();
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        when(usuarios.loadUserByUsername("vendedor")).thenReturn(vendedor(false, "ROLE_VENDEDOR"));
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("El usuario está deshabilitado"));
    }

    @Test
    void sinBearerYFirmaAlteradaSon401() throws Exception {
        String token = tokenVendedor();
        mvc.perform(get("/api/v1/auth/me").header("Authorization", token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Debe iniciar sesión para acceder a este recurso"));
        int inicioFirma = token.lastIndexOf('.') + 1;
        String alterado = token.substring(0, inicioFirma) + (token.charAt(inicioFirma) == 'A' ? 'B' : 'A') + token.substring(inicioFirma + 1);
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("El token no es válido"));
    }

    @Test
    void preflightCorsPermiteAuthorizationDesdeLaSpa() throws Exception {
        mvc.perform(options("/api/v1/auth/me").header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Headers", "authorization"));
    }
}
