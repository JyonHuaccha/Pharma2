# Guia de capturas de seguridad S10

Importa `postman/PharmaSoft-S10-Seguridad.json`. Cada carpeta tiene peticiones preparadas; usa Send individualmente. No ejecutes toda la coleccion con Runner.

1. Reinicia tu API para cargar los tres endpoints nuevos. Verifica que no siga una instancia anterior.
2. Ejecuta A-01. Despues entra en 00 Preparacion: GET roles, GET usuarios, GET empleados y las consultas del catalogo.
3. En Variables de la coleccion configura productoId, categoriaId y clienteId con IDs existentes. Elige empleadoLibreId solo si GET empleados indica estado true y tieneUsuario false. Verifica los IDs inexistentes.
4. Ejecuta A a U y los casos propios con los roles originales. Haz T-03 y T-04 con reinicios y restaura secreto y duracion antes de continuar. Ejecuta B en orden y restaura password, roles y estado donde se indica.
5. Captura con Win + Shift + S. Incluye metodo, URL, Authorization, codigo y respuesta; si no caben, toma dos imagenes. Usa solo tokens de desarrollo y no exportes sus valores al repositorio.
6. En Scripts > Post-response se validan HTTP y mensajes, se guardan tokens de login y se registran resultadosS10. Para ver el resumen sin tokens, abre Postman Console y ejecuta desde un script temporal: console.log(pm.collectionVariables.get("resultadosS10")).
7. Actualiza matriz y resumen C1 a partir de lo medido. Un caso con varias peticiones solo pasa si todas coinciden y tiene evidencia.

## CORS desde la SPA

Desde http://localhost:4200 abre F12, Network y Preserve log. En Console ejecuta lo siguiente; pega solo el token de desarrollo cuando aparezca el cuadro de entrada:

```javascript
fetch("http://localhost:8080/api/v1/productos", {
  headers: { Authorization: "Bearer " + prompt("Pega tu token de desarrollo") }
}).then(r => console.log("HTTP del GET", r.status));
```

## SQL para U 02

Sustituye mhuaman_s10 por usernameNuevo si elegiste otro. Son consultas de lectura; no recrean tablas ni borran cuentas.
```sql
SELECT id, username, empleado_id, LENGTH(password) AS longitud_hash, SUBSTR(password, 1, 7) AS prefijo_bcrypt FROM usuarios WHERE username = 'mhuaman_s10';
SELECT u.username, r.nombre AS rol FROM usuarios u JOIN usuario_roles ur ON ur.usuario_id = u.id JOIN roles r ON r.id = ur.rol_id WHERE u.username = 'mhuaman_s10';
```

## Clave temporal para T 03

Genera la clave localmente y guardala fuera del repositorio. En PowerShell:
```powershell
$claveTemporalS10 = New-Object byte[] 48
$generadorS10 = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$generadorS10.GetBytes($claveTemporalS10)
$generadorS10.Dispose()
[Convert]::ToBase64String($claveTemporalS10)
```

La clave debe tener al menos 32 bytes y estar en Base64. Conserva una copia privada del valor original y restauralo al terminar.

## A-01 Login de los cuatro roles

Precondicion: API iniciada y cuentas de clase activas. Contraseñas originales; si las cambiaste, usa las actuales.

1. Envía los cuatro logins de esta carpeta. Sus scripts guardan los tokens en variables de la colección.
2. Compara roles y permisos. Supervisor debe reunir los permisos de VENDEDOR y ALMACENERO sin REPORTES_VER.

Peticiones preparadas:

- Login admin: `POST {{baseUrl}}/auth/login` → 200
- Login vendedor: `POST {{baseUrl}}/auth/login` → 200
- Login almacen: `POST {{baseUrl}}/auth/login` → 200
- Login supervisor: `POST {{baseUrl}}/auth/login` → 200

Resultado esperado: Cuatro respuestas 200 con token, roles y permisos. Supervisor contiene ambos roles y la unión de permisos.

Captura: Cuatro capturas A-01_admin.png, A-01_vendedor.png, A-01_almacen.png y A-01_supervisor.png. Muestra POST, URL, 200 y las listas de roles y permisos; puedes plegar el token.

