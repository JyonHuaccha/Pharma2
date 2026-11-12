-- Índices para validaciones y relaciones en instalaciones nuevas.
create unique index ux_categorias_nombre on categorias(upper(nombre));
create unique index ux_producto_nombre on producto(upper(nombre));
create unique index ux_clientes_dni on clientes(dni);
create index ix_producto_categoria on producto(categoria_id);
create index ix_detalle_venta on detalle_ventas(venta_id);
create index ix_ventas_cliente_fecha on ventas(cliente_id, fecha);
