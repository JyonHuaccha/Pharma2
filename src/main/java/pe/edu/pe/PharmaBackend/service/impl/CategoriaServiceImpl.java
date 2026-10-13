package pe.edu.pe.PharmaBackend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.pe.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.pe.PharmaBackend.dto.CategoriaResponseDTO;
import pe.edu.pe.PharmaBackend.entity.Categoria;
import pe.edu.pe.PharmaBackend.exception.RecursosNoEncontradoException;
import pe.edu.pe.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.pe.PharmaBackend.repository.CategoriaRepository;
import pe.edu.pe.PharmaBackend.repository.ProductoRepository;
import pe.edu.pe.PharmaBackend.service.service.CategoriaService;

import java.time.LocalDateTime;

@Service
public class CategoriaServiceImpl implements CategoriaService {
    private static final Logger LOG = LoggerFactory.getLogger(CategoriaServiceImpl.class);
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public CategoriaResponseDTO create(CategoriaRequestDTO t) {
        String nombre = t.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException(
                    "Ya existe una categoria con el nombre "+nombre
            );
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(t.getDescripcion());
        categoria.setEstado(t.getEstado());

        Categoria catCreada = categoriaRepository.save(categoria);
        return convertirResponse(catCreada);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO update(Long aLong, CategoriaRequestDTO t) {
        Categoria categoria = categoriaRepository.findById(aLong).orElseThrow(()->
                new RecursosNoEncontradoException(
                        "Categoria no encontrada con id: "+aLong
                )
        );

        String nombre = t.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, aLong)) {
            throw new ReglaNegocioException(
                    "Ya existe otra categoria con el nombre " + nombre
            );
        }

        if (Boolean.TRUE.equals(categoria.getEstado()) && Boolean.FALSE.equals(t.getEstado())) {
            validarBaja(aLong);
        }
        categoria.setNombre(nombre);
        categoria.setDescripcion(t.getDescripcion());
        categoria.setEstado(t.getEstado());
        categoria.setFechaModificacion(LocalDateTime.now());

        Categoria catActualizada = categoriaRepository.save(categoria);
        return convertirResponse(catActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO read(Long aLong) {
        Categoria categoria = categoriaRepository.findById(aLong)
                .orElseThrow(()->
                        new RecursosNoEncontradoException(
                                "Categoria no encontrar con id: "+aLong
                        )
                );
        return convertirResponse(categoria);
    }

    @Override
    @Transactional
    public void delete(Long aLong) {
        Categoria categoria = categoriaRepository.findById(aLong).orElseThrow(()->
                new RecursosNoEncontradoException(
                        "Categoria no encontrada con id: "+ aLong
                )
        );

        if (!Boolean.TRUE.equals(categoria.getEstado())) {
            throw new ReglaNegocioException("La categoría ya está inactiva");
        }
        validarBaja(aLong);
        categoria.setEstado(false);
        categoria.setFechaModificacion(LocalDateTime.now());
        categoriaRepository.save(categoria);
    }

    private void validarBaja(Long id) {
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar la categoría porque tiene productos asociados"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<CategoriaResponseDTO> readAll() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }
    private CategoriaResponseDTO convertirResponse(Categoria categoria){
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getEstado(),
                categoria.getFechaCreacion(),
                categoria.getFechaModificacion()
        );
    }
}