**Insertar aqui las capturas de A-01.**

Resultado obtenido: pendiente. Estado: pendiente.

## A-02 Credenciales incorrectas con mensaje uniforme

Precondicion: No requiere token.

1. Envía un login con usuario inexistente y otro con vendedor y contraseña incorrecta.
2. Compara literalmente el campo message de las dos respuestas.

Peticiones preparadas:

- Login usuario_que_no_existe_s10: `POST {{baseUrl}}/auth/login` → 401. Usuario o contraseña incorrectos
- Contraseña incorrecta: `POST {{baseUrl}}/auth/login` → 401. Usuario o contraseña incorrectos

Resultado esperado: Ambos 401 y message Usuario o contraseña incorrectos.

Captura: A-02_inexistente.png y A-02_password.png. Incluye ambos cuerpos de petición y los mensajes 401 para compararlos.

**Insertar aqui las capturas de A-02.**

Resultado obtenido: pendiente. Estado: pendiente.

## A-03 Cuenta inactiva

Precondicion: La cuenta inactivo mantiene estado false.

1. Envía el login de inactivo con su contraseña correcta.
2. Compara su mensaje con A-02; no debe revelar que la cuenta existe.

Peticiones preparadas:

- Login inactivo: `POST {{baseUrl}}/auth/login` → 401. Usuario o contraseña incorrectos

Resultado esperado: 401 y Usuario o contraseña incorrectos.

Captura: A-03_postman.png con username inactivo, contraseña de clase, 401 y el mensaje genérico.

**Insertar aqui las capturas de A-03.**

Resultado obtenido: pendiente. Estado: pendiente.

## A-04 Usuario escrito en mayúsculas

Precondicion: Vendedor activo y contraseña vigente.

1. Envía VENDEDOR en mayúsculas.
2. La regla elegida es un login sin distinguir mayúsculas porque UsuarioRepository usa findByUsernameIgnoreCase. El username devuelto debe ser vendedor.

Peticiones preparadas:

- Login con mayúsculas: `POST {{baseUrl}}/auth/login` → 200

Resultado esperado: 200, username vendedor y rol VENDEDOR.

Captura: A-04_postman.png donde se vea VENDEDOR en el cuerpo y vendedor en la respuesta.

**Insertar aqui las capturas de A-04.**

Resultado obtenido: pendiente. Estado: pendiente.

## T-01 Token sin el prefijo Bearer

Precondicion: A-01 ya guardó tokenVendedor.

1. En esta petición Authorization está en No Auth y se envía el encabezado Authorization manualmente con {{tokenVendedor}}.
2. No escribas Bearer. Evita duplicar Authorization con la pestaña de autenticación.

Peticiones preparadas:

- Sin Bearer: `GET {{baseUrl}}/productos` → 401. Debe iniciar sesión para acceder a este recurso

Resultado esperado: 401 y Debe iniciar sesión para acceder a este recurso.

Captura: T-01_postman.png. En Headers debe verse Authorization con la variable sin prefijo y la respuesta 401.

**Insertar aqui las capturas de T-01.**

Resultado obtenido: pendiente. Estado: pendiente.

## T-02 Payload alterado sin volver a firmar

Precondicion: Token válido y reciente del vendedor.

1. El script previo modifica sub dentro del payload y conserva la firma original.
2. Envía la petición y comprueba que la API rechaza el cambio aunque el JWT conserve sus tres segmentos.

Peticiones preparadas:

- Payload alterado: `GET {{baseUrl}}/productos` → 401. El token no es válido

Resultado esperado: 401 y El token no es válido.

Captura: T-02_postman.png con Bearer {{tokenPayloadAlterado}}, GET productos y el 401. Complementa T-02_script.png con el script previo; no cambies el secreto para este caso.

**Insertar aqui las capturas de T-02.**

Resultado obtenido: pendiente. Estado: pendiente.

## T-03 Token firmado con el secreto anterior

Precondicion: Antes de cambiar nada, guarda una copia privada de la configuración local y del secreto original. No hagas commit de claves temporales.

