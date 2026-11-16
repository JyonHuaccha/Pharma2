# Incidencias de integración de la sesión 14

| ID | Módulo | Descripción y reproducción | Severidad | Causa | Corrección y commit | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| INC-01 | Ventas | Consultar /ventas/buscar con pagina=1. Se devolvía toda la lista y Angular la recortaba. | Alta | Repositorio List con fetch de toda la colección. | Page de cabeceras, countQuery y carga de detalles limitada a los IDs de la página. Backend d751c85. Angular consume la página en 9830aee. | Cerrada |
| INC-02 | Paginación | Cambiar página de Productos y pulsar F5 reiniciaba la página. Ventas tampoco conservaba los filtros en la URL. | Alta | Estado local en signals y formulario. | Inputs del router, rxResource, linkedSignal y queryParams. Filtros, orden y tamaño vuelven a página 0. Frontend 1e71111 y 9830aee. | Cerrada |
| INC-03 | Ordenamiento | Enviar ordenarPor=fe al buscador de ventas aceptaba un campo incompleto. | Alta | String.contains validaba subcadenas. | Set con coincidencia exacta, tamaño 1..100 y orden secundario por id. Backend d751c85. | Cerrada |
| INC-04 | Migraciones | Solo existía V2 y Flyway no estaba integrado. | Alta | Esquema creado mediante Hibernate update. | Flyway con V1-V4, adopción del esquema histórico en dev y validate. Historial nuevo comprobado en Oracle. Backend bc077cb. | Cerrada |
| INC-05 | Volumen | El script de la guía utiliza productos y la entidad real utiliza producto. | Media | Diferencia de nombres entre la guía y el esquema existente. | V4 y docs/sql/volumen_pruebas.sql usan PRODUCTO; carga separada en PHARMA_S14. Backend bc077cb. | Cerrada |
| INC-06 | Arranque en esquema histórico | Al ejecutar con PHARMADB, Hibernate informa missing column fecha_modificacion en CLIENTES. | Alta | La columna histórica contiene una tilde (FECHA_MODIFICACIÓN); V1 y la entidad usan el nombre sin tilde. El esquema aislado de S14 no reproducía esa diferencia. | V5 renombra la columna conservando sus fechas. Commit e4691d5. Arranque con PHARMADB y health 200; 8 clientes y 3 fechas no nulas, con idéntica huella SHA256 antes/después. Las 58 pruebas también pasan sobre PHARMA_S14 con V5. | Cerrada |

Las incidencias INC-01 a INC-05 se identificaron durante la inspección inicial. INC-06 se detectó posteriormente al ejecutar contra el esquema histórico PHARMADB. La medición inicial de Ventas conserva el contrato anterior de lista completa en las evidencias.

Verificación: matriz I-01 a I-08 cumplida; pruebas de auditoría de commit y rollback en Oracle; suite Angular y Maven sin fallos. Las siete pruebas E2E se ejecutan con un worker, retries=0 y --repeat-each=3. Los resultados concretos y las capturas se entregan en el informe PDF y en el reporte HTML de Playwright.

Los dos fallos iniciales de infraestructura de pruebas (componente ffmpeg ausente y servidor Angular no accesible desde el entorno aislado) se resolvieron instalando ffmpeg e iniciando el servidor local accesible a Chrome. No fueron fallos funcionales de la aplicación. Las pruebas unitarias anteriores se adaptaron para esperar la carga asíncrona de rxResource y consumir el contrato Page.






## Ampliación de la actividad autónoma

Las seis primeras incidencias se detectaron el 12/11/2026. Se conserva su tabla original para mantener la relación con los commits. Sus expectativas y regresiones se detallan a continuación.

| ID | Esperado frente a obtenido antes de corregir | Prueba que evita regresión |
| --- | --- | --- |
| INC-01 | Esperado: solo una página; obtenido: listado completo y recorte en SPA | E2E paginación; PM-08 y consulta paginada de ventas |
| INC-02 | Esperado: F5 conserva página y filtros; obtenido: reinicio por estado local | E2E paginación y E2E-11 |
| INC-03 | Esperado: rechazar fe; obtenido: coincidencia de subcadena | PM-13 tamaño y contrato de PaginacionUtil; prueba manual de ordenarPor=fe |
| INC-04 | Esperado: esquema reproducible; obtenido: tablas creadas con update sin migración completa | Maven verify arranca con Flyway validate; consulta flyway_schema_history |
| INC-05 | Esperado: cargar la tabla real; obtenido: script refería PRODUCTOS inexistente | Ejecutar volumen_pruebas.sql en esquema aislado y contar filas; se mantiene manual por tamaño |
| INC-06 | Esperado: arrancar contra esquema histórico; obtenido: columna sin tilde ausente | Maven verify y validación de V5; huella de fechas histórica ya registrada |

### INC-07 Origen del servidor E2E de producción

- Fecha: 15/11/2026. Módulo: infraestructura de integración. Severidad: alta. Estado: cerrada.
- Reproducción: servir el build en http://127.0.0.1:4210 y reenviar login a la API sin autorizar ese origen.
- Esperado: login válido 200 y contraseña errónea 401. Obtenido: 403 antes del controlador.
- Causa raíz: CorsConfig admite solo los orígenes configurados; 4210 no estaba en el entorno temporal.
- Corrección: agregar 4210 a APP_CORS_ALLOWED_ORIGINS del proceso de prueba. Se conserva Origin y la validación CORS, no se desactiva seguridad.
- Commit: frontend ecec0ec incluye servidor, ejecución y recordatorio del origen. La configuración del proceso es local.
- Regresión: E2E de login válido e inválido en build real. Traza privada conservada en tmp/s14a/incidencia-cors; contiene el 403 con Origin 4210.

