-- Todo usuario tiene rol: los registros antiguos sin rol eran clientes (rol por defecto).
UPDATE usuarios SET rol = 'CLIENTE' WHERE rol IS NULL;
ALTER TABLE usuarios ALTER COLUMN rol SET NOT NULL;