1. Envía Guardar token anterior con el secreto original y conserva tokenAnterior.
2. Cambia solo app.jwt.secret por una clave temporal Base64 de al menos 32 bytes y reinicia la API.
3. Envía Probar token anterior sin volver a obtenerlo: debe fallar por firma.
4. Restaura el secreto original, reinicia y repite los logins A-01 antes de continuar. Nunca incluyas secretos en capturas.

Peticiones preparadas:

- Guardar token anterior: `POST {{baseUrl}}/auth/login` → 200
- Probar token anterior: `GET {{baseUrl}}/productos` → 401. El token no es válido

Resultado esperado: Login inicial 200 y posterior GET con tokenAnterior 401 El token no es válido.

Captura: T-03_emision.png y T-03_postman.png. La segunda muestra tokenAnterior y el 401. Añade T-03_restaurado.png con un login 200 después de restaurar; no muestres el YAML ni claves.

**Insertar aqui las capturas de T-03.**

Resultado obtenido: pendiente. Estado: pendiente.

## T-04 Expiración del token

Precondicion: Secreto original restaurado; permite reiniciar la API local.

1. Cambia únicamente app.jwt.expiracion-minutos a 1 y reinicia.
2. Envía Emitir token de un minuto; expiraEnSegundos debe ser 60. No reutilices un JWT emitido antes del reinicio.
3. Espera 130 segundos y envía Probar token vencido sin renovar tokenCorto.
4. Restaura la duración 60, reinicia y vuelve a ejecutar A-01.

Peticiones preparadas:

- Emitir token de un minuto: `POST {{baseUrl}}/auth/login` → 200
- Probar token vencido: `GET {{baseUrl}}/productos` → 401. El token ha expirado. Inicie sesión nuevamente

Resultado esperado: Emisión 200 con 60 segundos; después 401 El token ha expirado. Inicie sesión nuevamente.

Captura: T-04_emision.png muestra 60 segundos; T-04_postman.png el 401 por expiración; T-04_restaurado.png un login final con 3600 segundos.

**Insertar aqui las capturas de T-04.**

Resultado obtenido: pendiente. Estado: pendiente.

## T-05 Contenido y duración del JWT

Precondicion: Duración restaurada a 60 y cuenta vendedor con su rol original.

1. Envía el login de esta carpeta.
2. Abre Visualize o Visualization en la respuesta. Revisa sub, authorities, iat y exp. La resta exp - iat debe ser 3600.
3. Comprueba que no se incluyen contraseña, hash ni secreto. Decodificar no verifica la firma; esa verificación la realiza JwtService en la API.

Peticiones preparadas:

- Login y visualizar claims: `POST {{baseUrl}}/auth/login` → 200

Resultado esperado: 200, claims correctos y duración de 3600 segundos sin datos sensibles.

Captura: T-05_postman.png con método, URL, 200 y visualización del payload y su duración.

**Insertar aqui las capturas de T-05.**

Resultado obtenido: pendiente. Estado: pendiente.

## P-01 Vendedor no modifica categorías

Precondicion: Vendedor conserva solo el rol VENDEDOR; define categoriaId con un ID existente.

1. Envía POST, PUT y DELETE con tokenVendedor.
2. Los tres deben detenerse en seguridad con 403 antes de escribir. Si alguno devuelve 2xx, registra un hallazgo y verifica los datos afectados.

Peticiones preparadas:

- POST categoria: `POST {{baseUrl}}/categorias` → 403. No tiene permisos para realizar esta operación
- PUT categoria: `PUT {{baseUrl}}/categorias/{{categoriaId}}` → 403. No tiene permisos para realizar esta operación
- DELETE categoria: `DELETE {{baseUrl}}/categorias/{{categoriaId}}` → 403. No tiene permisos para realizar esta operación

Resultado esperado: Tres respuestas 403 No tiene permisos para realizar esta operación.

Captura: P-01_post.png, P-01_put.png y P-01_delete.png con método, URL, Bearer del vendedor y 403.

**Insertar aqui las capturas de P-01.**

Resultado obtenido: pendiente. Estado: pendiente.

