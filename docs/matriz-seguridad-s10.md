# Matriz de seguridad de la sesion 10

Rama `feature/seguridad-pruebas-huaccha`. 28 casos: 16 base, 4 propios de grupos distintos (A-05, T-06, P-05, U-03) y B-01 a B-08. Los resultados manuales se registran al ejecutar en Postman y el navegador; no confundirlos con las pruebas automatizadas aisladas.

| ID | Grupo | Usuario o token | Peticion | Resultado esperado codigo y mensaje | Resultado obtenido | Estado | Evidencia |
| --- | --- | --- | --- | --- | --- | --- | --- |
| A-01 | Autenticación | No Auth | POST /auth/login<br>POST /auth/login<br>POST /auth/login<br>POST /auth/login | Cuatro respuestas 200 con token, roles y permisos. Supervisor contiene ambos roles y la unión de permisos. | Pendiente de ejecucion | Pendiente | A-01_*.png |
| A-02 | Autenticación | No Auth | POST /auth/login<br>POST /auth/login | Ambos 401 y message Usuario o contraseña incorrectos. | Pendiente de ejecucion | Pendiente | A-02_*.png |
| A-03 | Autenticación | No Auth | POST /auth/login | 401 y Usuario o contraseña incorrectos. | Pendiente de ejecucion | Pendiente | A-03_*.png |
| A-04 | Autenticación | No Auth | POST /auth/login | 200, username vendedor y rol VENDEDOR. | Pendiente de ejecucion | Pendiente | A-04_*.png |
| T-01 | Token | No Auth | GET /productos | 401 y Debe iniciar sesión para acceder a este recurso. | Pendiente de ejecucion | Pendiente | T-01_*.png |
| T-02 | Token | tokenPayloadAlterado | GET /productos | 401 y El token no es válido. | Pendiente de ejecucion | Pendiente | T-02_*.png |
| T-03 | Token | No Auth, tokenAnterior | POST /auth/login<br>GET /productos | Login inicial 200 y posterior GET con tokenAnterior 401 El token no es válido. | Pendiente de ejecucion | Pendiente | T-03_*.png |
| T-04 | Token | No Auth, tokenCorto | POST /auth/login<br>GET /productos | Emisión 200 con 60 segundos; después 401 El token ha expirado. Inicie sesión nuevamente. | Pendiente de ejecucion | Pendiente | T-04_*.png |
| T-05 | Token | No Auth | POST /auth/login | 200, claims correctos y duración de 3600 segundos sin datos sensibles. | Pendiente de ejecucion | Pendiente | T-05_*.png |
| P-01 | Permisos | tokenVendedor | POST /categorias<br>PUT /categorias/{{categoriaId}}<br>DELETE /categorias/{{categoriaId}} | Tres respuestas 403 No tiene permisos para realizar esta operación. | Pendiente de ejecucion | Pendiente | P-01_*.png |
| P-02 | Permisos | tokenAlmacen | GET /clientes<br>POST /ventas | Ambos 403 No tiene permisos para realizar esta operación. | Pendiente de ejecucion | Pendiente | P-02_*.png |
| P-03 | Permisos | tokenAlmacen, tokenVendedor | GET /reportes/productos-mas-vendidos<br>GET /reportes/productos-mas-vendidos | Ambos 403 No tiene permisos para realizar esta operación. | Pendiente de ejecucion | Pendiente | P-03_*.png |
| P-04 | Permisos | tokenAdmin | GET /categorias<br>GET /productos<br>GET /clientes<br>GET /ventas<br>GET /usuarios<br>GET /empleados<br>GET /roles<br>GET /reportes/productos-mas-vendidos | 200 en categorías, productos, clientes, ventas, usuarios, empleados, roles y reportes. | Pendiente de ejecucion | Pendiente | P-04_*.png |
| U-01 | Usuarios | tokenAdmin | POST /usuarios<br>POST /usuarios | Ambos 400 Existen errores de validación; error de password en el primero y rolIds en el segundo. | Pendiente de ejecucion | Pendiente | U-01_*.png |
| U-02 | Usuarios | tokenAdmin | POST /usuarios<br>POST /usuarios | 409 por empleado ya vinculado; luego 201 sin contraseña, hash BCrypt de 60 caracteres y relación en usuario_roles. | Pendiente de ejecucion | Pendiente | U-02_*.png |
| C-01 | CORS | No Auth | OPTIONS /productos | OPTIONS 200 con origen y Authorization permitidos; GET real 200 o 401 sin error CORS. | Pendiente de ejecucion | Pendiente | C-01_*.png |
| A-05 | Autenticación propio | No Auth | POST /auth/login | 400 Existen errores de validación y validationErrors.username. | Pendiente de ejecucion | Pendiente | A-05_*.png |
| T-06 | Token propio | No Auth | GET /productos | 401 El token no es válido. | Pendiente de ejecucion | Pendiente | T-06_*.png |
| P-05 | Permisos propio | tokenVendedor | GET /usuarios | 403 No tiene permisos para realizar esta operación. | Pendiente de ejecucion | Pendiente | P-05_*.png |
| U-03 | Usuarios propio | tokenAdmin | POST /usuarios | 400 y validationErrors.username indicando caracteres permitidos. | Pendiente de ejecucion | Pendiente | U-03_*.png |
| B-01 | Contraseñas | No Auth, tokenNuevaClave, tokenVendedor | PUT /auth/password<br>POST /auth/login<br>POST /auth/login<br>GET /auth/me<br>PUT /auth/password | 204 en cambio; login anterior 401 y nuevo 200; token anterior aún 200; restauración 204. | Pendiente de ejecucion | Pendiente | B-01_*.png |
| B-02 | Contraseñas | tokenVendedor | PUT /auth/password | 409 La contraseña actual no es correcta. | Pendiente de ejecucion | Pendiente | B-02_*.png |
| B-03 | Contraseñas | tokenVendedor | PUT /auth/password | 409 La nueva contraseña debe ser distinta de la actual. | Pendiente de ejecucion | Pendiente | B-03_*.png |
| B-04 | Contraseñas | No Auth, tokenVendedor | PUT /auth/password<br>PUT /auth/password<br>PUT /auth/password<br>PUT /auth/password<br>PUT /auth/password | 400 en los cuatro cuerpos inválidos con validationErrors; sin token 401 Debe iniciar sesión para acceder a este recurso. | Pendiente de ejecucion | Pendiente | B-04_*.png |
| B-05 | Roles | tokenAdmin, tokenVendedor | PUT /usuarios/{{usuarioVendedorId}}/roles<br>GET /productos/{{productoId}}<br>PUT /productos/{{productoId}}<br>PUT /usuarios/{{usuarioVendedorId}}/roles<br>PUT /productos/{{productoId}} | Roles 200 con ambos roles y permisos; PUT producto 200 con el mismo JWT; tras restaurar, el mismo PUT responde 403. | Pendiente de ejecucion | Pendiente | B-05_*.png |
| B-06 | Roles | tokenAdmin | PUT /usuarios/{{usuarioVendedorId}}/roles<br>PUT /usuarios/{{usuarioVendedorId}}/roles<br>PUT /usuarios/{{usuarioInexistenteId}}/roles<br>PUT /usuarios/{{usuarioVendedorId}}/roles<br>PUT /usuarios/{{usuarioAdminId}}/roles | 400 vacío/nulo, 404 usuario/rol ausente y 409 No puede quitarse a sí mismo el rol ADMIN. | Pendiente de ejecucion | Pendiente | B-06_*.png |
| B-07 | Estado | No Auth, tokenAdmin, tokenVendedor | PATCH /usuarios/{{usuarioVendedorId}}/estado<br>GET /productos<br>POST /auth/login<br>PATCH /usuarios/{{usuarioVendedorId}}/estado<br>GET /productos | PATCH 200; token previo 401 usuario deshabilitado; login 401 genérico; reactivación 200 y GET 200. | Pendiente de ejecucion | Pendiente | B-07_*.png |
| B-08 | Estado | tokenAdmin, tokenVendedor | PATCH /usuarios/{{usuarioAdminId}}/estado<br>PATCH /usuarios/{{usuarioInexistenteId}}/estado<br>PATCH /usuarios/{{usuarioVendedorId}}/estado<br>PATCH /usuarios/{{usuarioVendedorId}}/estado<br>PATCH /usuarios/{{usuarioVendedorId}}/estado<br>PUT /usuarios/{{usuarioVendedorId}}/roles | 409 auto desactivación, 404 usuario ausente, 400 estado requerido y 403 para las dos operaciones de vendedor. | Pendiente de ejecucion | Pendiente | B-08_*.png |

