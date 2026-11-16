# Guion de demostración de diez minutos

El estudiante conduce esta demostración y explica cada decisión con sus palabras. Preparar el entorno con checklist-demo.md y ejecutar preparar-demo.sql antes de cada verificación manual. Los tiempos siguientes son objetivos, no mediciones realizadas.

| Tiempo | Bloque | Acción concreta |
| --- | --- | --- |
| 0:00 a 1:00 | Contexto | Presentar problema de botica, roles ADMIN/VENDEDOR/ALMACENERO y arquitectura.png. Explicar navegador → API → Oracle y Redis. |
| 1:00 a 4:30 | Flujo principal | Login vendedor; buscar DNI 74140001; seleccionar AAA Demo Huaccha S14, 2 unidades a S/10; revisar y confirmar; anotar ID y requestId; mostrar comprobante; ir a Productos y comprobar 100→98 en la categoría S14 Demo Huaccha. |
| 4:30 a 6:00 | Seguridad | Vendedor sin Auditoría en menú; entrar /auditoria y mostrar 403 de la SPA. Mostrar PM-17 (403 API) y PM-16 (401 sin token). Para vencimiento, dejar antes una pestaña con sesión de duración corta en entorno de demo y mostrar aviso; un token alterado demuestra firma inválida, no vencimiento. |
| 6:00 a 7:30 | Volumen y rendimiento | Abrir Productos con volumen ≥5000, cambiar página y recargar; mostrar pagina en URL y request paginado en Network. Consultar Redis/Actuator con DEMO-01 a DEMO-03. Evitar mostrar todos los productos en un GET sin página. |
| 7:30 a 8:30 | Auditoría | Login admin; filtrar usuario vendedor, acción VENTA_REGISTRADA y fecha de hoy. Localizar la venta por ID; mostrar creado_por en SQL y requestId en el log. |
| 8:30 a 10:00 | Calidad | Ejecutar la suite de 14 casos con tres repeticiones; mientras corre, mostrar incidencias cerradas. Finalizar con el reporte HTML y el resumen. |

## Ensayos cronometrados

Pendientes de ejecución por el estudiante. Completar registro-ensayos.csv con dos ensayos reales, duración y desvíos; registrar también si superó diez minutos. No se marcan como realizados por disponer del guion.

## Verificación manual desde datos preparados

Pendiente. Hacer tres corridas separadas del flujo de 1:00 a 4:30: preparar los datos antes de cada una, cerrar sesiones y anotar fecha, duración, ID de venta, stock antes/después y cualquier desvío en registro-flujo-manual.csv. Una ejecución automática no cuenta como corrida manual.

## Video de respaldo

Pendiente de grabación por el estudiante. Grabar una demostración completa de máximo diez minutos con Recortes/Grabación de pantalla u OBS. Subirla a un enlace accesible para el docente y reemplazar el campo de video-enlace.txt. No compartir credenciales, secretos ni tokens visibles.

## Si algo falla

Oracle: mostrar health y revisar contenedor antes de continuar. Redis: mostrar health, no afirmar un hit si se ejecutó con la caché deshabilitada. API 401: autenticar otra vez y recuperar returnUrl. Puerto ocupado: identificar el proceso y no cerrar una ejecución ajena. Internet: usar repositorios locales, informe HTML y video ya descargado. No usar capturas como si fueran la aplicación en vivo.
