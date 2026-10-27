CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY,
    nombres VARCHAR(150) NOT NULL,
    apellidos VARCHAR(150) NOT NULL,
    fecha_nacimiento DATE,
    direccion VARCHAR(300),
    telefono VARCHAR(50),
    correo_electronico VARCHAR(320) NOT NULL,
    salario_base DECIMAL(17, 2) NOT NULL,
    CONSTRAINT uk_usuarios_correo_electronico UNIQUE (correo_electronico)
);

ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS documento_identidad VARCHAR(15);
CREATE UNIQUE INDEX IF NOT EXISTS uk_usuarios_documento_identidad
    ON usuarios (documento_identidad);
