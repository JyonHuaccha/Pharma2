-- Conectarse al mismo esquema que utiliza el backend.
-- En SQL Developer ejecutar como script con F5 e ingresar el ID real del comprobante.
set linesize 220 pagesize 100
define venta_id = &ID_VENTA
select id, fecha, cliente_id, total, estado, creado_por
from ventas where id = &venta_id;
select id, fecha, usuario, accion, entidad, entidad_id, detalle, request_id
from auditoria_eventos
where entidad = 'Venta' and entidad_id = &venta_id and accion = 'VENTA_REGISTRADA'
order by fecha desc;
undefine venta_id