## P-02 Almacenero no consulta clientes ni registra ventas

Precondicion: Token de almacen. Define clienteId y productoId usando registros existentes para la petición de venta.

1. Envía GET clientes y POST ventas con tokenAlmacen.
2. No basta un 400 o 409: ambos deben responder 403 antes de acceder a la operación.

Peticiones preparadas:

- GET clientes: `GET {{baseUrl}}/clientes` → 403. No tiene permisos para realizar esta operación
- POST venta: `POST {{baseUrl}}/ventas` → 403. No tiene permisos para realizar esta operación

Resultado esperado: Ambos 403 No tiene permisos para realizar esta operación.

Captura: P-02_clientes.png y P-02_ventas.png con las dos respuestas 403 y el token del almacenero.

**Insertar aqui las capturas de P-02.**

Resultado obtenido: pendiente. Estado: pendiente.

## P-03 Reportes protegidos con PreAuthorize

Precondicion: Tokens recientes de vendedor y almacen.

1. Envía el mismo GET de productos más vendidos con cada token.
2. Los rechazos corresponden a @PreAuthorize en ReporteController.

Peticiones preparadas:

- Reporte vendedor: `GET {{baseUrl}}/reportes/productos-mas-vendidos` → 403. No tiene permisos para realizar esta operación
- Reporte almacen: `GET {{baseUrl}}/reportes/productos-mas-vendidos` → 403. No tiene permisos para realizar esta operación

Resultado esperado: Ambos 403 No tiene permisos para realizar esta operación.

Captura: P-03_vendedor.png y P-03_almacen.png con GET del reporte y ambas respuestas 403.

**Insertar aqui las capturas de P-03.**

Resultado obtenido: pendiente. Estado: pendiente.

## P-04 Administrador consulta todos los módulos

Precondicion: tokenAdmin vigente; las consultas pueden devolver listas vacías y seguir siendo válidas.

1. Envía las ocho consultas de esta carpeta. No crean ni eliminan registros.
2. Comprueba 200 en cada módulo; una lista vacía no es un fallo de autorización.

Peticiones preparadas:

- GET categorias: `GET {{baseUrl}}/categorias` → 200
- GET productos: `GET {{baseUrl}}/productos` → 200
- GET clientes: `GET {{baseUrl}}/clientes` → 200
- GET ventas: `GET {{baseUrl}}/ventas` → 200
- GET usuarios: `GET {{baseUrl}}/usuarios` → 200
- GET empleados: `GET {{baseUrl}}/empleados` → 200
- GET roles: `GET {{baseUrl}}/roles` → 200
- GET reportes/productos-mas-vendidos: `GET {{baseUrl}}/reportes/productos-mas-vendidos` → 200

Resultado esperado: 200 en categorías, productos, clientes, ventas, usuarios, empleados, roles y reportes.

Captura: P-04_01.png a P-04_08.png. Una captura por petición mostrando método, URL, tokenAdmin, 200 y respuesta.

**Insertar aqui las capturas de P-04.**

Resultado obtenido: pendiente. Estado: pendiente.

## U-01 Validaciones al crear usuarios

Precondicion: Ejecuta Preparación para obtener IDs. Se prueban errores de DTO antes de escribir.

1. Envía primero contraseña abcdefgh (sin número).
2. Envía después una contraseña válida y rolIds vacío. Revisa validationErrors en cada respuesta.

Peticiones preparadas:

- Password sin numero: `POST {{baseUrl}}/usuarios` → 400. Existen errores de validación
- Roles vacios: `POST {{baseUrl}}/usuarios` → 400. Existen errores de validación

Resultado esperado: Ambos 400 Existen errores de validación; error de password en el primero y rolIds en el segundo.

Captura: U-01_password.png y U-01_roles.png incluyendo Body, 400 y el campo concreto de validationErrors.

**Insertar aqui las capturas de U-01.**

Resultado obtenido: pendiente. Estado: pendiente.

## U-02 Una cuenta por empleado y hash BCrypt

Precondicion: Comprueba GET empleados: para el alta 201 necesitas un empleado activo con tieneUsuario false. Si María ya tiene cuenta desde la práctica, no borres su cuenta: usa otro empleado libre o registra el caso pendiente hasta prepararlo.

