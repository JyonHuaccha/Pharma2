# Guía de capturas de la actividad autónoma S14

Esta guía sigue las partes A a F de la actividad. La implementación y sus pruebas automáticas ya fueron comprobadas; tú tomarás las capturas, realizarás los ensayos y grabarás el video. Después se elaborará el PDF final Huaccha_LP2_S14_Autonoma.pdf con tus evidencias. No se consideran completadas las partes manuales por tener esta guía.

## Carpeta y nombres de archivos

Guarda todas tus imágenes en C:\peack\output\documentos\S14 AUTONOMA capturas. Usa los nombres que indica cada prueba. Cuando haya dos o más imágenes del mismo caso, usa .1, .2 y .3; siguen siendo una sola prueba con un ACTOR y un resultado en el informe. Conserva imágenes originales PNG, sin tapar resultados ni retocar estados HTTP.

Antes de capturar mueve el puntero fuera del área y usa Win+Shift+S. No hace falta la herramienta de control remoto: tus capturas no tendrán su halo azul. Deja visibles URL o nombre de petición, resultado y usuario cuando sea relevante. Evita contraseña, Authorization y JWT completos. Para varios paneles usa dos capturas legibles en vez de una imagen de texto minúsculo.

## Inicio del proyecto

1. Abre C:\peack\pharmaSoft-main en IntelliJ y comprueba application-dev.yaml: puerto 1522/FREEPDB1 y el esquema que usarás. La verificación técnica se hizo en PHARMA_S14 aislado, pero tu ejecución normal usa PHARMADB salvo que la configures de otra forma. SQL Developer, Postman y SPA deben consultar la misma ejecución.
2. Reinicia tu backend para incorporar los nuevos eventos. Mi backend temporal de comprobación en 8082 se detendrá al terminar. Tu proceso ya abierto en 8080 se respeta; puede seguir ejecutando código anterior hasta que lo reinicies.
3. Oracle y Redis deben estar iniciados. En C:\peack\pharma-frontend ejecuta npm start y abre http://localhost:4200. Para estas capturas usa ese frontend actualizado; el contenedor Nginx de 8081 no incorpora un nuevo build por guardar archivos.
4. Ten a mano tus accesos admin, vendedor y almacen. El entorno local de Postman trae los valores conocidos; si cambiaste una contraseña, actualiza su variable privada. No subas ese archivo privado a GitHub.
5. Para la demo manual abre en SQL Developer docs/sql/preparar-demo.sql y ejecútalo con F5. Solo usa un esquema de desarrollo. No ejecuta una limpieza masiva. La carga de 5000 productos de la práctica no se repite si ya existe.
6. Cuando prepares stock por SQL, usa una base Redis/prefijo de demo sin claves anteriores o limpia únicamente esas claves propias; reiniciar Spring no elimina por sí solo las claves Redis. Para una edición desde la API, CacheEvict sí invalida productos.

## Importar y preparar Postman

1. Abre Postman de escritorio. En el espacio de trabajo pulsa Import (arriba a la izquierda o el botón + de importación).
2. Selecciona C:\peack\output\s14-autonoma\PharmaBackend.postman_collection.json y confirma Import. En Collections verás PharmaSoft S14 Autónoma Huaccha.
3. Pulsa Import otra vez y selecciona C:\peack\output\s14-autonoma\Pharma-S14-Autonoma.postman_environment.json. Arriba a la derecha elige el entorno Pharma S14 Autónoma local. No dejes No environment.
4. En Environments abre ese entorno. baseUrl debe ser http://localhost:8080, sin /api/v1 al final. Los campos passwordAdmin, passwordVendedor y passwordAlmacen son privados. Corrige solo si cambiaste las claves. Pulsa Save si tu versión lo requiere.
5. Abre 00 Inicio de sesión. Ejecuta LOGIN-ADMIN, LOGIN-VENDEDOR y LOGIN-ALMACEN, uno por uno con Send. Cada uno debe dar 200 y guardar el token automáticamente en Variables de la colección. No necesitas pegar Bearer a mano. No tomes capturas del token completo.
6. Abre 01 Preparar datos propios y ejecuta PRE-01, PRE-02, PRE-03 y PRE-04 una sola vez, en ese orden. Deben dar 201. Se crean una categoría única, un cliente con DNI único, un producto con stock 50 y otro sin stock. IDs, DNI y nombre quedan guardados automáticamente. Puedes tomar PRE-01.png a PRE-04.png como evidencia adicional.
7. Sigue PM-01 a PM-28 en orden, luego DEMO-01 a DEMO-03. En cada petición la pestaña Scripts > After response contiene los tests. Test Results o Tests aparece en el panel de respuesta; si no lo ves, expande la parte inferior arrastrando su separador hacia arriba.
8. Para ver las variables: haz clic en el nombre de la colección > Variables. clienteId, productoId, categoriaId, ventaId y requestIdVenta se llenan solos. No crees variables de entorno con esos mismos nombres porque ocultarían las de la colección. Evita escribirlos manualmente.
9. Los tokens duran 60 minutos. Si obtienes 401 en un caso que esperaba 200/201, repite solo los tres LOGIN y vuelve al caso; no necesitas ejecutar PRE otra vez. Las pruebas que esperan 401 deben seguir usando No Auth.
10. La colección también puede correrse de una vez: clic derecho > Run collection > Run. Mantén una iteración y el orden. Al terminar guarda PM-resumen.png con 38 requests y 86 tests correctos. El Runner crea nuevas ventas/datos al repetir PRE y PM-01; no ejecutes otra suite a la vez durante las comparaciones de rollback.

## Orden de trabajo

