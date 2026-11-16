-- Ejecutar con F5 sobre un esquema de DESARROLLO antes de cada corrida manual.
-- Solo restablece el producto creado por este script; no borra ventas ni clientes existentes.
whenever sqlerror exit failure rollback
declare
    categoria_demo number;
    coincidencias number;
begin
    select count(*) into coincidencias from categorias
      where nombre='S14 Demo Huaccha' and nvl(creado_por,'otro') <> 'demo-s14a';
    if coincidencias > 0 then raise_application_error(-20001,'Categoría homónima ajena; use otro nombre de demo'); end if;
    select count(*) into coincidencias from clientes
      where (dni='74140001' or email='huaccha-s14a@example.test') and nvl(creado_por,'otro') <> 'demo-s14a';
    if coincidencias > 0 then raise_application_error(-20002,'DNI o correo ocupado; cambie los datos del script'); end if;
    select count(*) into coincidencias from producto
      where nombre='AAA Demo Huaccha S14' and nvl(creado_por,'otro') <> 'demo-s14a';
    if coincidencias > 0 then raise_application_error(-20003,'Producto homónimo ajeno; use otro nombre de demo'); end if;
    merge into categorias c using (select 'S14 Demo Huaccha' nombre from dual) d
    on (c.nombre=d.nombre)
    when not matched then insert(nombre, descripcion, estado, fecha_creacion, creado_por)
      values(d.nombre,'Demostración autónoma S14',true,systimestamp,'demo-s14a');
    select id into categoria_demo from categorias where nombre='S14 Demo Huaccha';
    merge into clientes c using (select '74140001' dni from dual) d on (c.dni=d.dni)
    when not matched then insert(dni,nombres,apellidos,email,estado,fecha_creacion,creado_por)
      values(d.dni,'Cliente Demo','Huaccha','huaccha-s14a@example.test',true,systimestamp,'demo-s14a');
    merge into producto p using (select 'AAA Demo Huaccha S14' nombre from dual) d on (p.nombre=d.nombre)
    when matched then update set p.stock=100,p.precio=10,p.estado=true,p.fecha_modificacion=systimestamp,p.modificado_por='demo-s14a'
    when not matched then insert(nombre,precio,stock,estado,categoria_id,fecha_creacion,creado_por)
      values(d.nombre,10,100,true,categoria_demo,systimestamp,'demo-s14a');
    commit;
end;
/
select id,nombre,precio,stock,estado from producto where nombre='AAA Demo Huaccha S14';
select id,dni,nombres,apellidos from clientes where dni='74140001';
-- Tras el restablecimiento SQL, Redis podría conservar la página anterior.
-- Reiniciar la app no limpia Redis: borrar SOLO claves del prefijo del esquema de demo,
-- o usar una base Redis vacía reservada a la demo. Nunca ejecutar FLUSHALL.
