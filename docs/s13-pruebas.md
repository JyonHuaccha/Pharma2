# Pruebas de optimización y producción de PharmaSoft

Rama: `feature/optimizacion-produccion-huaccha`. Proyecto frontend: `Pharma-frontend`.

## Ejecución

1. En el frontend, ejecutar `npm ci` si faltan dependencias y `npm run build -- --stats-json --verbose`.
2. Desde `pharma-frontend/deploy`, ejecutar `docker compose up -d`.
3. En el backend, compilar con `./mvnw.cmd clean package`. Las pruebas `CacheOracleRollbackTest` requieren Oracle y Redis; usan la base Redis 15 y revierten sus escrituras en Oracle.
4. Definir `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y una `JWT_SECRET` Base64 nueva de al menos 32 bytes. No guardar valores reales en Git. Generación en PowerShell:

```powershell
$bytesJwt = [System.Security.Cryptography.RandomNumberGenerator]::GetBytes(48)
$env:JWT_SECRET = [Convert]::ToBase64String($bytesJwt)
$env:DB_URL = 'jdbc:oracle:thin:@//localhost:1522/FREEPDB1'
$env:DB_USERNAME = 'PHARMADB'
# Establecer DB_PASSWORD en esta terminal sin guardarla en el repositorio.
.\deploy\iniciar-prod.ps1
```

5. Abrir `http://localhost:8081`. El API utiliza `/api/v1` desde el mismo origen; Actuator se consulta directamente en `http://localhost:8080/actuator`.
6. Importar `postman/PharmaSoft-S13-Produccion.json`. Completar `adminPassword` y `vendedorPassword` en Variables y enviar ambos logins. No mostrar los tokens en las capturas ni exportar la colección con tokens activos.

## Capturas de las 14 pruebas

### 01 Compilación
En la terminal del frontend ejecutar `npm run build -- --stats-json --verbose`. Capturar `Initial total` y la lista de archivos diferidos, incluida la fila `mas-vendidos`. Guardar también `browser-stats.json`; analizar sus tres entradas mayores con el analizador de esbuild. La búsqueda de imports de features en el núcleo debe excluir `*.spec.ts`, que no forman parte del paquete de producción.

### 02 Inicio de vendedor
Cerrar sesión e ingresar como vendedor. Antes de ingresar, abrir F12 → Red y activar la grabación. Limpiar la lista y abrir Inicio. En Red, filtrar por el nombre del archivo `mas-vendidos` que aparece en la compilación: no debe existir una descarga ni una petición a `productos-mas-vendidos`. Capturar el inicio y la lista de Red sin coincidencias. No activar precarga durante esta prueba.

### 03 Inicio de administrador
Ingresar como admin. En Inicio, antes de bajar, capturar el marcador `Productos más vendidos`. Desplazarse hasta él: se carga el componente y aparece la tabla. Si la descarga es demasiado rápida para capturar `Cargando reporte…`, aplicar temporalmente una red lenta en F12 y repetir. Guardar marcador, carga y tabla como 03.1, 03.2 y 03.3.

### 04 Caché del JavaScript
En F12 → Red, abrir un `main-*.js` de la SPA de Nginx, no la servida por `ng serve`. En Encabezados capturar `Cache-Control: public, max-age=31536000, immutable`. Si sale desde memoria, desactivar la caché solo para obtener una respuesta nueva y después volver a habilitarla.

### 05 Revalidación del HTML
En Red abrir la petición del documento o recargar `/index.html`. Capturar `Cache-Control: no-cache`. Este encabezado obliga a revalidar el HTML; no significa que los JS deban descargarse otra vez.

### 06 Recarga de una ruta Angular
Con «Disable cache» desmarcado, entrar a `http://localhost:8081/productos`, guardar la cifra de bytes transferidos y capturar la primera visita. Pulsar F5 y capturar la página, la ruta y la cifra transferida de la recarga. Los JS y CSS deben venir de memoria/disco. Las consultas del API siguen transfiriendo datos y no deben contarse como archivos estáticos cacheados. Usar el mismo navegador y procedimiento en ambas mediciones.

### 07 Dos consultas de categorías
Con el backend en `dev` para ver SQL, enviar las dos consultas de categorías de Postman consecutivamente. La primera consulta fría entra a `readAll`, registra `Inicio/Fin listar categorias` y consulta Oracle. La segunda debe ir a Redis. Para repetir una caché fría, borrar solo `categorias::todas` del Redis de la práctica. En `pharma-frontend/deploy`:

