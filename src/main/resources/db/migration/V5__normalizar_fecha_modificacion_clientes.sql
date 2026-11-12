-- El esquema historico creado por Hibernate utilizaba una tilde en esta columna.
-- V1 ya usa el nombre sin tilde. Esta migracion admite ambos esquemas sin perder fechas.
DECLARE
    columna_actual NUMBER;
    columna_historica VARCHAR2(128);
BEGIN
    SELECT COUNT(*) INTO columna_actual
    FROM user_tab_columns
    WHERE table_name = 'CLIENTES' AND column_name = 'FECHA_MODIFICACION';

    SELECT MIN(column_name) INTO columna_historica
    FROM user_tab_columns
    WHERE table_name = 'CLIENTES'
      AND column_name = UNISTR('FECHA_MODIFICACI\00D3N');

    IF columna_actual = 0 AND columna_historica IS NOT NULL THEN
        EXECUTE IMMEDIATE 'ALTER TABLE clientes RENAME COLUMN "'
            || columna_historica || '" TO fecha_modificacion';
    ELSIF columna_actual = 1 AND columna_historica IS NOT NULL THEN
        -- Si existen ambas, completar solo fechas vacias y conservar la columna historica.
        EXECUTE IMMEDIATE 'UPDATE clientes SET fecha_modificacion = "'
            || columna_historica || '" WHERE fecha_modificacion IS NULL';
    END IF;
END;
/
