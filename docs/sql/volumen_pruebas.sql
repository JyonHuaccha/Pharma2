-- Solo ejecutar en el esquema de desarrollo o PHARMA_S14, tras V4.
-- Las ventas sintéticas carecen de detalle y se identifican con creado_por=carga.
insert into producto (nombre, precio, stock, estado, categoria_id, fecha_creacion, creado_por)
select 'Producto de prueba ' || lpad(level, 5, '0'),
       round(dbms_random.value(1, 200), 2), trunc(dbms_random.value(0, 500)), true,
       (select min(id) from categorias where estado=true), systimestamp, 'carga'
from dual connect by level <= 5000;

insert into ventas (fecha, total, estado, cliente_id, creado_por)
select systimestamp - numtodsinterval(dbms_random.value(0, 365), 'DAY'),
       round(dbms_random.value(5, 500), 2), 'REGISTRADA',
       (select min(id) from clientes where estado=true), 'carga'
from dual connect by level <= 20000;
commit;