```powershell
docker compose exec -T redis redis-cli KEYS 'categorias::*'
docker compose exec -T redis redis-cli TTL 'categorias::todas'
```

Capturar ambas respuestas con sus tiempos, el log y el TTL cercano a 600 segundos. En `prod` el SQL está apagado deliberadamente; comparar entonces métricas de caché y el log de acceso.

### 08 Crear e invalidar
Consultar categorías para poblar la caché. Enviar `08 Crear categoría e invalidar caché` y luego volver a listar. Capturar el 201 y la lista con el nombre recién creado. Esta prueba manual conserva la categoría; si solo se quiere verificar sin dejar registros, ejecutar `CacheOracleRollbackTest#crearCategoriaInvalidaRedisYExisteDentroDeLaTransaccion` y capturar el resultado de la prueba automatizada, identificándolo como tal.

### 09 Venta e invalidación de productos
Consultar productos y anotar el stock de uno activo. Elegir un cliente activo y poner los IDs numéricos en las variables de Postman. Enviar la venta con cantidad 1 y luego consultar el producto. Capturar la venta 201 y el stock disminuido en una unidad. Esta venta manual queda registrada. La alternativa automatizada sin registros permanentes es `CacheOracleRollbackTest#ventaDescuentaStockEInvalidaProductosConRollback`.

### 10 Redis detenido
En `pharma-frontend/deploy`, ejecutar `docker compose stop redis`. Abrir Categorías o enviar su GET: sigue respondiendo con los datos de Oracle. Capturar el resultado y el WARN `Caché categorias no disponible`. Enviar el Health de Redis detenido: debe devolver 503 y `DOWN`. Restaurar inmediatamente con `docker compose start redis`; esperar `PONG` y confirmar Health `UP`.

### 11 Identificador de petición
Enviar GET categorías en Postman y abrir Headers de la respuesta. Copiar visualmente `X-Request-Id` y buscarlo en `logs/pharma-backend.log`. Capturar el encabezado y las líneas que llevan el mismo identificador. Cada petición recibe un identificador nuevo; un acierto de caché sigue generando su línea de acceso aunque no entre al servicio.

### 12 Health público
Enviar `12 Health público sin detalles` con Authorization → No Auth. Capturar el 200 y `"status":"UP"`, sin componentes ni detalles internos. Al habilitar probes, Spring Boot también puede mostrar los nombres de grupos `liveness` y `readiness`; esto no expone detalles de Oracle ni Redis. Con admin se permiten componentes; con vendedor siguen ocultos.

### 13 Métricas restringidas
Enviar `13 Métricas vendedor prohibidas` con el token vendedor: 403. Capturar URL, estado y respuesta sin mostrar el valor del token. Con admin, las métricas deben responder 200. Para `cache.gets` usar `tag=name:categorias` y `tag=result:hit`.

### 14 Logs sin secretos
Con el perfil `prod`, ejecutar desde la raíz del backend:

```powershell
Select-String -Path .\logs\pharma-backend.log -Pattern 'password|Bearer'
```

Capturar el comando y el siguiente prompt sin coincidencias. No confundir este archivo de producción con los logs de las pruebas automatizadas. Verificar además rotación de 10 MB e historial de 14 archivos en `application-prod.yaml`.

## Decisiones

- Las rutas ya usan `loadChildren` y `loadComponent`; no se habilitó `PreloadAllModules` para no descargar módulos no utilizados.
- El reporte mantiene estados diferentes para descarga del componente, consulta HTTP, error HTTP y lista vacía.
- Productos admite consulta paginada con `pagina`, `tamanio`, `ordenarPor`, `direccion` y `categoriaId`. El GET sin `pagina` conserva la lista completa para los formularios de venta.
- En Spring Data Redis 4 se usan escrituras inmediatas para que una inserción pendiente no reponga una entrada recién invalidada. La caché envuelve las transacciones de los servicios.
- TTL: 10 minutos. Se invalidan categorías en sus escrituras; cambiar una categoría invalida también productos; registrar una venta invalida productos.
- Los fallos de Redis producen advertencias y permiten consultar Oracle. Si hubo escrituras mientras Redis estaba caído, conviene limpiar las cachés al recuperarlo; el TTL limita la vigencia de cualquier entrada anterior.
- Los resultados del tamaño inicial deben interpretarse junto con la funcionalidad agregada: incorporar `@defer` puede añadir infraestructura al paquete inicial; el reporte sigue siendo un archivo diferido y se mide por separado.