1. Envía Empleado con cuenta usando empleadoConCuentaId: debe ser 409.
2. Configura empleadoLibreId con el empleado libre elegido y usernameNuevo con un nombre no usado. Envía Alta de empleado libre una sola vez.
3. Comprueba 201 sin password ni hash y verifica las dos consultas SQL de la guía de capturas. Si no tienes un empleado libre, no inventes un 201.

Peticiones preparadas:

- Empleado con cuenta: `POST {{baseUrl}}/usuarios` → 409
- Alta de empleado libre: `POST {{baseUrl}}/usuarios` → 201

Resultado esperado: 409 por empleado ya vinculado; luego 201 sin contraseña, hash BCrypt de 60 caracteres y relación en usuario_roles.

Captura: U-02_conflicto.png, U-02_alta.png y U-02_sql.png. SQL debe mostrar solo longitud y prefijo BCrypt y la relación de roles; no publiques el hash completo.

**Insertar aqui las capturas de U-02.**

Resultado obtenido: pendiente. Estado: pendiente.

## C-01 Preflight desde el origen de la SPA

Precondicion: Frontend servido en http://localhost:4200 y token de desarrollo vigente.

1. Abre la SPA y F12. En Network o Red activa Preserve log y filtra productos.
2. Desde Console ejecuta el fetch indicado en la preparación de esta guía. Pega el token solo en el cuadro de entrada local.
3. Inspecciona OPTIONS y el GET real. OPTIONS debe devolver 200 y Allow-Origin http://localhost:4200; GET debe ser 200 o 401, sin bloqueo de CORS.
4. El OPTIONS de Postman es una comprobación adicional. Postman no aplica la política del navegador y no sustituye la captura en Red.

Peticiones preparadas:

- Preflight complementario: `OPTIONS {{baseUrl}}/productos` → 200

Resultado esperado: OPTIONS 200 con origen y Authorization permitidos; GET real 200 o 401 sin error CORS.

Captura: C-01_options.png con Network, URL, código y encabezados CORS; C-01_get.png con el GET real y su código. No uses solo Postman para dar este caso por aprobado.

**Insertar aqui las capturas de C-01.**

Resultado obtenido: pendiente. Estado: pendiente.

## A-05 Usuario vacío

Precondicion: Caso propio del grupo Autenticación. No requiere token.

1. Envía username vacío con password no vacío.
2. Distingue validación de formato (400) de credenciales incorrectas (401).

Peticiones preparadas:

- Username vacio: `POST {{baseUrl}}/auth/login` → 400. Existen errores de validación

Resultado esperado: 400 Existen errores de validación y validationErrors.username.

Captura: A-05_postman.png con username vacío y validationErrors.username.

**Insertar aqui las capturas de A-05.**

Resultado obtenido: pendiente. Estado: pendiente.

## T-06 Token malformado

Precondicion: Caso propio del grupo Token.

1. Envía Bearer con el texto esto-no-es-un-jwt.
2. Comprueba 401 y no un error interno 500.

Peticiones preparadas:

- JWT malformado: `GET {{baseUrl}}/productos` → 401. El token no es válido

Resultado esperado: 401 El token no es válido.

Captura: T-06_postman.png con Authorization malformado y el 401.

**Insertar aqui las capturas de T-06.**

Resultado obtenido: pendiente. Estado: pendiente.

## P-05 Vendedor no administra cuentas

Precondicion: Caso propio del grupo Permisos. Vendedor mantiene su rol original.

1. Envía GET usuarios con tokenVendedor.
2. Debe ser 403: el usuario está autenticado pero carece de USUARIOS_ADMINISTRAR.

Peticiones preparadas:

- GET usuarios vendedor: `GET {{baseUrl}}/usuarios` → 403. No tiene permisos para realizar esta operación

Resultado esperado: 403 No tiene permisos para realizar esta operación.

Captura: P-05_postman.png con tokenVendedor, GET usuarios y el mensaje 403.

**Insertar aqui las capturas de P-05.**

