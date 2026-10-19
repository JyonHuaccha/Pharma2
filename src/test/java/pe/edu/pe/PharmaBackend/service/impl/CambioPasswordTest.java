package pe.edu.pe.PharmaBackend.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pe.edu.pe.PharmaBackend.dto.auth.CambioPasswordRequestDTO;
import pe.edu.pe.PharmaBackend.entity.Usuario;
import pe.edu.pe.PharmaBackend.exception.RecursosNoEncontradoException;
import pe.edu.pe.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.pe.PharmaBackend.mapper.UsuarioMapper;
import pe.edu.pe.PharmaBackend.repository.UsuarioRepository;
import pe.edu.pe.PharmaBackend.security.JwtService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CambioPasswordTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final AuthServiceImpl servicio = new AuthServiceImpl(
            mock(AuthenticationManager.class), mock(JwtService.class), usuarios,
            new UsuarioMapper(), encoder);

    private Usuario vendedor() {
        Usuario usuario = new Usuario();
        usuario.setUsername("vendedor");
        usuario.setPassword(encoder.encode("Vendedor2026*"));
        when(usuarios.findByUsernameIgnoreCase("vendedor")).thenReturn(Optional.of(usuario));
        return usuario;
    }

    @Test
    void guardaUnHashNuevoQueSoloAceptaLaNuevaClave() {
        Usuario usuario = vendedor();
        String anterior = usuario.getPassword();
        servicio.cambiarPassword("vendedor", new CambioPasswordRequestDTO("Vendedor2026*", "NuevaClave2026"));
        assertThat(usuario.getPassword()).hasSize(60).isNotEqualTo(anterior).startsWith("$2a$");
        assertThat(encoder.matches("NuevaClave2026", usuario.getPassword())).isTrue();
        assertThat(encoder.matches("Vendedor2026*", usuario.getPassword())).isFalse();
        verify(usuarios).save(usuario);
    }

    @Test
    void claveActualIncorrectaNoModificaLaCuenta() {
        Usuario usuario = vendedor();
        String anterior = usuario.getPassword();
        assertThatThrownBy(() -> servicio.cambiarPassword("vendedor",
                new CambioPasswordRequestDTO("Incorrecta2026", "NuevaClave2026")))
                .isInstanceOf(ReglaNegocioException.class).hasMessage("La contraseña actual no es correcta");
        assertThat(usuario.getPassword()).isEqualTo(anterior);
        verify(usuarios, never()).save(any());
    }

    @Test
    void noPermiteReutilizarLaClaveActual() {
        vendedor();
        assertThatThrownBy(() -> servicio.cambiarPassword("vendedor",
                new CambioPasswordRequestDTO("Vendedor2026*", "Vendedor2026*")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La nueva contraseña debe ser distinta de la actual");
        verify(usuarios, never()).save(any());
    }

    @Test
    void rechazaMasDe72BytesAunqueNoSupere72Caracteres() {
        vendedor();
        assertThatThrownBy(() -> servicio.cambiarPassword("vendedor",
                new CambioPasswordRequestDTO("Vendedor2026*", "Ab1" + "ñ".repeat(35))))
                .isInstanceOf(ReglaNegocioException.class).hasMessage("La contraseña no puede superar 72 bytes");
        verify(usuarios, never()).save(any());
    }

    @Test
    void usuarioInexistenteNoEscribeEnRepositorio() {
        assertThatThrownBy(() -> servicio.cambiarPassword("ausente",
                new CambioPasswordRequestDTO("Vendedor2026*", "NuevaClave2026")))
                .isInstanceOf(RecursosNoEncontradoException.class);
        verify(usuarios, never()).save(any());
    }
}
