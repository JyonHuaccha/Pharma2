package pe.edu.pe.PharmaBackend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.pe.PharmaBackend.dto.ProductoRequestDTO;
import pe.edu.pe.PharmaBackend.dto.ProductoResponseDTO;
import pe.edu.pe.PharmaBackend.entity.Categoria;
import pe.edu.pe.PharmaBackend.entity.Producto;
import pe.edu.pe.PharmaBackend.exception.RecursosNoEncontradoException;
import pe.edu.pe.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.pe.PharmaBackend.repository.CategoriaRepository;
import pe.edu.pe.PharmaBackend.repository.ProductoRepository;
import pe.edu.pe.PharmaBackend.service.service.ProductoService;

import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import pe.edu.pe.PharmaBackend.dto.PaginaResponseDTO;
@Service
public class ProductoServiceImpl implements ProductoService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductoServiceImpl.class);
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository, CategoriaRepository categoriaRepository){
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public ProductoResponseDTO create(ProductoRequestDTO t) {
        String nombre = t.getNombre().trim();
        if (productoRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException(
                    "Ya existe un producto con el nombre "+nombre
            );
        }
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(t.getDescripcion());
        producto.setPrecio(t.getPrecio());
        producto.setStock(t.getStock());
        producto.setEstado(t.getEstado());
        Categoria categoria = buscarCategoriaPorId(t.getCategoriaId());
        validarCategoriaActiva(categoria);
        producto.setCategoria(categoria);
        Producto prodCreada = productoRepository.save(producto);
        return convertirResponse(prodCreada);
    }

    private Categoria buscarCategoriaPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException(
                        "Categoría no encontrada con id: " + id
                ));
    }
    private void validarCategoriaActiva(Categoria categoria) {
        if (!Boolean.TRUE.equals(categoria.getEstado())) {
            throw new ReglaNegocioException(
                    "La categoría \"" + categoria.getNombre() + "\" está inactiva"
            );
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public ProductoResponseDTO update(Long aLong, ProductoRequestDTO t) {
        Producto producto = productoRepository.findById(aLong).orElseThrow(()->
                new RecursosNoEncontradoException(
                        "Producto no encontrada con id: "+aLong
                )
        );

        String nombre = t.getNombre().trim();
        if (productoRepository.existsByNombreIgnoreCaseAndIdNot(nombre, aLong)) {
            throw new ReglaNegocioException(
                    "Ya existe un producto con el nombre " + nombre
            );
        }

        Categoria categoria = buscarCategoriaPorId(t.getCategoriaId());
        boolean cambioCategoria = producto.getCategoria() == null
                || !producto.getCategoria().getId().equals(categoria.getId());
        if (cambioCategoria) {
            validarCategoriaActiva(categoria);
        }

        producto.setNombre(nombre);
        producto.setDescripcion(t.getDescripcion());
        producto.setPrecio(t.getPrecio());
        producto.setStock(t.getStock());
        producto.setEstado(t.getEstado());
        producto.setCategoria(categoria);
        producto.setFechaModificacion(LocalDateTime.now());

        Producto prodActualizada = productoRepository.save(producto);
        return convertirResponse(prodActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productos", key = "'id-' + #p0")
    public ProductoResponseDTO read(Long aLong) {
        Producto producto = productoRepository.findById(aLong)
                .orElseThrow(()->
                        new RecursosNoEncontradoException(
                                "Producto no encontrar con id: "+aLong
                        )
                );
        return convertirResponse(producto);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public void delete(Long aLong) {
        Producto producto = productoRepository.findById(aLong).orElseThrow(()->
                new RecursosNoEncontradoException(
                        "Producto no encontrada con id: "+ aLong
                )
        );

        if (!Boolean.TRUE.equals(producto.getEstado())) {
            throw new ReglaNegocioException("El producto ya está inactivo");
        }

        producto.setEstado(false);
        productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productos", key = "'todas'")
    public Iterable<ProductoResponseDTO> readAll() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productos", key = "#p0 + '-' + #p1 + '-' + #p2 + '-' + #p3 + '-cat-' + #p4")
    public PaginaResponseDTO<ProductoResponseDTO> listar(int pagina, int tamanio,
            String ordenarPor, String direccion, Long categoriaId) {
        if (pagina < 0 || tamanio < 1 || tamanio > 100) {
            throw new ReglaNegocioException("Página desde 0 y tamaño entre 1 y 100");
        }
        if (!Set.of("id", "nombre", "precio", "stock", "estado", "categoriaNombre").contains(ordenarPor)) {
            throw new ReglaNegocioException("Campo de ordenamiento no permitido");
        }
        if (!"asc".equalsIgnoreCase(direccion) && !"desc".equalsIgnoreCase(direccion)) {
            throw new ReglaNegocioException("La dirección debe ser asc o desc");
        }
        if (categoriaId != null && categoriaId < 1) {
            throw new ReglaNegocioException("La categoría debe tener un identificador positivo");
        }
        String campo = "categoriaNombre".equals(ordenarPor) ? "categoria.nombre" : ordenarPor;
        Sort sort = Sort.by(Sort.Direction.fromString(direccion), campo);
        if (!"id".equals(campo)) sort = sort.and(Sort.by("id"));
        PageRequest pageable = PageRequest.of(pagina, tamanio, sort);
        Page<Producto> resultado = categoriaId == null
                ? productoRepository.findAll(pageable)
                : productoRepository.findByCategoriaId(categoriaId, pageable);
        return new PaginaResponseDTO<>(resultado.getContent().stream().map(this::convertirResponse).toList(),
                resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(),
                resultado.getTotalPages(), resultado.isLast());
    }
    private ProductoResponseDTO convertirResponse(Producto producto){
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoria() != null ? producto.getCategoria().getId() : null,
                producto.getCategoria()!=null ? producto.getCategoria().getNombre() : null,
                producto.getEstado(),
                producto.getFechaCreacion(),
                producto.getFechaModificacion()
        );
    }
}
