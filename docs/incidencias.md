# Incidencias de integración de la sesión 14

| ID | Módulo | Descripción y reproducción | Severidad | Causa | Corrección y commit | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| INC-01 | Ventas | Consultar /ventas/buscar con pagina=1. Se devolvía toda la lista y Angular la recortaba. | Alta | Repositorio List con fetch de toda la colección. | Page de cabeceras, countQuery y carga de detalles limitada a los IDs de la página. Backend a70fb25. Angular consume la página en 5306089. | Cerrada |
| INC-02 | Paginación | Cambiar página de Productos y pulsar F5 reiniciaba la página. Ventas tampoco conservaba los filtros en la URL. | Alta | Estado local en signals y formulario. | Inputs del router, rxResource, linkedSignal y queryParams. Filtros, orden y tamaño vuelven a página 0. Frontend f1edc71 y 5306089. | Cerrada |
| INC-03 | Ordenamiento | Enviar ordenarPor=fe al buscador de ventas aceptaba un campo incompleto. | Alta | String.contains validaba subcadenas. | Set con coincidencia exacta, tamaño 1..100 y orden secundario por id. Backend a70fb25. | Cerrada |
| INC-04 | Migraciones | Solo existía V2 y Flyway no estaba integrado. | Alta | Esquema creado mediante Hibernate update. | Flyway con V1-V4, adopción del esquema histórico en dev y validate. Historial nuevo comprobado en Oracle. Backend 7b0cfd7. | Cerrada |
| INC-05 | Volumen | El script de la guía utiliza productos y la entidad real utiliza producto. | Media | Diferencia de nombres entre la guía y el esquema existente. | V4 y docs/sql/volumen_pruebas.sql usan PRODUCTO; carga separada en PHARMA_S14. Backend 7b0cfd7. | Cerrada |

Las incidencias se identificaron durante la inspección inicial y se registraron antes de aplicar sus correcciones. La medición inicial de Ventas conserva el contrato anterior de lista completa en las evidencias.

Verificación: matriz I-01 a I-08 cumplida; pruebas de auditoría de commit y rollback en Oracle; suite Angular y Maven sin fallos. Las siete pruebas E2E se ejecutan con un worker, retries=0 y --repeat-each=3. Los resultados concretos y las capturas se entregan en el informe PDF y en el reporte HTML de Playwright.

Los dos fallos iniciales de infraestructura de pruebas (componente ffmpeg ausente y servidor Angular no accesible desde el entorno aislado) se resolvieron instalando ffmpeg e iniciando el servidor local accesible a Chrome. No fueron fallos funcionales de la aplicación. Las pruebas unitarias anteriores se adaptaron para esperar la carga asíncrona de rxResource y consumir el contrato Page.
