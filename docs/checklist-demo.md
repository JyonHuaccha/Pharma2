# Lista previa de la demostración

Marcar únicamente después de comprobarlo en el equipo que se presentará.

- [ ] Oracle accesible en localhost:1522/FREEPDB1; confirmar qué esquema usa el perfil activo.
- [ ] Redis accesible y backend iniciado con perfil dev o prod conocido; /actuator/health responde UP.
- [ ] Migraciones Flyway V1 a V5 correctas y Hibernate validate, sin ddl-auto=update.
- [ ] Datos de demo preparados con docs/sql/preparar-demo.sql; cliente DNI 74140001, producto AAA Demo Huaccha S14, precio 10, stock 100.
- [ ] Caché de demo coherente con Oracle después del SQL. La opción más simple es usar otra base Redis/prefijo para la demo y eliminar solamente esas claves propias antes de iniciarla. No usar FLUSHALL.
- [ ] Al menos 5000 productos y 60 ventas si se mostrarán volumen y tercera página. Si faltan, cargar volumen_pruebas.sql solo en el esquema de pruebas y comprobar conteos.
- [ ] Admin, vendedor y almacen activos; contraseñas conocidas en el entorno privado de Postman. No incluirlas en el video ni en docs.
- [ ] Backend reiniciado después de incorporar los eventos nuevos; la ejecución anterior no los incorpora por guardar archivos.
- [ ] SPA iniciada con npm start (4200), o build servido con Nginx (8081) y API apuntando al backend correcto.
- [ ] Las pestañas viejas no conservan un build anterior; cerrar sesión y recargar si se usó Nginx antes de reconstruir.
- [ ] Si se prueba el build con scripts/servidor-e2e.mjs en 4210, permitir ese origen en APP_CORS_ALLOWED_ORIGINS.
- [ ] Postman: importar colección y entorno; seleccionar entorno; LOGIN de los tres roles; PRE una sola vez.
- [ ] SQL Developer conectado al mismo esquema, archivo trazabilidad-demo.sql preparado.
- [ ] Logs de archivo activos con requestId. En dev: añadir LOGGING_FILE_NAME=logs/pharma-backend.log al arranque o filtrar la consola de IntelliJ.
- [ ] Pestañas de SPA, Postman, SQL, terminal de log, Actuator e informe HTML preparadas.
- [ ] Sesiones de los roles cerradas antes de comenzar el video. No mostrar Authorization ni respuestas de login con token.
- [ ] Dos ensayos y tres verificaciones manuales anotados con sus tiempos reales.
- [ ] Video de respaldo de máximo diez minutos disponible, enlace probado en otra sesión.
- [ ] Etiqueta v1.0-demo verificada en ambos repositorios; comprobar el commit final antes de la presentación.
- [ ] PDF final revisado con las capturas propias y enlaces; carga al aula virtual a cargo del estudiante.