Resultado obtenido: pendiente. Estado: pendiente.

## U-03 Nombre de usuario con espacio

Precondicion: Caso propio del grupo Usuarios. IDs obtenidos en Preparación.

1. Envía username con un espacio y contraseña válida.
2. La validación del DTO debe responder antes de comprobar duplicados o escribir en la base.

Peticiones preparadas:

- Username invalido: `POST {{baseUrl}}/usuarios` → 400. Existen errores de validación

Resultado esperado: 400 y validationErrors.username indicando caracteres permitidos.

Captura: U-03_postman.png con el espacio en username, 400 y el error de validación.

**Insertar aqui las capturas de U-03.**

Resultado obtenido: pendiente. Estado: pendiente.

## B-01 Cambio correcto y vigencia del token anterior

Precondicion: Vendedor activo y contraseña inicial Vendedor2026*. Conserva tokenVendedor del login inicial.

1. Envía Cambiar password con tokenVendedor: debe ser 204 sin cuerpo.
2. Prueba login con la clave anterior (401) y la nueva (200). La nueva se guarda en tokenNuevaClave; no sobrescribas tokenVendedor.
3. Consulta auth/me con el token anterior: sigue siendo 200 hasta su vencimiento.
4. Envía Restaurar password con la clave nueva como actual y la original como nueva. Repite A-01 para continuar con las contraseñas de clase.

Peticiones preparadas:

- Cambiar password: `PUT {{baseUrl}}/auth/password` → 204
- Login clave anterior: `POST {{baseUrl}}/auth/login` → 401. Usuario o contraseña incorrectos
- Login clave nueva: `POST {{baseUrl}}/auth/login` → 200
- Token anterior sigue valido: `GET {{baseUrl}}/auth/me` → 200
- Restaurar password: `PUT {{baseUrl}}/auth/password` → 204

Resultado esperado: 204 en cambio; login anterior 401 y nuevo 200; token anterior aún 200; restauración 204.

Captura: B-01_cambio.png, B-01_anterior.png, B-01_nueva.png, B-01_token.png y B-01_restaurado.png. En el 204 debe verse que no hay cuerpo.

**Insertar aqui las capturas de B-01.**

Resultado obtenido: pendiente. Estado: pendiente.

## B-02 Contraseña actual incorrecta

Precondicion: Contraseña original restaurada después de B-01.

1. Envía una contraseña actual incorrecta y una nueva válida.
2. El resultado debe ser 409; no debe cambiar la contraseña.

Peticiones preparadas:

- Password actual incorrecta: `PUT {{baseUrl}}/auth/password` → 409. La contraseña actual no es correcta

Resultado esperado: 409 La contraseña actual no es correcta.

Captura: B-02_postman.png con cuerpo, token y el mensaje 409.

**Insertar aqui las capturas de B-02.**

Resultado obtenido: pendiente. Estado: pendiente.

## B-03 Nueva contraseña igual a la actual

Precondicion: Vendedor mantiene Vendedor2026*.

1. Envía la misma clave en passwordActual y passwordNueva.
2. Comprueba que no se admite reutilizar la clave vigente.

Peticiones preparadas:

- Password repetida: `PUT {{baseUrl}}/auth/password` → 409. La nueva contraseña debe ser distinta de la actual

Resultado esperado: 409 La nueva contraseña debe ser distinta de la actual.

Captura: B-03_postman.png con los dos campos iguales y el 409.

**Insertar aqui las capturas de B-03.**

Resultado obtenido: pendiente. Estado: pendiente.

## B-04 Validación y autenticación del cambio

Precondicion: Contraseña original restaurada. Estas peticiones no cambian la contraseña.

1. Envía cada cuerpo inválido: nueva sin número, demasiado corta, mayor de 72 caracteres y passwordActual vacía.
2. Envía además un cuerpo válido sin token: debe ser 401 y no 204.

Peticiones preparadas:

