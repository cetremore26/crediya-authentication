-- Esquema inicial de usuarios (el mismo que creaba schema.sql antes de usar Flyway).
CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nombres VARCHAR(150) NOT NULL,
    apellidos VARCHAR(150) NOT NULL,
    fecha_nacimiento DATE,
    direccion VARCHAR(300),
    telefono VARCHAR(50),
    correo_electronico VARCHAR(320) NOT NULL,
    salario_base DECIMAL(17, 2) NOT NULL,
    documento_identidad VARCHAR(15),
    clave VARCHAR(100),
    rol VARCHAR(20),
    CONSTRAINT uk_usuarios_correo_electronico UNIQUE (correo_electronico)
);

CREATE UNIQUE INDEX uk_usuarios_documento_identidad ON usuarios (documento_identidad);
