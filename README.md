# PharmaBackend

## Despliegue en producción

```text
Navegador ── HTTPS 443 ──► Nginx ── HTTP privado 8080 ──► PharmaBackend
                              │                              ├── Oracle TCP 1521
                              └── Angular y archivos con hash └── Redis TCP 6379
```

Nginx escucha en el puerto 80 dentro del contenedor. En la práctica se publica en `127.0.0.1:8081` y Oracle usa el puerto publicado `1522`. En producción, termina TLS en Nginx o en el proxy de entrada y restringe Oracle, Redis y el backend a la red privada. Los tres locales acceden a la misma URL HTTPS.

| Variable | Propósito |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión a Oracle con una cuenta de aplicación de permisos limitados. |
| `JWT_SECRET` | Clave de firma codificada en Base64, de al menos 32 bytes, administrada como secreto. |
| `JWT_EXPIRACION_MINUTOS` | Vigencia de las sesiones emitidas. |
| `FRONTEND_ORIGINS` | Lista de orígenes autorizados para solicitudes de origen cruzado. |
| `REDIS_HOST`, `REDIS_PORT` | Dirección privada del Redis compartido por las instancias del backend. |
| `SPRING_PROFILES_ACTIVE`, `SERVER_PORT` | Seleccionan el perfil `prod` y el puerto HTTP del servicio. |
| `LOGGING_FILE_NAME` | Ubicación del log rotado cuando el servicio escribe en un volumen dedicado. |

1. Selecciona un commit aprobado. Ejecuta `./mvnw clean package` con Oracle y Redis de pruebas disponibles. Guarda el JAR, su SHA-256 y el commit como una versión inmutable. En Windows usa `mvnw.cmd`.
2. Verifica las migraciones en un entorno previo y toma una copia recuperable de Oracle. Configura las variables desde el gestor de secretos e inicia el JAR con el perfil `prod`. En Windows, `deploy/iniciar-prod.ps1` valida las variables requeridas.
3. Comprueba `/actuator/health`, el login y una lectura de catálogo detrás de Nginx. Verifica que un error devuelva el mismo identificador en el cuerpo, la cabecera y el log. Mantén las métricas restringidas al permiso administrativo.
4. Para volver atrás, retira la instancia fallida del proxy, arranca el JAR anterior con su configuración compatible y verifica health y catálogo. Revisa la compatibilidad del esquema antes de retroceder el binario. Si la migración es incompatible, aplica una reversión ensayada o restaura Oracle desde la copia. Invalida las entradas de catálogo de la aplicación si cambió su formato o hubo escrituras durante una caída de Redis.

| Buena práctica de la sesión | Estado | Acción pendiente |
| --- | --- | --- |
| Rutas y reporte con carga diferida | Cumple | Medir el presupuesto inicial en cada versión. |
| HTML revalidado y JS/CSS con hash e inmutables | Cumple | Conservar los chunks previos durante la ventana de reversión. |
| API autenticada sin almacenamiento en el navegador | Cumple | Verificar cabeceras desde el proxy de producción. |
| Redis con TTL, invalidación y consulta a Oracle en caída | Cumple parcialmente | Automatizar la purga de catálogo tras una interrupción con escrituras y alertar por fallos de caché. |
| Trazabilidad, rotación y auditoría de secretos | Cumple | Centralizar logs con acceso restringido y retención definida. |
| Health público mínimo y métricas protegidas | Cumple | Configurar sondeos y alertas operativas. |
| Transporte cifrado y restauración ensayada | Cumple parcialmente | Habilitar TLS en el entorno final y probar la recuperación de Oracle. |

1. **Prioridad alta.** Publica una URL HTTPS común a los tres locales. Limita la exposición a Nginx y almacena las credenciales fuera del repositorio.
2. **Prioridad alta.** Programa copias de Oracle y ensayos de restauración. Define con operaciones el tiempo de recuperación y la pérdida de datos admisible.
3. **Prioridad media.** Mantén un Redis privado compartido por las instancias del backend y alertas de health, errores y latencia. Tras una caída con escrituras, verifica los catálogos antes de dar por recuperado el servicio.