- Nueva sin numero: `PUT {{baseUrl}}/auth/password` → 400. Existen errores de validación
- Nueva corta: `PUT {{baseUrl}}/auth/password` → 400. Existen errores de validación
- Nueva mayor de 72: `PUT {{baseUrl}}/auth/password` → 400. Existen errores de validación
- Actual vacia: `PUT {{baseUrl}}/auth/password` → 400. Existen errores de validación
- Cambio sin token: `PUT {{baseUrl}}/auth/password` → 401. Debe iniciar sesión para acceder a este recurso

Resultado esperado: 400 en los cuatro cuerpos inválidos con validationErrors; sin token 401 Debe iniciar sesión para acceder a este recurso.

Captura: B-04_01.png a B-04_05.png con cada Body y su 400 o 401. Muestra el campo de validationErrors en las cuatro primeras.

**Insertar aqui las capturas de B-04.**

Resultado obtenido: pendiente. Estado: pendiente.

## B-05 Reemplazar roles y aplicar el nuevo permiso al mismo token

Precondicion: Preparación guardó los IDs de roles y rolesOriginalesVendedor. Vendedor conserva solo VENDEDOR. Elige productoId existente; no registres ventas durante la secuencia.

1. Sin renovar tokenVendedor, usa tokenAdmin para asignar VENDEDOR y ALMACENERO al vendedor.
2. Consulta el producto con tokenVendedor. El script guarda un cuerpo que conserva todos sus valores actuales.
3. Envía PUT del mismo producto con ese cuerpo y el mismo tokenVendedor: debe ser 200 por el permiso nuevo.
4. Restaura los roles originales con tokenAdmin. Repite PUT usando el mismo tokenVendedor: ahora debe ser 403. No vuelvas a iniciar sesión entre estas peticiones.

Peticiones preparadas:

- Agregar rol almacen: `PUT {{baseUrl}}/usuarios/{{usuarioVendedorId}}/roles` → 200
- Leer producto actual: `GET {{baseUrl}}/productos/{{productoId}}` → 200
- PUT producto con permiso nuevo: `PUT {{baseUrl}}/productos/{{productoId}}` → 200
- Restaurar roles originales: `PUT {{baseUrl}}/usuarios/{{usuarioVendedorId}}/roles` → 200
- PUT despues de retirar permiso: `PUT {{baseUrl}}/productos/{{productoId}}` → 403. No tiene permisos para realizar esta operación

Resultado esperado: Roles 200 con ambos roles y permisos; PUT producto 200 con el mismo JWT; tras restaurar, el mismo PUT responde 403.

Captura: B-05_roles.png muestra roles y permisos actualizados; B-05_permitido.png el PUT 200; B-05_retirado.png el PUT 403 con la misma variable tokenVendedor. No renueves el token durante la prueba.

**Insertar aqui las capturas de B-05.**

Resultado obtenido: pendiente. Estado: pendiente.

## B-06 Validaciones y protección del administrador

Precondicion: ADMIN autenticado y IDs de preparación correctos. usuarioInexistenteId y rolInexistenteId deben estar verificados como ausentes.

1. Envía rolIds vacío y luego con null: deben responder 400.
2. Prueba usuario inexistente y rol inexistente: deben responder 404.
3. Intenta quitar ADMIN a tu propia cuenta asignándote solo VENDEDOR: debe ser 409 sin modificar roles.

Peticiones preparadas:

- Roles vacios: `PUT {{baseUrl}}/usuarios/{{usuarioVendedorId}}/roles` → 400. Existen errores de validación
- Rol nulo: `PUT {{baseUrl}}/usuarios/{{usuarioVendedorId}}/roles` → 400. Existen errores de validación
- Usuario inexistente: `PUT {{baseUrl}}/usuarios/{{usuarioInexistenteId}}/roles` → 404
- Rol inexistente: `PUT {{baseUrl}}/usuarios/{{usuarioVendedorId}}/roles` → 404
- Auto retiro de ADMIN: `PUT {{baseUrl}}/usuarios/{{usuarioAdminId}}/roles` → 409. No puede quitarse a sí mismo el rol ADMIN

Resultado esperado: 400 vacío/nulo, 404 usuario/rol ausente y 409 No puede quitarse a sí mismo el rol ADMIN.

Captura: B-06_01.png a B-06_05.png con los cinco cuerpos y códigos, incluyendo los mensajes 404 y 409.

