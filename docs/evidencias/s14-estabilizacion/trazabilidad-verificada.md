# Caso técnico de trazabilidad verificado

Esta es la venta de la verificación automática de Postman en PHARMA_S14; no sustituye las tres corridas manuales ni la venta propia del video.

ID de venta: 22255. Autor: vendedor. Total: 20. Request ID: 8821d482.

## Filas consultadas en Oracle

```text
Session altered.


	ID	TOTAL CREADO_POR
---------- ---------- --------------------------------------------------
     22255	   20 vendedor


USUARIO 					   ACCION				    ENTIDAD_ID REQUEST_ID
-------------------------------------------------- ---------------------------------------- ---------- ------------------------------------
vendedor					   VENTA_REGISTRADA				 22255 8821d482
```

## Mismas líneas de log

```text
2026-11-15 23:00:59.195 INFO  [8821d482] p.e.p.P.s.impl.VentaServiceImpl - Inicio registrar venta | lineas=1
2026-11-15 23:00:59.214 INFO  [8821d482] p.e.p.P.s.impl.VentaServiceImpl - Venta preparada | ventaId=22255 | lineas=1 | total=20
2026-11-15 23:00:59.233 INFO  [8821d482] p.e.p.P.config.RequestIdFilter - POST /api/v1/ventas -> 201 (56 ms)
```

## Verificación del patrón de archivo en dev

JAR con configuración 4305a17. El identificador de cabecera se encuentra también en el archivo:

```text
2026-11-15 23:16:34.473 INFO  [4655f458] p.e.p.P.config.RequestIdFilter - GET /api/health -> 200 (33 ms)
```
