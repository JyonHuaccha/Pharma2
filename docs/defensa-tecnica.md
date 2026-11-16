# Preparación de la defensa técnica

Borrador de estudio basado en el código real. El estudiante debe revisarlo, probar los recorridos y explicarlo con sus propias palabras; este archivo no acredita una sustentación realizada.

## 1 Por qué la página se calcula en el servidor y qué consulta Oracle

Una página reduce los datos transferidos y las filas que Angular tiene que pintar. ProductoRepository devuelve Page: Hibernate aplica offset/fetch y puede ejecutar count para el total. VentaRepository primero obtiene una página de cabeceras con countQuery y luego carga los detalles solo de esos IDs; no se pagina un join que multiplique filas. El conteo puede omitirse cuando Spring deduce el total de una página incompleta.

Código: repository/ProductoRepository.java y repository/VentaRepository.java. Las rutas Java parten de src/main/java/pe/edu/pe/PharmaBackend/.

## 2 Cómo una venta evita datos a medias y eventos de una operación fallida

VentaServiceImpl.registrar es Transactional. Si la segunda línea no tiene stock, la excepción revierte la primera y la cabecera. EventoAuditoria se entrega al listener AFTER_COMMIT: una venta revertida no crea el evento exitoso. La escritura del evento usa REQUIRES_NEW. Esa separación requiere vigilar el log si falla la bitácora después del commit; no garantiza un outbox duradero.

Código: service/impl/VentaServiceImpl.java y auditoria/AuditoriaListener.java. Las rutas Java parten de src/main/java/pe/edu/pe/PharmaBackend/.

## 3 Qué pasa cuando vence el token a mitad de una venta

AuthService calcula el tiempo restante y guarda la ruta interna. El temporizador cierra la sesión con expirada=1 y returnUrl; si una petición llega antes de que actúe, JwtAuthenticationFilter valida exp y devuelve 401. El interceptor de errores limpia la sesión y lleva al login. Tras autenticar se vuelve a la ruta. El formulario en memoria no se conserva después de destruirlo; volver a la ruta no promete recuperar el carrito.

Código: security/JwtAuthenticationFilter.java; frontend src/app/core/auth/auth-service.ts y core/interceptors/error-interceptor-interceptor.ts. Las rutas Java parten de src/main/java/pe/edu/pe/PharmaBackend/.

## 4 Por qué se invalida la caché de productos al vender

El stock forma parte del producto consultado. Tras confirmar la venta, CacheEvict invalida productos para que la siguiente lectura observe Oracle. Sin eso se mostraría stock anterior hasta el TTL y el usuario podría intentar vender existencias ya consumidas. El descuento condicional sigue siendo la protección final del backend.

Código: service/impl/VentaServiceImpl.java, config/CacheConfig.java y repository/ProductoRepository.java. Las rutas Java parten de src/main/java/pe/edu/pe/PharmaBackend/.

## 5 Qué distingue logs columnas de auditoría y bitácora

El log muestra ejecución y diagnóstico; puede rotar. CreadoPor y modificadoPor identifican la autoría del estado persistido. AuditoriaEvento registra hechos de negocio con fecha y requestId. Ninguno justifica registrar contraseñas o tokens. Para defender una venta comparo su id, creado_por, evento y requestId del log.

Código: entity/Venta.java, entity/AuditoriaEvento.java y config/RequestIdFilter.java. Las rutas Java parten de src/main/java/pe/edu/pe/PharmaBackend/.

## 6 Cómo se decide que una prueba E2E es estable

Debe sincronizarse con respuestas o elementos observables, repetir sobre datos conocidos y dar el mismo resultado sin sleeps fijos ni retries. La suite tiene un worker y retries=0. Al fallar se para y conserva trace.zip. Aquí el total 20.00 fue confundido con la subcadena 0.00; se corrigió comparando un número positivo. También se corrigieron CORS del entorno y el nombre accesible del selector antes de la corrida de 42 resultados correctos.

Código: frontend playwright.config.ts, e2e/flujo-venta.spec.ts y e2e/autonoma.spec.ts. Las rutas Java parten de src/main/java/pe/edu/pe/PharmaBackend/.

## 7 Por qué ocultar un botón no basta para autorizar

El usuario puede escribir una URL o construir HTTP en Postman. La SPA mejora la experiencia con menú, directiva y permisoGuard; SecurityConfig vuelve a comprobar la autoridad en cada petición. Vendedor no ve Auditoría y un GET directo a su API devuelve 403 aunque se modifique el HTML.

Código: config/SecurityConfig.java; frontend src/app/app.routes.ts y core/config/menu.ts. Las rutas Java parten de src/main/java/pe/edu/pe/PharmaBackend/.

## 8 Cuál es la diferencia entre 400 401 403 404 y 409

400 indica un DTO o parámetro inválido; 401 que falta autenticación válida; 403 que el usuario autenticado no tiene permiso; 404 que el recurso no existe; 409 que una regla del negocio impide la operación, por ejemplo stock insuficiente. La colección los comprueba para ventas, productos y auditoría; no se espera que todo error sea 500.

Código: exception/GlobalExceptionHandler.java y security/RestAccessDeniedHandler.java. Las rutas Java parten de src/main/java/pe/edu/pe/PharmaBackend/.

## 9 Por qué el precio y el total se calculan de nuevo en el backend

El navegador puede manipularse y el catálogo cambiar mientras se llena la venta. El DTO lleva productoId y cantidad; el servicio busca el precio vigente en Oracle y calcula subtotal y total con BigDecimal. La interfaz solo muestra un estimado. Las líneas repetidas se rechazan para conservar una regla clara.

Código: dto/VentaRequestDTO.java y service/impl/VentaServiceImpl.java. Las rutas Java parten de src/main/java/pe/edu/pe/PharmaBackend/.

## 10 Cómo se evita vender el mismo stock simultáneamente

ProductoRepository realiza un update condicional que descuenta solo si estado es activo y stock alcanza. Si actualiza cero filas, el servicio lanza 409 y revierte la transacción. No basta con leer el número y restarlo en Java porque dos peticiones podrían leer el mismo valor. E2E-08 cambia stock después de que se pinta el formulario y comprueba el rechazo real.

Código: repository/ProductoRepository.java y service/impl/VentaServiceImpl.java. Las rutas Java parten de src/main/java/pe/edu/pe/PharmaBackend/.
