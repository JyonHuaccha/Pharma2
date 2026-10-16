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
