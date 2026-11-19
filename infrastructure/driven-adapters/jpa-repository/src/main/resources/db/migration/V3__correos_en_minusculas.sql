-- Los correos se comparan tal cual (sin lower()) para que las búsquedas usen el índice único.
-- La aplicación ya los guarda normalizados; esto normaliza los que se hubieran guardado antes.
UPDATE usuarios
SET correo_electronico = LOWER(TRIM(correo_electronico))
WHERE correo_electronico <> LOWER(TRIM(correo_electronico));
