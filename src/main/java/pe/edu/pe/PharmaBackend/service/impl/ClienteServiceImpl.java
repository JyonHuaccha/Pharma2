package pe.edu.pe.PharmaBackend.service.impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.pe.PharmaBackend.dto.ClienteRequestDTO;
import pe.edu.pe.PharmaBackend.dto.ClienteResponseDTO;
import pe.edu.pe.PharmaBackend.entity.Cliente;
import pe.edu.pe.PharmaBackend.exception.RecursosNoEncontradoException;
import pe.edu.pe.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.pe.PharmaBackend.repository.ClienteRepository;
import pe.edu.pe.PharmaBackend.repository.VentaRepository;
import pe.edu.pe.PharmaBackend.service.service.ClienteService;

import java.util.List;

@Service
public class ClienteServiceImpl
        implements ClienteService {

    private static final Logger log =
            LoggerFactory.getLogger(ClienteServiceImpl.class);

    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;

    public ClienteServiceImpl(
            ClienteRepository clienteRepository, VentaRepository ventaRepository) {
        this.clienteRepository = clienteRepository;
        this.ventaRepository = ventaRepository;
    }

    @Override
    @Transactional
    public ClienteResponseDTO create(
            ClienteRequestDTO request) {

        log.info(
                "Registrando cliente con DNI={}",
                request.getDni()
        );

        String dni = request.getDni().trim();
        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (clienteRepository.existsByDni(dni)) {
            throw new ReglaNegocioException(
                    "Ya existe un cliente con el DNI: " + dni
            );
        }

        if (clienteRepository.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException(
                    "Ya existe un cliente con el correo: " + email
            );
        }

        Cliente cliente = new Cliente();

        cliente.setDni(dni);
        cliente.setNombres(
                request.getNombres().trim()
        );
        cliente.setApellidos(
                request.getApellidos().trim()
        );
        cliente.setEmail(email);
        cliente.setTelefono(
                normalizar(request.getTelefono())
        );
        cliente.setDireccion(
                normalizar(request.getDireccion())
        );
        cliente.setEstado(request.getEstado());

        Cliente guardado =
                clienteRepository.save(cliente);

        log.info(
                "Cliente registrado correctamente id={}",
                guardado.getId()
        );

        return convertirResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO read(Long id) {

        log.info("Buscando cliente id={}", id);

        Cliente cliente =
                clienteRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursosNoEncontradoException(
                                        "Cliente no encontrado con id: " + id
                                )
                        );

        return convertirResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> readAll() {

        log.info("Listando clientes");

        return clienteRepository.findAll()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClienteResponseDTO update(
            Long id,
            ClienteRequestDTO request) {

        Cliente cliente =
                clienteRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursosNoEncontradoException(
                                        "Cliente no encontrado con id: " + id
                                )
                        );

        String dni = request.getDni().trim();
        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (clienteRepository
                .existsByDniAndIdNot(dni, id)) {

            throw new ReglaNegocioException(
                    "Ya existe otro cliente con el DNI: "
                            + dni
            );
        }

        if (clienteRepository
                .existsByEmailIgnoreCaseAndIdNot(
                        email,
                        id)) {

            throw new ReglaNegocioException(
                    "Ya existe otro cliente con el correo: "
                            + email
            );
        }

        if (Boolean.TRUE.equals(cliente.getEstado()) && Boolean.FALSE.equals(request.getEstado())) {
            validarBaja(id);
        }

        cliente.setDni(dni);
        cliente.setNombres(
                request.getNombres().trim()
        );
        cliente.setApellidos(
                request.getApellidos().trim()
        );
        cliente.setEmail(email);
        cliente.setTelefono(
                normalizar(request.getTelefono())
        );
        cliente.setDireccion(
                normalizar(request.getDireccion())
        );
        cliente.setEstado(request.getEstado());

        Cliente actualizado =
                clienteRepository.save(cliente);

        log.info(
                "Cliente id={} actualizado correctamente",
                id
        );

        return convertirResponse(actualizado);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Cliente cliente =
                clienteRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursosNoEncontradoException(
                                        "Cliente no encontrado con id: " + id
                                )
                        );

        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new ReglaNegocioException("El cliente ya está inactivo");
        }

        validarBaja(id);
        cliente.setEstado(false);
        clienteRepository.save(cliente);

        log.info(
                "Cliente id={} dado de baja correctamente",
                id
        );
    }

    private void validarBaja(Long id) {
        if (ventaRepository.existsByClienteId(id)) {
            throw new ReglaNegocioException("No se puede eliminar el cliente porque tiene ventas asociadas");
        }
    }

    private ClienteResponseDTO convertirResponse(
            Cliente cliente) {

        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getDni(),
                cliente.getNombres(),
                cliente.getApellidos(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                cliente.getEstado(),
                cliente.getFechaCreacion(),
                cliente.getFechaModificacion()
        );
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
