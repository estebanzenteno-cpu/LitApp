-- Ejecutar en el esquema de LitApp. MySQL 9.4.
-- Se mantienen la clave foránea y el índice único de usuario.idCliente.
-- Crear las nuevas reglas primero evita dejar la tabla sin protección.

DELIMITER $$

CREATE TRIGGER IF NOT EXISTS trg_usuario_vinculo_cliente_ins
BEFORE INSERT ON usuario FOR EACH ROW
BEGIN
    IF NEW.idCliente IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM rol WHERE idRol = NEW.idRol AND nombre = 'cliente'
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Solo las cuentas con rol cliente pueden vincularse a un cliente';
    END IF;
END$$

CREATE TRIGGER IF NOT EXISTS trg_usuario_vinculo_cliente_upd
BEFORE UPDATE ON usuario FOR EACH ROW
BEGIN
    IF NEW.idCliente IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM rol WHERE idRol = NEW.idRol AND nombre = 'cliente'
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Solo las cuentas con rol cliente pueden vincularse a un cliente';
    END IF;
END$$

DELIMITER ;

DROP TRIGGER IF EXISTS trg_usuario_rol_cliente_ins;
DROP TRIGGER IF EXISTS trg_usuario_rol_cliente_upd;
