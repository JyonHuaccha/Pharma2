# Resultados de pruebas de la actividad autónoma

Ejecución real: 15/11/2026 23:01:35, America/Bogota. Código backend 672c9ef50baf32693ca8ae97b4902dee3c3c669f; frontend ecec0ecb89a9188d3babb184dcd7325e6060b3d0. El commit de la suite se creó al terminar la ejecución sobre el mismo contenido verificado. Los posteriores cambios documentales no modifican ese código.

## Resultado general

| Verificación | Aprobadas | Fallidas | Duración |
| --- | --- | --- | --- |
| Maven verify con Oracle y Redis aislados | 61 | 0 | 37.011 s |
| Angular unitarias Vitest | 119 | 0 | 9.70 s |
| Playwright 14 casos por 3 repeticiones | 42 | 0 | 51.267 s |
| Postman CLI peticiones | 38 | 0 | 4.577 s |
| Postman aserciones | 86 | 0 | Incluida arriba |

El build de producción también terminó correctamente. Playwright: workers=1, retries=0, maxFailures=1 y sin waitForTimeout. El backend de verificación usa PHARMA_S14 en 8082; el build real se sirvió en 4210 con proxy hacia esa API. La ejecución del usuario en 8080 se mantuvo intacta.

## Repeticiones e informe

Se ejecutó npx playwright test --repeat-each=3. Los 14 casos pasaron tres veces seguidas, 0 flaky y 0 skipped. [Detalle de las 42 ejecuciones](evidencias/s14-estabilizacion/resultados-e2e.md). Abrir el HTML local desde el frontend con npx playwright show-report; su archivo es playwright-report/index.html. [Resumen JSON sin credenciales](evidencias/s14-estabilizacion/resumen-resultados.json).

Los fallos previos no se ocultaron con reintentos: se detuvo la corrida, se inspeccionaron trazas y se corrigieron sus causas. INC-07, INC-08 e INC-09 describen los problemas de CORS, etiqueta accesible y comparación del total. La corrida final fue posterior a esas correcciones.

## Postman

Colección: [PharmaBackend](postman/PharmaBackend.postman_collection.json). Contiene login, preparación propia y estados 200/201, 400, 401, 403, 404 y 409 en Ventas, Productos y Auditoría, además de rollback y Actuator. El ambiente privado con contraseñas no se incorpora a Git.

| Petición | HTTP real | Aserciones correctas | Tiempo ms |
| --- | --- | --- | --- |
| LOGIN-ADMIN Iniciar sesión admin | 200 | 2 | 121 |
| LOGIN-VENDEDOR Iniciar sesión vendedor | 200 | 2 | 95 |
| LOGIN-ALMACEN Iniciar sesión almacen | 200 | 2 | 143 |
| PRE-01 Crear categoría de demostración | 201 | 2 | 47 |
| PRE-02 Crear cliente de demostración | 201 | 2 | 55 |
| PRE-03 Crear producto activo con stock 50 | 201 | 2 | 38 |
| PRE-04 Crear producto sin stock | 201 | 2 | 45 |
| PM-01 Registrar venta y guardar ID y requestId | 201 | 3 | 60 |
| PM-02 Consultar venta registrada y creadoPor | 200 | 3 | 26 |
| PM-03 Venta sin productos devuelve 400 | 400 | 2 | 19 |
| PM-04 Ventas sin token devuelve 401 | 401 | 2 | 8 |
| PM-05 Almacenero no registra ventas 403 | 403 | 2 | 20 |
| PM-06 Venta inexistente devuelve 404 | 404 | 2 | 26 |
| PM-07 Stock insuficiente devuelve 409 | 409 | 2 | 28 |
| PM-08 Productos paginados devuelve 200 | 200 | 3 | 40 |
| PM-09 Producto inválido devuelve 400 | 400 | 2 | 23 |
| PM-10 Productos sin token devuelve 401 | 401 | 2 | 9 |
| PM-11 Vendedor no crea productos 403 | 403 | 2 | 16 |
| PM-12 Producto inexistente devuelve 404 | 404 | 2 | 23 |
| PM-13 Tamaño 500 devuelve 409 | 409 | 2 | 25 |
| PM-14 Encontrar la venta en auditoría con el mismo requestId | 200 | 3 | 29 |
| PM-15 Fecha no válida devuelve 400 | 400 | 2 | 19 |
| PM-16 Auditoría sin token devuelve 401 | 401 | 2 | 11 |
| PM-17 Auditoría vendedor devuelve 403 | 403 | 2 | 18 |
| PM-18 Ruta de auditoría inexistente devuelve 404 | 404 | 2 | 27 |
| PM-19 Rango de fechas invertido devuelve 409 | 409 | 2 | 20 |
| PM-20 Verificar evento CLIENTE_REGISTRADO | 200 | 3 | 24 |
| PM-21 Verificar evento CATEGORIA_REGISTRADA | 200 | 3 | 23 |
| PM-22 Guardar stock antes del rechazo | 200 | 2 | 30 |
| PM-23 Guardar cantidad de ventas del cliente | 200 | 2 | 27 |
| PM-24 Guardar cantidad de eventos de venta | 200 | 2 | 21 |
| PM-25 Rechazar segunda línea y revertir toda la operación | 409 | 2 | 39 |
| PM-26 Stock se conserva después del rollback | 200 | 3 | 20 |
| PM-27 No se agregó una venta fallida | 200 | 3 | 35 |
| PM-28 No existe evento del requestId rechazado | 200 | 4 | 26 |
| DEMO-01 Estado de salud Oracle y Redis | 200 | 2 | 30 |
| DEMO-02 Métricas disponibles | 200 | 2 | 18 |
| DEMO-03 Métrica de aciertos de caché | 200 | 2 | 19 |

Los JSON originales de Postman y las trazas se conservan localmente para diagnóstico; pueden contener tokens. Aquí se publica solo el resumen de resultados. Las capturas manuales se incorporarán después de que el estudiante las tome.

Cambio posterior de configuración: 4305a17 incorpora requestId en el archivo de desarrollo. El código frontend se formateó en 00aed13 sin alterar comportamiento. El JAR y el build de producción se compilaron otra vez.