Prepara Postman y datos, realiza A, ejecuta la suite B y luego sus comprobaciones individuales. Toma las pruebas API PM en orden. Continúa con C, D, E y F. Los ensayos y las tres corridas manuales son distintos de las tres repeticiones automáticas.


## A-01 Auditoría protegida con filtros y paginación

ACTOR: ADMIN

RESULTADO ESPERADO: La pantalla muestra eventos, todos los filtros se reflejan en la URL y la página se conserva con F5.

1. Reinicia el backend desde IntelliJ para cargar los cambios y ejecuta npm start en el frontend. Abre http://localhost:4200/login. Usa admin y tu contraseña habitual. Si estás autenticado con otro rol, pulsa Cerrar sesión primero.

2. En el menú izquierdo abre Auditoría. Debe aparecer la tabla con Fecha, Usuario, Acción, Entidad, ID, Detalle y Request ID. Deja visible la barra de direcciones y el paginador. Guarda A-01.1.png.

3. Escribe vendedor en Usuario y elige VENTA_REGISTRADA en Acción. En Desde y Hasta coloca la fecha en que registraste tus ventas de prueba. Pulsa Buscar. Comprueba usuario=vendedor, accion=VENTA_REGISTRADA y pagina=0 en la URL. Guarda A-01.2.png con filtros, filas y URL.

4. Pulsa Limpiar. En el selector Filas elige 10. Pulsa Siguiente; debe mostrar Página 2 y pagina=1. Pulsa F5; debe conservar la misma página. Guarda A-01.3.png. Si no hay una segunda página, genera eventos con los login y PRE de Postman; no edites la base para inventar eventos.

5. Como comprobación adicional, pon Desde posterior a Hasta y pulsa Buscar: debe aparecer validación del rango sin ejecutar una consulta válida nueva.

Archivos: A-01.1.png, A-01.2.png, A-01.3.png.


## A-02 Evento nuevo de cliente registrado

ACTOR: VENDEDOR crea y ADMIN consulta

RESULTADO ESPERADO: El cliente creado tiene un evento CLIENTE_REGISTRADO con su ID y requestId, sin copiar DNI o correo al detalle.

1. Importa y prepara Postman como se indica al inicio de esta guía. Ejecuta LOGIN de los tres roles y PRE-01 a PRE-04 en orden. PRE-02 crea el cliente y guarda clienteId.

2. Abre PM-20 Verificar evento CLIENTE_REGISTRADO y pulsa Send. Debe responder 200 y pasar el test Evento de la entidad creada. En Body busca la fila con entidadId igual a clienteId.

3. En Angular entra como admin a Auditoría y selecciona CLIENTE_REGISTRADO. Pulsa Buscar. Localiza el ID de PRE-02. Guarda A-02.png con la acción, ID, usuario, detalle y requestId visibles.

Archivos: A-02.png.


## A-03 Evento nuevo de categoría registrada

ACTOR: ADMIN

RESULTADO ESPERADO: La categoría creada en PRE-01 tiene un evento CATEGORIA_REGISTRADA.

1. Ejecuta PM-21 en Postman y comprueba 200 y el test correcto. La variable categoriaId fue guardada automáticamente en PRE-01.

2. En Auditoría de Angular selecciona CATEGORIA_REGISTRADA y pulsa Buscar. Localiza entidadId igual al ID de la categoría de PRE-01.

3. Guarda A-03.png con el filtro, usuario admin, entidad Categoria, ID y requestId. No borres esa categoría, ya que los productos de demostración dependen de ella.

Archivos: A-03.png.


## A-04 Trazabilidad de una misma venta de demostración

ACTOR: VENDEDOR y ADMIN

RESULTADO ESPERADO: La venta muestra creado_por=vendedor y el mismo ID y requestId relacionan evento y log.

1. Prepara datos con docs/sql/preparar-demo.sql en el esquema usado por TU backend. La guía de inicio explica cómo verificarlo. El producto AAA Demo Huaccha S14 queda con stock 100 y precio 10; el cliente tiene DNI 74140001.

2. Inicia sesión como vendedor, abre Ventas > Nueva venta, busca DNI 74140001, selecciona el cliente y agrega dos unidades de AAA Demo Huaccha S14. Revisa y confirma sin imprimir automáticamente. Anota el ID real del comprobante; no uses un ID de ejemplo.

3. Abre SQL Developer con la misma conexión del backend. Abre docs/sql/trazabilidad-demo.sql, pulsa F5 e introduce ese ID. La primera consulta muestra ventas.id, fecha, cliente_id, total, estado y creado_por. Guarda A-04.1.png mostrando el ID y creado_por=vendedor.

4. En la segunda consulta localiza VENTA_REGISTRADA para el mismo ID. Copia su request_id y comprueba usuario=vendedor. En Angular como admin puedes mostrar también ese evento filtrando usuario, acción y fecha. Guarda A-04.2.png.

5. En la consola del backend de IntelliJ pulsa Ctrl+F y busca el request_id copiado. Deben verse Venta preparada | ventaId=... y POST /api/v1/ventas -> 201 con el mismo identificador entre corchetes. Guarda A-04.3.png. Si usas archivo, ejecuta en PowerShell: Get-Content C:\peack\pharmaSoft-main\logs\pharma-backend.log | Select-String -SimpleMatch "TU_REQUEST_ID". Sustituye TU_REQUEST_ID por el real.

6. En dev, si no se genera archivo de log, configura LOGGING_FILE_NAME=logs/pharma-backend.log antes de reiniciar. La consola ya contiene requestId. No captures Authorization, contraseña ni el token completo.

7. Si deseas comprobar la venta desde Postman, pon ventaId y requestIdVenta con esos valores reales en Variables de la colección y ejecuta PM-02 y PM-14. Si luego corres la colección completa, PM-01 los sustituirá por la venta que acaba de crear.

