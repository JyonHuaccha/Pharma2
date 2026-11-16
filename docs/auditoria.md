# Auditoría de PharmaSoft

La autoría permite saber quién creó o modificó una fila; la bitácora registra hechos importantes y el log permite seguir una petición. La consulta de la bitácora se reserva a USUARIOS_ADMINISTRAR tanto en Spring Security como en /auditoria de Angular.

## Eventos implementados

| Acción | Momento | Entidad y detalle |
| --- | --- | --- |
| LOGIN_EXITOSO | Autenticación correcta, transacción independiente | Usuario solicitado; sin contraseña ni token |
| LOGIN_FALLIDO | Autenticación rechazada, transacción independiente | Usuario solicitado; sin contraseña ni motivo sensible |
| VENTA_REGISTRADA | Después del commit | ID de venta, total y cantidad de líneas |
| PRECIO_MODIFICADO | Después del commit | ID de producto, precio anterior y nuevo |
| PRODUCTO_ELIMINADO | Después del commit | ID y nombre del producto; la baja es lógica |
| CLIENTE_REGISTRADO | Después del commit, nuevo en la autónoma | ID y estado; no copia DNI, email, teléfono ni dirección |
| CATEGORIA_REGISTRADA | Después del commit, nuevo en la autónoma | ID y estado de la categoría |

Se auditan estos hechos porque afectan la venta, el catálogo o el acceso. No se genera un evento por cada GET: el filtro HTTP ya registra la petición y no conviene llenar la bitácora con lecturas repetidas.

## Columnas bitácora y logs

Las entidades Producto, Cliente y Categoria usan CreatedBy/LastModifiedBy y fechas; Venta conserva creadoPor. AuditoriaConfig resuelve el usuario autenticado. Las columnas guardan la autoría del estado actual y no sustituyen un historial.

AuditoriaEvento es una fila persistente con fecha, usuario, acción, entidad, entidadId, detalle y requestId. La tabla se consulta en páginas de 1 a 100 filas mediante AuditoriaController y AuditoriaServiceImpl. La SPA conserva usuario, accion, desde, hasta, pagina y tamanio en la URL. El rango incluye todo el último día usando un límite superior exclusivo.

RequestIdFilter crea X-Request-Id, lo coloca en MDC, registra método, ruta, estado y duración y lo elimina al terminar. En producción el patrón del archivo incluye ese identificador. No se deben habilitar logs de cuerpos ni de valores enlazados de SQL.

## Confirmación y rollback

VentaServiceImpl, ClienteServiceImpl y CategoriaServiceImpl publican EventoAuditoria dentro de su transacción. AuditoriaListener lo procesa con TransactionalEventListener AFTER_COMMIT; si el negocio se revierte, el listener no se ejecuta. AuditoriaServiceImpl.registrar utiliza REQUIRES_NEW porque la transacción de negocio ya terminó. La prueba AuditoriaNuevosEventosTest demuestra que la categoría todavía no tiene evento dentro de la transacción, que aparece tras confirmar y que no queda fila ni evento al revertir.

Un login fallido debe quedar registrado precisamente aunque se lance AuthenticationException. Por eso AuthServiceImpl llama directamente al servicio independiente y no espera el commit de la operación fallida. Un login correcto también usa ese servicio independiente. Estos son los dos eventos de acceso y no deben describirse como eventos AFTER_COMMIT del dominio.

Existe una limitación: si la base falla al guardar el evento después de confirmar el negocio, AuditoriaListener registra el error y el negocio permanece confirmado. No hay garantía de entrega duradera ni patrón outbox en esta versión; se debe supervisar ese log. El rollback de negocio, en cambio, sí evita publicar un evento exitoso.

## Datos excluidos

Nunca se guardan contraseñas, hashes BCrypt, JWT completos, secretos de firma, cabeceras Authorization ni cuerpos de login en la bitácora o los logs. Los eventos nuevos no duplican datos personales del cliente. Los reportes técnicos de Postman y las trazas completas se conservan localmente porque pueden contener credenciales o tokens.

## Caso trazado de punta a punta

PM-01 crea una venta como vendedor y guarda ventaId y requestIdVenta. PM-02 verifica creadoPor=vendedor. PM-14 busca el evento VENTA_REGISTRADA y compara entidadId y requestId con esa misma respuesta. SQL de apoyo: [trazabilidad-demo.sql](sql/trazabilidad-demo.sql). La guía de capturas explica cómo filtrar el log por el mismo identificador.

Para la demostración manual se repetirá este recorrido con la venta que registre el estudiante; sus IDs y capturas se incorporarán cuando estén disponibles. No se reemplazará esa venta por un ID de ejemplo. La evidencia automática se resume en [resultados](resultados-pruebas.md).

## Archivos para explicar el código

- [src/main/java/pe/edu/pe/PharmaBackend/auditoria/EventoAuditoria.java](../src/main/java/pe/edu/pe/PharmaBackend/auditoria/EventoAuditoria.java)
- [src/main/java/pe/edu/pe/PharmaBackend/auditoria/AuditoriaListener.java](../src/main/java/pe/edu/pe/PharmaBackend/auditoria/AuditoriaListener.java)
- [src/main/java/pe/edu/pe/PharmaBackend/service/impl/AuditoriaServiceImpl.java](../src/main/java/pe/edu/pe/PharmaBackend/service/impl/AuditoriaServiceImpl.java)
- [src/main/java/pe/edu/pe/PharmaBackend/config/AuditoriaConfig.java](../src/main/java/pe/edu/pe/PharmaBackend/config/AuditoriaConfig.java)
- [src/main/java/pe/edu/pe/PharmaBackend/config/RequestIdFilter.java](../src/main/java/pe/edu/pe/PharmaBackend/config/RequestIdFilter.java)
- Frontend: src/app/features/auditoria, app.routes.ts y core/config/menu.ts.

Caso técnico verificado: [venta autor evento y log](evidencias/s14-estabilizacion/trazabilidad-verificada.md). Las capturas manuales se incorporarán después.