**Insertar aqui las capturas de B-06.**

Resultado obtenido: pendiente. Estado: pendiente.

## B-07 Desactivar y reactivar conservando el historial

Precondicion: A-01 obtuvo tokenVendedor; mantenlo sin renovarlo durante la desactivación. Vendedor activo y contraseña original.

1. Con tokenAdmin envía estado false al vendedor: 200 y estado false.
2. Con el tokenVendedor ya emitido consulta productos: 401 El usuario está deshabilitado.
3. Intenta su login: 401 con el mismo mensaje genérico de A-02.
4. Con tokenAdmin reactiva al vendedor. El mismo token anterior vuelve a ser aceptado si no ha vencido; compruébalo con GET productos. La cuenta conserva ID y roles.

Peticiones preparadas:

- Desactivar vendedor: `PATCH {{baseUrl}}/usuarios/{{usuarioVendedorId}}/estado` → 200
- Token de desactivado: `GET {{baseUrl}}/productos` → 401. El usuario está deshabilitado
- Login desactivado: `POST {{baseUrl}}/auth/login` → 401. Usuario o contraseña incorrectos
- Reactivar vendedor: `PATCH {{baseUrl}}/usuarios/{{usuarioVendedorId}}/estado` → 200
- Token despues de reactivar: `GET {{baseUrl}}/productos` → 200

Resultado esperado: PATCH 200; token previo 401 usuario deshabilitado; login 401 genérico; reactivación 200 y GET 200.

Captura: B-07_01.png a B-07_05.png. Deben verse el mismo ID de usuario, los dos estados y las respuestas al token previo. Termina dejando vendedor activo.

**Insertar aqui las capturas de B-07.**

Resultado obtenido: pendiente. Estado: pendiente.

## B-08 Protecciones de estado y acceso administrativo

Precondicion: Vendedor activo de nuevo y admin con su rol original.

1. Intenta que admin se desactive: 409.
2. Prueba un usuario inexistente: 404.
3. Envía estado null y luego un cuerpo vacío: ambos 400.
4. Con tokenVendedor intenta PATCH estado y PUT roles: ambos 403, sin modificar cuentas.

Peticiones preparadas:

- Auto desactivacion: `PATCH {{baseUrl}}/usuarios/{{usuarioAdminId}}/estado` → 409. No puede desactivarse a sí mismo
- Usuario inexistente: `PATCH {{baseUrl}}/usuarios/{{usuarioInexistenteId}}/estado` → 404
- Estado nulo: `PATCH {{baseUrl}}/usuarios/{{usuarioVendedorId}}/estado` → 400. Existen errores de validación
- Estado ausente: `PATCH {{baseUrl}}/usuarios/{{usuarioVendedorId}}/estado` → 400. Existen errores de validación
- Estado por vendedor: `PATCH {{baseUrl}}/usuarios/{{usuarioVendedorId}}/estado` → 403. No tiene permisos para realizar esta operación
- Roles por vendedor: `PUT {{baseUrl}}/usuarios/{{usuarioVendedorId}}/roles` → 403. No tiene permisos para realizar esta operación

Resultado esperado: 409 auto desactivación, 404 usuario ausente, 400 estado requerido y 403 para las dos operaciones de vendedor.

Captura: B-08_01.png a B-08_06.png. Incluye URL, token usado, Body, código y mensaje. Al terminar comprueba que admin y vendedor siguen activos.

**Insertar aqui las capturas de B-08.**

Resultado obtenido: pendiente. Estado: pendiente.

## Entrega

El informe breve ocupa 3 paginas; las fichas y capturas adicionales van como anexos. Reemplaza resultados pendientes solo despues de medirlos. Exporta el Word con tus capturas como Huaccha_LP2_S10_Autonoma.pdf. Actualiza docs/informe-s10.pdf con ese informe final y guarda las evidencias localmente sin tokens de produccion.

Los tres commits de esta implementacion tienen fecha real del 18/10/2026. El requisito de dias distintos sigue pendiente; no se alteran fechas para simularlo. La solicitud se dirige a main, rama principal de este repositorio.