Archivos: A-04.1.png, A-04.2.png, A-04.3.png.


## B-01 Suite completa de catorce pruebas repetida tres veces

ACTOR: ADMIN VENDEDOR y ALMACENERO automatizados

RESULTADO ESPERADO: 42 passed, cero fallos, cero omitidas y sin reintentos.

1. Deja Oracle, Redis y el backend iniciados; usa una base de pruebas con al menos 5000 productos y 60 ventas para la demostración de volumen. Ejecutar la suite crea su propia categoría, cliente y producto, y ventas identificables como E2E; no borra ventas previas.

2. Abre PowerShell en C:\peack\pharma-frontend y ejecuta powershell -ExecutionPolicy Bypass -File .\scripts\ejecutar-e2e.ps1. Escribe las contraseñas de admin, vendedor y almacen cuando las solicite; no se mostrarán al teclear. El script utiliza Chrome instalado y ejecuta npx playwright test --repeat-each=3.

3. Espera hasta el resumen. Guarda B-01.1.png mostrando el comando y 42 passed con la duración real. Si falla, conserva trace.zip, identifica la causa y corrígela antes de ejecutar otra vez; no tomes como final un resultado parcial.

4. Ejecuta npx playwright show-report. Abre la dirección que muestre la terminal (habitualmente http://localhost:9323). Guarda B-01.2.png con All 42 y Passed 42; si no cabe, añade B-01.3.png desplazando el informe.

5. El resumen actualizado queda en frontend/docs/resultados-pruebas.md. No sustituyas la fecha o duración por las de mi verificación: el informe debe conservar las de tu ejecución si vas a usar tus capturas.

Archivos: B-01.1.png, B-01.2.png.


## E2E-01 Ruta protegida sin sesión

ACTOR: Sin sesión

RESULTADO ESPERADO: /ventas lleva al login con returnUrl.

1. En una ventana nueva o tras Cerrar sesión, escribe http://localhost:4200/ventas.

2. Espera el login. La URL debe contener returnUrl=%2Fventas. No ingreses todavía credenciales.

3. Captura login y URL completos.

Archivos: E2E-01.png.


## E2E-02 Credenciales incorrectas

ACTOR: ADMIN con clave errónea

RESULTADO ESPERADO: Se muestra Usuario o contraseña incorrectos.

1. En /login escribe admin y una contraseña incorrecta, por ejemplo incorrecta. Pulsa Ingresar.

2. Espera el mensaje rojo Usuario o contraseña incorrectos. El servidor devuelve 401, no un 500.

3. Captura el mensaje sin mostrar contraseñas.

Archivos: E2E-02.png.


## E2E-03 Regreso a la ruta solicitada

ACTOR: VENDEDOR

RESULTADO ESPERADO: Después del login se muestra Consulta de ventas.

1. Cierra sesión y escribe /ventas. En el login resultante ingresa como vendedor con la contraseña correcta.

2. Espera la tabla de Consulta de ventas. La dirección debe terminar en /ventas y el usuario debe ser vendedor.

3. Captura cabecera, usuario y tabla.

Archivos: E2E-03.png.


## E2E-04 Venta y stock actualizado

ACTOR: VENDEDOR

RESULTADO ESPERADO: Una venta de dos unidades descuenta exactamente dos y Productos lo muestra sin F5.

1. En Productos filtra la categoría S14 Demo Huaccha y anota el stock del producto de demo; toma E2E-04.1.png.

2. Registra dos unidades desde Ventas > Nueva venta para el cliente de demo. Desmarca imprimir antes de confirmar. Guarda E2E-04.2.png del comprobante.

3. Usa el enlace Productos del menú, sin F5. Filtra la misma categoría y comprueba stockAntes-2. Guarda E2E-04.3.png. No afirmes que siempre será 98 si ya vendiste antes sin preparar de nuevo.

Archivos: E2E-04.1.png, E2E-04.2.png, E2E-04.3.png.


## E2E-05 Paginación y recarga

ACTOR: ADMIN

RESULTADO ESPERADO: La segunda página permanece al recargar y se pide una página al servidor.

1. En Productos usa 20 en Filas y pulsa Siguiente. Comprueba Página 2 y pagina=1 en la dirección.

2. Pulsa F5. Debe permanecer Página 2. Guarda la captura con URL, tabla y paginador.

3. Si quieres añadir Network: F12 > Network > Fetch/XHR; selecciona productos?pagina=1... y muestra 200 y el contrato contenido, totalElementos, totalPaginas.

Archivos: E2E-05.png.


## E2E-06 Almacenero no accede a Ventas

ACTOR: ALMACENERO

RESULTADO ESPERADO: Ventas no aparece en el menú y /ventas muestra acceso denegado.

1. Cierra sesión e ingresa con usuario almacen. El rol visible debe ser ALMACENERO.

2. Verifica que el menú no tiene Ventas. Escribe http://localhost:4200/ventas directamente.

3. Captura el menú y la página 403 Acceso denegado.

Archivos: E2E-06.png.


## E2E-07 Vendedor consulta sin editar productos

ACTOR: VENDEDOR

RESULTADO ESPERADO: No aparecen Nuevo producto, Editar ni Dar de baja.

1. Cierra sesión e ingresa como vendedor. Abre Productos.

2. Verifica que la tabla está cargada y que no hay acciones de escritura ni botón de nuevo producto.

3. Captura el usuario, encabezados y filas.

Archivos: E2E-07.png.


## E2E-08 Stock cambia después de abrir el formulario

ACTOR: VENDEDOR con cambio concurrente ADMIN

RESULTADO ESPERADO: El POST devuelve 409 y no deja venta parcial.

1. Esta prueba automatizada ya prepara una condición real de concurrencia: abre el formulario con stock y, antes de confirmar, lo cambia a cero mediante API como admin. No simula la respuesta.

2. La forma más directa de tu captura es abrir el informe HTML de B-01, buscar E2E-08, abrir una ejecución Passed y desplegar el adjunto 08-stock-insuficiente. Captura el caso aprobado y la imagen del mensaje Stock insuficiente.

3. Para repetir manualmente: entra vendedor, elige cliente y producto con stock y cantidad 2. En Postman consulta ese producto como admin, conserva todos sus campos y envía PUT con stock 0. Vuelve al formulario sin refrescar, revisa y confirma. Debe mostrarse Stock insuficiente; registra E2E-08.png. Al terminar restablece solamente el producto de demo.

4. Si solo escribes una cantidad mayor al stock ya conocido, Angular impide continuar; eso valida el formulario, pero no demuestra el 409 del servidor. PM-07 y PM-25 son las comprobaciones API correspondientes.

Archivos: E2E-08.png.


## E2E-09 Venta sin productos

ACTOR: VENDEDOR

RESULTADO ESPERADO: Revisar venta deshabilitado y API con detalles vacíos devuelve 400.

1. Abre Ventas > Nueva venta y deja el detalle sin productos. Puedes elegir un cliente, pero no agregues productos.

2. El botón Revisar venta debe estar deshabilitado. Captura formulario y botón.

3. Complementa con PM-03 en Postman: detalles=[] devuelve 400.

Archivos: E2E-09.png.


## E2E-10 Cliente nuevo y venta inmediata

ACTOR: VENDEDOR

RESULTADO ESPERADO: El cliente recién guardado aparece en la venta sin reiniciar la aplicación.

1. Abre Clientes > Nuevo. Usa un DNI de prueba de ocho dígitos que no exista, nombre Nuevo Demo, apellido Huaccha y correo único @example.test. Pulsa Guardar y confirma Guardar.

2. Usa Ventas > Nueva venta inmediatamente. Busca ese mismo DNI y selecciona al cliente; guarda E2E-10.1.png con el cliente nuevo seleccionado.

3. Agrega un producto de demo con stock y confirma una unidad. Guarda E2E-10.2.png del comprobante del cliente nuevo.

Archivos: E2E-10.1.png, E2E-10.2.png.


## E2E-11 Filtrar ventas desde la tercera página

ACTOR: VENDEDOR

RESULTADO ESPERADO: Cambiar fechas reinicia a página 1 y la fecha queda en la URL.

1. Abre /ventas?pagina=2&tamanio=20; debe mostrar Página 3. Guarda E2E-11.1.png. Si hay menos de 41 ventas, usa el esquema de volumen de S14; no basta con escribir pagina=2 en una tabla vacía.

2. En Desde selecciona la fecha de tus ventas de demo y pulsa Buscar. Debe quedar pagina=0 y Página 1, con desde en la URL.

3. Pulsa F5 y comprueba que conserva el filtro. Guarda E2E-11.2.png.

Archivos: E2E-11.1.png, E2E-11.2.png.


## E2E-12 Admin encuentra una venta en Auditoría

ACTOR: ADMIN

RESULTADO ESPERADO: La bitácora contiene la venta real y su requestId.

1. Usa la venta de A-04 o PM-01. Inicia sesión admin y abre Auditoría.

2. Filtra usuario vendedor, acción VENTA_REGISTRADA y la fecha de la venta. Localiza el ID exacto; no basta con mostrar otra venta de la tabla.

3. Captura filtro, ID, usuario, detalle y requestId. Puedes reutilizar la captura A-04.2 si muestra todo; no necesitas una imagen duplicada para el mismo resultado.

Archivos: E2E-12.png.


## E2E-13 Vendedor sin acceso a Auditoría

ACTOR: VENDEDOR

RESULTADO ESPERADO: No hay opción en menú y la URL directa muestra 403 de la SPA.

1. Cierra sesión, inicia vendedor y deja visible el menú.

2. Escribe http://localhost:4200/auditoria. Debe mostrar Acceso denegado, no la tabla ni Página no encontrada.

3. Captura menú, usuario y mensaje. PM-17 demuestra además el 403 de la API.

Archivos: E2E-13.png.


## E2E-14 Token alterado en sessionStorage

ACTOR: VENDEDOR

RESULTADO ESPERADO: El backend rechaza la firma y la SPA vuelve al login con aviso de expiración.

1. La alternativa sin consola es abrir en el informe HTML de Playwright E2E-14 > ejecución Passed > adjunto 14-token-alterado. Guarda una captura con el nombre del caso y el resultado.

2. Para hacerlo manual: inicia vendedor en Productos; pulsa F12 > Application (puede estar dentro de >>) > Session storage > http://localhost:4200. Selecciona pharmasoft.sesion.

3. Copia el JSON completo a un editor local. Dentro de token hay tres partes separadas por puntos. Cambia solo la primera letra de la tercera parte: si es a, usa b; en otro caso usa a. Conserva el resto del JSON y no lo compartas.

4. Reemplaza el Value de pharmasoft.sesion con el JSON modificado y pulsa Enter. Recarga la página. Espera que se ejecute la petición de productos: devuelve 401 y la SPA va a /login?expirada=1...

5. Captura únicamente la pantalla de login con el aviso Su sesión expiró y la URL. Cierra Application para no mostrar el token. La prueba de firma alterada no equivale a demostrar un token vencido; el guion trata ambos por separado.

Archivos: E2E-14.png.


## PM-01 Registrar venta y guardar ID y requestId

ACTOR: vendedor

RESULTADO ESPERADO: HTTP 201 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-01 Registrar venta y guardar ID y requestId». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: POST {{baseUrl}}/api/v1/ventas.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. La respuesta debe mostrar id, total 20, creadoPor vendedor y los detalles. Copia también la cabecera X-Request-Id si deseas una captura adicional. La venta descuenta dos unidades y reemplaza ventaId/requestIdVenta.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-01.png. Si los tests no caben, toma PM-01.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Si aparece un valor {{sin resolver}}, vuelve a ejecutar los LOGIN y PRE necesarios en orden. No inventes IDs.

Archivos: PM-01.png.


## PM-02 Consultar venta registrada y creadoPor

ACTOR: vendedor

RESULTADO ESPERADO: HTTP 200 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-02 Consultar venta registrada y creadoPor». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/ventas/{{ventaId}}.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. Comprueba que el ID coincide con PM-01 y que creadoPor es vendedor. Este GET no crea otra venta.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-02.png. Si los tests no caben, toma PM-02.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Si aparece un valor {{sin resolver}}, vuelve a ejecutar los LOGIN y PRE necesarios en orden. No inventes IDs.

Archivos: PM-02.png.


## PM-03 Venta sin productos devuelve 400

ACTOR: vendedor

RESULTADO ESPERADO: HTTP 400 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-03 Venta sin productos devuelve 400». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: POST {{baseUrl}}/api/v1/ventas.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. En Body de la petición se ve detalles vacío; en la respuesta deben verse los errores de validación y 400.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-03.png. Si los tests no caben, toma PM-03.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-03.png.


## PM-04 Ventas sin token devuelve 401

ACTOR: Sin token

RESULTADO ESPERADO: HTTP 401 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-04 Ventas sin token devuelve 401». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/ventas/{{ventaId}}.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. En Authorization debe estar No Auth. No agregues el token manualmente; 401 es lo esperado.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-04.png. Si los tests no caben, toma PM-04.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-04.png.


## PM-05 Almacenero no registra ventas 403

ACTOR: almacen

RESULTADO ESPERADO: HTTP 403 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-05 Almacenero no registra ventas 403». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: POST {{baseUrl}}/api/v1/ventas.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. La petición utiliza tokenAlmacen. El JSON es válido, pero el rol no puede registrar ventas y devuelve 403.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-05.png. Si los tests no caben, toma PM-05.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-05.png.


## PM-06 Venta inexistente devuelve 404

ACTOR: vendedor

RESULTADO ESPERADO: HTTP 404 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-06 Venta inexistente devuelve 404». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/ventas/900000000000.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. El ID muy grande es intencional y no existe. Debe indicar venta no encontrada y 404.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-06.png. Si los tests no caben, toma PM-06.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-06.png.


## PM-07 Stock insuficiente devuelve 409

ACTOR: vendedor

RESULTADO ESPERADO: HTTP 409 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-07 Stock insuficiente devuelve 409». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: POST {{baseUrl}}/api/v1/ventas.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. Se usa el producto creado con stock cero. Debe decir Stock insuficiente y responder 409.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-07.png. Si los tests no caben, toma PM-07.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-07.png.


## PM-08 Productos paginados devuelve 200

ACTOR: admin

RESULTADO ESPERADO: HTTP 200 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-08 Productos paginados devuelve 200». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/productos?pagina=0&tamanio=20.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. Abre Body Pretty JSON y muestra pagina, tamanio, totalElementos y contenido. Debe haber como máximo 20 filas.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-08.png. Si los tests no caben, toma PM-08.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Si aparece un valor {{sin resolver}}, vuelve a ejecutar los LOGIN y PRE necesarios en orden. No inventes IDs.

Archivos: PM-08.png.


## PM-09 Producto inválido devuelve 400

ACTOR: admin

RESULTADO ESPERADO: HTTP 400 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-09 Producto inválido devuelve 400». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: POST {{baseUrl}}/api/v1/productos.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. El nombre vacío, precio negativo, stock negativo y categoría ausente deben aparecer en las validaciones. No corrijas el JSON: es una prueba negativa.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-09.png. Si los tests no caben, toma PM-09.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-09.png.


## PM-10 Productos sin token devuelve 401

ACTOR: Sin token

RESULTADO ESPERADO: HTTP 401 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-10 Productos sin token devuelve 401». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/productos?pagina=0&tamanio=20.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. Se usa No Auth y se espera 401, incluso si otro tab de Postman tiene una sesión correcta.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-10.png. Si los tests no caben, toma PM-10.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-10.png.


## PM-11 Vendedor no crea productos 403

ACTOR: vendedor

RESULTADO ESPERADO: HTTP 403 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-11 Vendedor no crea productos 403». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: POST {{baseUrl}}/api/v1/productos.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. Se usa tokenVendedor y un JSON válido. Debe ser 403 porque el vendedor no administra catálogo.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-11.png. Si los tests no caben, toma PM-11.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-11.png.


## PM-12 Producto inexistente devuelve 404

ACTOR: admin

RESULTADO ESPERADO: HTTP 404 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-12 Producto inexistente devuelve 404». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/productos/900000000000.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. El ID deliberadamente inexistente debe devolver 404 Producto no encontrado.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-12.png. Si los tests no caben, toma PM-12.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-12.png.


## PM-13 Tamaño 500 devuelve 409

ACTOR: admin

RESULTADO ESPERADO: HTTP 409 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-13 Tamaño 500 devuelve 409». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/productos?pagina=0&tamanio=500.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. El tamaño 500 supera el máximo 100 y debe responder 409. No cambies el tamaño para que sea 200.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-13.png. Si los tests no caben, toma PM-13.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-13.png.


## PM-14 Encontrar la venta en auditoría con el mismo requestId

ACTOR: admin

RESULTADO ESPERADO: HTTP 200 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-14 Encontrar la venta en auditoría con el mismo requestId». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/auditoria?usuario=vendedor&accion=VENTA_REGISTRADA&pagina=0&tamanio=100.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. El test compara el evento con ventaId y requestIdVenta. Expande la fila con entidadId igual a ventaId. Si muestra otros eventos, busca el ID exacto.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-14.png. Si los tests no caben, toma PM-14.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Si aparece un valor {{sin resolver}}, vuelve a ejecutar los LOGIN y PRE necesarios en orden. No inventes IDs.

Archivos: PM-14.png.


## PM-15 Fecha no válida devuelve 400

ACTOR: admin

RESULTADO ESPERADO: HTTP 400 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-15 Fecha no válida devuelve 400». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/auditoria?desde=no-es-fecha.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. desde=no-es-fecha debe ser 400 por conversión inválida. El formato del cuerpo puede ser el error estándar de Spring; la cabecera X-Request-Id sigue presente.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-15.png. Si los tests no caben, toma PM-15.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-15.png.


## PM-16 Auditoría sin token devuelve 401

ACTOR: Sin token

RESULTADO ESPERADO: HTTP 401 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-16 Auditoría sin token devuelve 401». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/auditoria.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. Sin token se obtiene 401. Auditoría no es una ruta pública.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-16.png. Si los tests no caben, toma PM-16.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-16.png.


## PM-17 Auditoría vendedor devuelve 403

ACTOR: vendedor

RESULTADO ESPERADO: HTTP 403 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-17 Auditoría vendedor devuelve 403». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/auditoria.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. Con tokenVendedor se obtiene 403, porque falta USUARIOS_ADMINISTRAR.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-17.png. Si los tests no caben, toma PM-17.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-17.png.


## PM-18 Ruta de auditoría inexistente devuelve 404

ACTOR: admin

RESULTADO ESPERADO: HTTP 404 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-18 Ruta de auditoría inexistente devuelve 404». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/auditoria/no-existe.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. La ruta /auditoria/no-existe no tiene controlador y debe ser 404 con admin autenticado. No se agregó un endpoint ficticio para provocar este código.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-18.png. Si los tests no caben, toma PM-18.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-18.png.


## PM-19 Rango de fechas invertido devuelve 409

ACTOR: admin

RESULTADO ESPERADO: HTTP 409 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-19 Rango de fechas invertido devuelve 409». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/auditoria?desde=2026-12-31&hasta=2026-01-01.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. Las fechas son válidas, pero desde es posterior a hasta: regla de negocio 409.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-19.png. Si los tests no caben, toma PM-19.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-19.png.


## PM-20 Verificar evento CLIENTE_REGISTRADO

ACTOR: admin

RESULTADO ESPERADO: HTTP 200 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-20 Verificar evento CLIENTE_REGISTRADO». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/auditoria?accion=CLIENTE_REGISTRADO&pagina=0&tamanio=100.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. El test debe encontrar el evento CLIENTE_REGISTRADO cuyo entidadId corresponde a PRE-02.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-20.png. Si los tests no caben, toma PM-20.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Si aparece un valor {{sin resolver}}, vuelve a ejecutar los LOGIN y PRE necesarios en orden. No inventes IDs.

Archivos: PM-20.png.


## PM-21 Verificar evento CATEGORIA_REGISTRADA

ACTOR: admin

RESULTADO ESPERADO: HTTP 200 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-21 Verificar evento CATEGORIA_REGISTRADA». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/auditoria?accion=CATEGORIA_REGISTRADA&pagina=0&tamanio=100.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. El test debe encontrar el evento CATEGORIA_REGISTRADA cuyo entidadId corresponde a PRE-01.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-21.png. Si los tests no caben, toma PM-21.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Si aparece un valor {{sin resolver}}, vuelve a ejecutar los LOGIN y PRE necesarios en orden. No inventes IDs.

Archivos: PM-21.png.


## PM-22 Guardar stock antes del rechazo

ACTOR: admin

RESULTADO ESPERADO: HTTP 200 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-22 Guardar stock antes del rechazo». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/productos/{{productoId}}.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. Anota el stock del producto de PRE-03. Se guarda en stockAntes para la comparación posterior.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-22.png. Si los tests no caben, toma PM-22.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Si aparece un valor {{sin resolver}}, vuelve a ejecutar los LOGIN y PRE necesarios en orden. No inventes IDs.

Archivos: PM-22.png.


## PM-23 Guardar cantidad de ventas del cliente

ACTOR: admin

RESULTADO ESPERADO: HTTP 200 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-23 Guardar cantidad de ventas del cliente». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/ventas/buscar?clienteId={{clienteId}}&pagina=0&tamanio=20.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. Se guarda el total de ventas del cliente antes del intento fallido. No ejecutes otra venta del mismo cliente hasta PM-28.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-23.png. Si los tests no caben, toma PM-23.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Si aparece un valor {{sin resolver}}, vuelve a ejecutar los LOGIN y PRE necesarios en orden. No inventes IDs.

Archivos: PM-23.png.


## PM-24 Guardar cantidad de eventos de venta

ACTOR: admin

RESULTADO ESPERADO: HTTP 200 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-24 Guardar cantidad de eventos de venta». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/auditoria?usuario=vendedor&accion=VENTA_REGISTRADA&pagina=0&tamanio=100.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. Se guarda el total de eventos VENTA_REGISTRADA del vendedor. Durante PM-24 a PM-28 no ejecutes una suite E2E ni otra venta, para que la comparación no mezcle nuevas operaciones.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-24.png. Si los tests no caben, toma PM-24.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Si aparece un valor {{sin resolver}}, vuelve a ejecutar los LOGIN y PRE necesarios en orden. No inventes IDs.

Archivos: PM-24.png.


## PM-25 Rechazar segunda línea y revertir toda la operación

ACTOR: vendedor

RESULTADO ESPERADO: HTTP 409 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-25 Rechazar segunda línea y revertir toda la operación». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: POST {{baseUrl}}/api/v1/ventas.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. La primera línea puede descontarse dentro de la transacción, pero la segunda no tiene stock. El 409 revierte toda la operación y guarda requestIdRechazo.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-25.png. Si los tests no caben, toma PM-25.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Un 400, 401, 403, 404 o 409 esperado también es una prueba correcta. El resultado de Tests debe ser Passed; no debe responder 200 en estos casos negativos.

Archivos: PM-25.png.


## PM-26 Stock se conserva después del rollback

ACTOR: admin

RESULTADO ESPERADO: HTTP 200 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-26 Stock se conserva después del rollback». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/productos/{{productoId}}.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. El test Stock sin cambios compara con stockAntes. Debe ser 200 con el mismo valor que PM-22.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-26.png. Si los tests no caben, toma PM-26.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Si aparece un valor {{sin resolver}}, vuelve a ejecutar los LOGIN y PRE necesarios en orden. No inventes IDs.

Archivos: PM-26.png.


## PM-27 No se agregó una venta fallida

ACTOR: admin

RESULTADO ESPERADO: HTTP 200 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-27 No se agregó una venta fallida». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/ventas/buscar?clienteId={{clienteId}}&pagina=0&tamanio=20.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. El total de ventas del cliente debe coincidir con ventasAntes. No se agregó una cabecera fallida.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-27.png. Si los tests no caben, toma PM-27.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Si aparece un valor {{sin resolver}}, vuelve a ejecutar los LOGIN y PRE necesarios en orden. No inventes IDs.

Archivos: PM-27.png.


## PM-28 No existe evento del requestId rechazado

ACTOR: admin

RESULTADO ESPERADO: HTTP 200 y todas las comprobaciones verdes.

1. En la colección PharmaSoft S14 Autónoma Huaccha abre la petición «PM-28 No existe evento del requestId rechazado». Conserva el entorno Pharma S14 Autónoma local seleccionado. Petición: GET {{baseUrl}}/api/v1/auditoria?usuario=vendedor&accion=VENTA_REGISTRADA&pagina=0&tamanio=100.

2. No cambies el JSON, el token ni los parámetros que vienen preparados. Pulsa Send. Espera la respuesta y comprueba el código HTTP en la parte superior derecha del panel de respuesta.

3. Deben pasar Eventos sin cambios y Sin evento de la venta revertida. Si alguien vendió al mismo tiempo, repite todo el bloque desde PM-22 en un entorno tranquilo; no edites los valores para forzar el resultado.

4. Abre Body > Pretty > JSON en la respuesta. Ajusta la división de paneles para que se vea el nombre de la petición, método, URL, estado HTTP y resultado. Guarda PM-28.png. Si los tests no caben, toma PM-28.2.png en Test Results / Tests mostrando todas las comprobaciones verdes.

5. Si aparece un valor {{sin resolver}}, vuelve a ejecutar los LOGIN y PRE necesarios en orden. No inventes IDs.

Archivos: PM-28.png.


## C-01 Registro de incidencias y cierre

ACTOR: ADMIN y estudiante

RESULTADO ESPERADO: El registro contiene once incidencias con causas, commits, regresiones y cero críticas o altas abiertas.

1. Abre C:\peack\pharmaSoft-main\docs\incidencias.md en IntelliJ o VS Code y activa la vista previa Markdown.

2. Toma C-01.1.png mostrando la tabla inicial de seis incidencias y parte de sus correcciones.

3. Desplázate a INC-07 a INC-11; toma C-01.2.png o más imágenes si hace falta para ver causa, corrección y prueba. Al final toma C-01.3.png con el resumen por severidad y módulo.

4. Si quieres inspeccionar una traza privada fallida de verificación, se conserva en C:\peack\tmp\s14a. No la publiques con tokens visibles. El informe solo debe afirmar que una incidencia está cerrada si se comprobó la corrección.

Archivos: C-01.1.png, C-01.2.png, C-01.3.png.


## D-01 Dos ensayos completos cronometrados

ACTOR: Estudiante

RESULTADO ESPERADO: Dos ensayos reales del guion, con tiempo y desvíos anotados; objetivo de diez minutos.

1. Abre docs/guion-demostracion.md y prepara los seis bloques. Abre un cronómetro visible en el teléfono o la aplicación Reloj de Windows.

2. Haz la demostración completa desde el contexto hasta calidad. Inicia el cronómetro antes de hablar y detenlo al terminar. No sustituyas este tiempo por la duración de Playwright.

3. Completa la primera fila de C:\peack\output\s14-autonoma\registro-ensayos.csv con fecha, duración mm:ss y desvíos. Repite un segundo ensayo separado y completa la segunda fila.

4. Guarda D-01.1.png y D-01.2.png con cada registro y el cronómetro. Si excediste diez minutos, anota el valor real y ajusta el guion para el siguiente intento.

Archivos: D-01.1.png, D-01.2.png.


## D-02 Tres corridas manuales del flujo desde datos preparados

ACTOR: VENDEDOR

RESULTADO ESPERADO: Tres verificaciones manuales registran venta real, duración y stock 100 a 98 sin desvíos ocultos.

1. Antes de cada corrida, ejecuta preparar-demo.sql en tu esquema de desarrollo. Asegura coherencia de la caché como explica el checklist; un cambio por SQL no ejecuta CacheEvict.

2. Cierra sesión. Inicia el cronómetro. Entra vendedor, selecciona cliente DNI 74140001, producto AAA Demo Huaccha S14 y cantidad 2, confirma y revisa comprobante.

3. Navega por menú a Productos, filtra categoría S14 Demo Huaccha y comprueba stock 98. Detén el cronómetro. Anota ID real, fecha, duración, stock y desvíos en registro-flujo-manual.csv.

4. Guarda D-02.1.png. Prepara de nuevo el producto y realiza la segunda y tercera corrida independientes; guarda D-02.2.png y D-02.3.png.

5. La preparación restablece solo el producto de demo y conserva las ventas. No borres toda tu base ni cambies resultados anteriores. Tres capturas del mismo comprobante no son tres corridas.

Archivos: D-02.1.png, D-02.2.png, D-02.3.png.


## D-03 Video de respaldo de máximo diez minutos

ACTOR: Estudiante

RESULTADO ESPERADO: Video completo y enlace accesible para el docente.

1. Prepara el guion, pestañas y datos. Abre Recortes y selecciona Grabación de pantalla, o usa OBS si ya lo tienes. Activa micrófono para explicar las decisiones.

2. Graba una demostración completa con los seis bloques. Comprueba que dura como máximo diez minutos y que se escucha tu explicación.

3. Guarda el video original. Súbelo al servicio que uses para compartir y permite acceso al docente por enlace. Prueba el enlace desde otra sesión.

4. Pega el enlace en C:\peack\output\s14-autonoma\video-enlace.txt. Guarda D-03.png con el reproductor, título y duración, sin exponer claves. Para el informe necesito el enlace real; una captura no sustituye el video.

Archivos: D-03.png.


## D-04 Caché Redis y salud del sistema

ACTOR: ADMIN

RESULTADO ESPERADO: Actuator informa salud y la métrica de caché refleja consultas reales.

1. En Postman abre DEMO-01 y pulsa Send. Debe ser 200 con status UP; si el usuario admin está autenticado verás detalles de los componentes. Guarda D-04.1.png.

2. Ejecuta PM-08 dos veces seguidas sin cambiar la URL ni vender entre ambas. Así la segunda consulta puede usar la misma clave de caché.

3. Ejecuta DEMO-03. Debe ser 200 con measurements para cache.gets y etiquetas de productos/hit. Guarda D-04.2.png. Si el valor es cero, confirma que Redis está habilitado y que repetiste exactamente la misma consulta; no modifiques el número del resultado.

4. DEMO-02 lista las métricas disponibles. Usa esa petición para comprobar el nombre si tu versión de Actuator cambia.

Archivos: D-04.1.png, D-04.2.png.


## E-01 Índice arquitectura y defensa técnica

ACTOR: Estudiante

RESULTADO ESPERADO: docs tiene índice navegable, arquitectura y diez respuestas referidas al código real.

1. Abre C:\peack\pharmaSoft-main\docs\README.md. Es el índice que esta guía exige expresamente, no un README agregado a la raíz. Guarda E-01.1.png con sus enlaces.

2. Abre docs/arquitectura.png y guarda E-01.2.png si quieres evidenciar la lámina de contexto. Ya es una imagen elaborada del diagrama, no una captura simulada del sistema.

3. Abre docs/defensa-tecnica.md y lee las diez respuestas comparando los archivos indicados. Reescribe lo que necesites para explicarlo con tus propias palabras; no memorices una frase sin entenderla. Guarda E-01.3.png mostrando algunas preguntas y sus rutas.

4. Las carpetas u2-spa-segura y s13-optimizacion conservan informes previos. La carpeta u1-backend se conserva vacía de imágenes porque indicaste que ya no tienes esos originales. No necesitas buscarlas ni recrearlas.

Archivos: E-01.1.png, E-01.2.png, E-01.3.png.


## F-01 Commits y etiqueta de la versión de demo

ACTOR: Estudiante

RESULTADO ESPERADO: Ambos repositorios tienen commits con fechas reales y etiqueta v1.0-demo en la versión entregada.

1. En PowerShell ejecuta git -C C:\peack\pharmaSoft-main log -8 --date=short --pretty=format:"%h %ad %s". Captura los commits feat, test y docs de esta actividad como F-01.1.png.

2. Ejecuta git -C C:\peack\pharma-frontend log -8 --date=short --pretty=format:"%h %ad %s". Guarda F-01.2.png.

3. Ejecuta git -C C:\peack\pharmaSoft-main show v1.0-demo --no-patch y el mismo comando para pharma-frontend. Guarda F-01.3.png con ambos resultados o dos capturas.

4. El historial conserva trabajo real previo del 8 y 12 de noviembre y el de esta autónoma del 15. Los commits nuevos de esta sesión se hicieron el mismo día; no se alteran fechas para aparentar trabajo en varios días. Si el docente exige tres días exclusivamente para esta autónoma, necesitarás continuar con cambios reales en días posteriores.

5. Cuando recibas el documento final con tus imágenes, verifica enlaces de repositorios, índice y video; tú lo subes al aula virtual.

Archivos: F-01.1.png, F-01.2.png, F-01.3.png.


## Archivos que necesitaré para preparar el informe final

Cuando termines, deja las imágenes en la carpeta asignada y completa registro-ensayos.csv, registro-flujo-manual.csv y video-enlace.txt en output/s14-autonoma. Avísame que están listas. Si alguna prueba no coincide con lo esperado, guarda la evidencia real y consúltala antes de repetir; el informe reflejará lo que ocurrió.

El PDF final incorporará un único bloque ACTOR, RESULTADO ESPERADO y RESULTADO OBTENIDO por prueba, aunque esta tenga varias imágenes. También incluirá enlaces al backend, frontend, índice docs/README.md y video, y distinguirá resultados automáticos comprobados de tus verificaciones manuales.