## Regla para el estado

Pasa solo si se cumplen todas las subpeticiones, verificaciones SQL o de navegador y restauraciones indicadas para ese ID. Si una subpeticion falla, el caso es Falla y requiere ficha de hallazgo. Un caso sin captura o sin entorno preparado queda Pendiente.

## Resumen manual

| Grupo | Planificados | Ejecutados | Pasa | Falla | Pendientes |
| --- | --- | --- | --- | --- | --- |
| Autenticación | 5 | 0 | 0 | 0 | 5 |
| Token | 6 | 0 | 0 | 0 | 6 |
| Permisos | 5 | 0 | 0 | 0 | 5 |
| Usuarios | 3 | 0 | 0 | 0 | 3 |
| CORS | 1 | 0 | 0 | 0 | 1 |
| Contraseñas | 4 | 0 | 0 | 0 | 4 |
| Roles | 2 | 0 | 0 | 0 | 2 |
| Estado | 2 | 0 | 0 | 0 | 2 |
| Total | 28 | 0 | 0 | 0 | 28 |

Actualizar esta tabla y C1 del informe solo con los resultados obtenidos. Las variables resultadosS10 de la coleccion guardan codigo, mensaje y estado de las peticiones enviadas; no incluyen tokens ni cuerpos. CORS y SQL se verifican manualmente.