### INC-08 Nombre accesible del filtro Acción

- Fecha: 15/11/2026. Módulo: Auditoría SPA. Severidad: media. Estado: cerrada.
- Reproducción: consultar getByLabel('Acción', exact:true) en el selector nuevo.
- Esperado: seleccionar VENTA_REGISTRADA mediante un nombre accesible preciso. Obtenido: timeout del localizador.
- Causa raíz: el label envolvía el select y su texto se combinaba con los nombres de las opciones.
- Corrección: aria-label='Acción' explícito. Commit frontend cd28730.
- Regresión: E2E-12 selecciona la acción, filtra y encuentra el ID y requestId reales; no se añadió waitForTimeout ni retry.
- Evidencia diagnóstica: trace.zip inspeccionado, guardado localmente como tmp/s14a/incidencia-selector.zip.

### INC-09 Total positivo confundido con cero

- Fecha: 15/11/2026. Módulo: pruebas de Ventas. Severidad: media. Estado: cerrada.
- Reproducción: vender dos unidades de S/10 con la aserción not.toContainText('0.00').
- Esperado: aceptar total positivo S/20.00. Obtenido: fallo porque 20.00 contiene la subcadena 0.00.
- Causa raíz: aserción textual no representaba la regla numérica.
- Corrección: convertir el importe visible a número y esperar un valor mayor que cero; filtrar la categoría propia al verificar stock. Commit frontend ecec0ec.
- Regresión: el flujo de venta pasa con ese precio en tres repeticiones. Traza inspeccionada en tmp/s14a/incidencia-total.zip.

### INC-10 Sintaxis del script Postman de rollback

- Fecha: 15/11/2026. Módulo: pruebas API. Severidad: media. Estado: cerrada.
- Reproducción: ejecutar PM-28 en la primera versión de la colección autónoma.
- Esperado: comparar conteo de eventos y ausencia del requestId rechazado. Obtenido: SyntaxError por paréntesis adicional en el test.
- Causa raíz: error al escribir la expresión pm.expect con Array.some.
- Corrección: corregir los paréntesis y validar toda la colección con Postman CLI. Commit backend 672c9ef.
- Regresión: 38 peticiones y 86 aserciones correctas; cero errores de scripts.

### INC-11 Request ID ausente en el archivo de desarrollo

- Fecha: 15/11/2026. Módulo: trazabilidad. Severidad: media. Estado: cerrada.
- Reproducción: iniciar dev con LOGGING_FILE_NAME y buscar en el archivo el requestId de una venta.
- Esperado: el archivo conserva el identificador que la consola ya muestra. Obtenido: el patrón por defecto del archivo lo omitía.
- Causa raíz: application-dev.yaml solo configuraba logging.pattern.console.
- Corrección: agregar logging.pattern.file con MDC requestId, igual que producción. Commit backend 4305a17.
- Regresión: arrancar el JAR corregido, ejecutar una petición y comparar X-Request-Id con el archivo. La venta técnica 22255 ya se relacionó con el evento 8821d482 y las líneas de consola.

### INC-12 Restauración de stock antes de finalizar la recarga

- Fecha: 16/11/2026. Módulo: pruebas E2E e integración de catálogo. Severidad: media. Estado: cerrada.
- Reproducción: correr la suite con más de 5000 productos; E2E-08 provoca 409 y restaura stock mientras la SPA aún está actualizando el catálogo.
- Esperado: el siguiente caso encuentra el producto con stock restaurado. Obtenido: E2E-10 no encuentra el producto; la traza muestra el catálogo con stock 0.
- Causa raíz: la limpieza de E2E-08 no esperaba la lectura disparada por el 409; esa lectura podía conservarse en caché después del restablecimiento.
- Corrección: esperar el estado visible Stock y precios actualizados antes de restaurar el producto. Sin sleeps fijos ni reintentos. Commit frontend 91a20c1.
- Regresión: 14 pruebas repetidas tres veces, 42 correctas, 0 flaky, sobre PHARMADB y su volumen real. La traza anterior permanece local en tmp/s14a-final/diagnostico/stock-catalogo.zip.

## Resumen de cierre

| Severidad | Cantidad | Abiertas |
| --- | --- | --- |
| Crítica | 0 | 0 |
| Alta | 6 | 0 |
| Media | 6 | 0 |
| Baja | 0 | 0 |

Por módulo principal: Ventas 1, Paginación 1, Ordenamiento 1, Migraciones 1, Volumen 1, Arranque 1, Infraestructura E2E 1, Auditoría SPA 1, Pruebas de Ventas 1 y Pruebas API 1 y Trazabilidad 1, más 1 de sincronización E2E del catálogo. Total: doce incidencias cerradas. No queda ninguna crítica o alta abierta en este registro. Ensayos, capturas propias y video son actividades de entrega pendientes, no incidencias técnicas resueltas ni pruebas aprobadas.
