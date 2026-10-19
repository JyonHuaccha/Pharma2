# PharmaBackend

API REST de PharmaSoft con Spring Boot 4, Java 21 y Oracle. La sesión 10 incorpora autenticación JWT, contraseñas con BCrypt y autorización por roles y permisos.

## Seguridad

- Públicos: `POST /api/v1/auth/login`, `GET /api/health` y la documentación Swagger habilitada en desarrollo.
- En las peticiones protegidas: `Authorization: Bearer <token>`.
- `GET /api/v1/auth/me` devuelve los datos del usuario autenticado sin contraseña ni hash.
- ADMIN dispone de todos los permisos; VENDEDOR consulta catálogo, administra clientes y registra/consulta ventas; ALMACENERO consulta y modifica catálogo. Varios roles suman sus permisos.
- Los reportes requieren `REPORTES_VER` mediante `@PreAuthorize`. La administración de usuarios, empleados y roles requiere `USUARIOS_ADMINISTRAR`.
- La API es sin estado y responde 401 por falta de autenticación o token inválido/expirado, y 403 por falta de permiso, con el formato de error de la API.

## Configuración y base de datos

El perfil local es `dev`. Requiere una base Oracle y la configuración de conexión de `application-dev.yaml`. La clave JWT incluida en desarrollo y las cuentas del script SQL son exclusivamente para clase.

En producción se utiliza `application-prod.yaml`: configurar las variables de entorno de conexión y `JWT_SECRET` (clave Base64 de al menos 32 bytes). `JWT_EXPIRACION_MINUTOS` y `FRONTEND_ORIGINS` permiten configurar duración y CORS. No guardar secretos de producción ni tokens de sesión en el repositorio.

El script de seguridad está en `src/main/resources/db/migration/V2__seguridad.sql`. Crea empleados, usuarios, roles, asociaciones y cuentas de desarrollo con hashes BCrypt. **Este proyecto no incluye actualmente Flyway: el script no se ejecuta automáticamente.** En una base donde ya se aplicó, no volver a ejecutar sus instrucciones de creación e inserción. Esta entrega no modifica ni reinicializa la base de datos.

La SPA requiere incorporar login e interceptor para enviar el token; los accesos sin autenticación reciben 401, tal como contempla la guía de la sesión 10.

## Pruebas unitarias

Desde la raíz del backend, con un JDK compatible instalado:

```powershell
.\mvnw.cmd --batch-mode "-Dtest=JwtServiceTest,PermisosPorRolTest,BajaLogicaTest,ReporteContratoTest" test
```

Verificación del 16/10/2026: **20 pruebas aprobadas**, sin errores ni fallos. Incluye las 5 de `JwtServiceTest` y las 4 de `PermisosPorRolTest`. Son pruebas unitarias que no requieren iniciar la API ni escribir en Oracle. La prueba de contexto `PharmaBackendApplicationTests` requiere el entorno de base de datos y no forma parte de esta ejecución.

Las 24 pruebas manuales del paso 14, sus capturas y el documento PDF están a cargo del estudiante y no se incluyen en esta entrega ni se dan por verificadas aquí.

## Entrega de la sesión 10

Rama: `feature/seguridad-jwt`. Por decisión del estudiante, se consolida el trabajo en **un único commit**. La solicitud de incorporación se dirige a `main`, rama principal real de este repositorio. La guía propone cuatro commits y la rama `master`; esta entrega conserva la decisión de un solo commit y el nombre de la rama principal existente.

## Actividad autónoma de la sesión 10

Rama `feature/seguridad-pruebas-huaccha`, creada desde la práctica. Se incorporan:

| Endpoint | Acceso | Comportamiento |
| --- | --- | --- |
| `PUT /api/v1/auth/password` | Cualquier usuario autenticado | Obtiene el usuario del token, comprueba la contraseña actual con BCrypt y guarda el nuevo hash. Responde 204, 400 por validación y 409 por contraseña actual incorrecta o reutilización. |
| `PUT /api/v1/usuarios/{id}/roles` | ADMIN mediante `USUARIOS_ADMINISTRAR` | Reemplaza los roles; devuelve roles y permisos actualizados. Responde 400 por conjunto vacío o IDs nulos, 404 por usuario o rol inexistente y 409 si el administrador intenta quitarse ADMIN. |
| `PATCH /api/v1/usuarios/{id}/estado` | ADMIN mediante `USUARIOS_ADMINISTRAR` | Cambia el estado conservando la cuenta y sus relaciones. Responde 400 por estado ausente, 404 por usuario inexistente y 409 por desactivación de la propia cuenta. |

`JwtAuthenticationFilter` consulta `UsuarioDetailsService` en cada petición: usa los permisos y el estado actuales de la base, aunque el JWT se haya emitido antes. Un usuario desactivado recibe 401 con su token previo. El cambio de contraseña no revoca tokens emitidos; siguen válidos hasta su expiración si la cuenta continúa activa.

Verificación automatizada del 18/10/2026: **45 pruebas aprobadas**, sin usar Oracle ni iniciar una API real. Se incluyen 25 pruebas nuevas de cambio de contraseña, gestión de cuentas y cadena de seguridad con MockMvc. La prueba de contexto con base de datos no se ejecuta en esta verificación.

```powershell
.\mvnw.cmd --batch-mode "-Dtest=JwtServiceTest,PermisosPorRolTest,SeguridadEndpointsTest,CambioPasswordTest,GestionUsuarioTest,BajaLogicaTest,ReporteContratoTest" test
```

Materiales:

- `postman/PharmaSoft-S10-Seguridad.json`: colección con 28 casos, 72 peticiones y preparación adicional. Variables de tokens exportadas vacías. Enviar peticiones individualmente; hay reinicios, esperas y cambios temporales que deben restaurarse.
- `docs/matriz-seguridad-s10.md`: 16 casos base, cuatro propios de grupos distintos y B-01 a B-08. Los resultados manuales quedan pendientes hasta medirlos y adjuntar capturas.
- `docs/guia-capturas-s10.md`: precondiciones, secuencia, capturas necesarias y restauración de datos/configuración.
- `docs/informe-s10.docx` y `docs/informe-s10.pdf`: informe breve de tres páginas con análisis y espacios para registrar resultados reales y hallazgos.
- `docs/Huaccha_LP2_S10_Autonoma.docx`: informe con anexo de instrucciones y espacios para las capturas. Completar la matriz y el resumen con lo obtenido antes de exportar y entregar el PDF final.

Los tres commits de la actividad tienen sus fechas reales. El criterio de realizarlos en días distintos sigue pendiente; no se simulan fechas ni pruebas manuales. La solicitud de incorporación se dirige a `main`, rama principal existente. No se incluye una ejecución automática de Flyway ni se reinicializa la base actual.
