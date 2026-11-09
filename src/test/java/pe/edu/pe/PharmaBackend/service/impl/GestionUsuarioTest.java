package pe.edu.pe.PharmaBackend.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.pe.PharmaBackend.dto.auth.AsignarRolesRequestDTO;
import pe.edu.pe.PharmaBackend.dto.auth.CambiarEstadoUsuarioRequestDTO;
import pe.edu.pe.PharmaBackend.entity.Empleado;
import pe.edu.pe.PharmaBackend.entity.Rol;
import pe.edu.pe.PharmaBackend.entity.Usuario;
import pe.edu.pe.PharmaBackend.exception.RecursosNoEncontradoException;
import pe.edu.pe.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.pe.PharmaBackend.mapper.UsuarioMapper;
import pe.edu.pe.PharmaBackend.repository.EmpleadoRepository;
import pe.edu.pe.PharmaBackend.repository.RolRepository;
import pe.edu.pe.PharmaBackend.repository.UsuarioRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GestionUsuarioTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final RolRepository roles = mock(RolRepository.class);
    private final UsuarioServiceImpl servicio = new UsuarioServiceImpl(usuarios,
            mock(EmpleadoRepository.class), roles, mock(PasswordEncoder.class), new UsuarioMapper());
    private final Rol vendedor = new Rol(2L, "VENDEDOR", null);
    private final Rol almacen = new Rol(3L, "ALMACENERO", null);
    private final Rol admin = new Rol(1L, "ADMIN", null);

    private Usuario usuario(String username, Rol... asignados) {
        Empleado empleado = new Empleado();
        empleado.setId(7L); empleado.setNombres("Persona"); empleado.setApellidos("Prueba");
        Usuario usuario = new Usuario();
        usuario.setId(8L); usuario.setUsername(username); usuario.setEstado(true);
        usuario.setEmpleado(empleado); usuario.setRoles(new HashSet<>(List.of(asignados)));
        when(usuarios.findById(8L)).thenReturn(Optional.of(usuario));
        when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        return usuario;
    }

    @Test
    void rolesReemplazanLosAnterioresYDevuelvenLaUnionDePermisos() {
        Usuario usuario = usuario("vendedor", admin);
        when(roles.findAllById(Set.of(2L, 3L))).thenReturn(List.of(vendedor, almacen));
        var respuesta = servicio.asignarRoles(8L, new AsignarRolesRequestDTO(Set.of(2L, 3L)), "admin");
        assertThat(usuario.getRoles()).containsExactlyInAnyOrder(vendedor, almacen);
        assertThat(respuesta.getRoles()).containsExactly("ALMACENERO", "VENDEDOR");
        assertThat(respuesta.getPermisos()).contains("CATALOGO_ESCRIBIR", "VENTAS_REGISTRAR")
                .doesNotContain("REPORTES_VER").doesNotHaveDuplicates();
        verify(usuarios).save(usuario);
    }

    @Test
    void rolInexistenteNoEliminaLosRolesPrevios() {
        Usuario usuario = usuario("vendedor", vendedor);
        when(roles.findAllById(Set.of(2L, 99L))).thenReturn(List.of(vendedor));
        assertThatThrownBy(() -> servicio.asignarRoles(8L,
                new AsignarRolesRequestDTO(Set.of(2L, 99L)), "admin"))
                .isInstanceOf(RecursosNoEncontradoException.class).hasMessage("Roles no encontrados: [99]");
        assertThat(usuario.getRoles()).containsExactly(vendedor);
        verify(usuarios, never()).save(any());
    }

    @Test
    void rolesDeUsuarioInexistenteRespondenNoEncontrado() {
        assertThatThrownBy(() -> servicio.asignarRoles(999L,
                new AsignarRolesRequestDTO(Set.of(2L)), "admin"))
                .isInstanceOf(RecursosNoEncontradoException.class);
        verifyNoInteractions(roles);
        verify(usuarios, never()).save(any());
    }

    @Test
    void administradorNoPuedeQuitarseSuRolAunqueCambieMayusculas() {
        Usuario usuario = usuario("admin", admin);
        when(roles.findAllById(Set.of(2L))).thenReturn(List.of(vendedor));
        assertThatThrownBy(() -> servicio.asignarRoles(8L,
                new AsignarRolesRequestDTO(Set.of(2L)), "ADMIN"))
                .isInstanceOf(ReglaNegocioException.class).hasMessage("No puede quitarse a sí mismo el rol ADMIN");
        assertThat(usuario.getRoles()).containsExactly(admin);
        verify(usuarios, never()).save(any());
    }

    @Test
    void administradorPuedeMantenerAdminYAgregarOtroRol() {
        usuario("admin", admin);
        when(roles.findAllById(Set.of(1L, 2L))).thenReturn(List.of(admin, vendedor));
        assertThat(servicio.asignarRoles(8L, new AsignarRolesRequestDTO(Set.of(1L, 2L)), "admin").getRoles())
                .containsExactly("ADMIN", "VENDEDOR");
    }

    @Test
    void desactivacionConservaLaCuentaSuEmpleadoYSusRoles() {
        Usuario usuario = usuario("vendedor", vendedor);
        var respuesta = servicio.cambiarEstado(8L, new CambiarEstadoUsuarioRequestDTO(false), "admin");
        assertThat(respuesta.getEstado()).isFalse();
        assertThat(usuario.getEmpleado()).isNotNull();
        assertThat(usuario.getRoles()).containsExactly(vendedor);
        verify(usuarios).save(usuario);
        verify(usuarios, never()).delete(any(Usuario.class));
    }

    @Test
    void reactivacionConservaLaCuentaExistente() {
        Usuario usuario = usuario("vendedor", vendedor); usuario.setEstado(false);
        assertThat(servicio.cambiarEstado(8L, new CambiarEstadoUsuarioRequestDTO(true), "admin").getEstado()).isTrue();
        verify(usuarios).save(usuario);
    }

    @Test
    void administradorNoPuedeDesactivarseASiMismo() {
        Usuario usuario = usuario("admin", admin);
        assertThatThrownBy(() -> servicio.cambiarEstado(8L,
                new CambiarEstadoUsuarioRequestDTO(false), "ADMIN"))
                .isInstanceOf(ReglaNegocioException.class).hasMessage("No puede desactivarse a sí mismo");
        assertThat(usuario.getEstado()).isTrue();
        verify(usuarios, never()).save(any());
    }

    @Test
    void estadoDeUsuarioInexistenteRespondeNoEncontrado() {
        assertThatThrownBy(() -> servicio.cambiarEstado(999L,
                new CambiarEstadoUsuarioRequestDTO(false), "admin"))
                .isInstanceOf(RecursosNoEncontradoException.class);
        verify(usuarios, never()).save(any());
    }
}
