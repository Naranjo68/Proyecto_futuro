CREATE TABLE usuario (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email          VARCHAR(150) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    nombre         VARCHAR(150) NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- equipo_id sin FK hasta V7 (la agrega A2-L5)
CREATE TABLE empleado_equipo (
    usuario_id  BIGINT PRIMARY KEY,
    equipo_id   BIGINT NOT NULL,
    CONSTRAINT fk_empleado_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- empresa_id sin FK hasta V7 (la agrega A2-L5)
CREATE TABLE personal_empresa (
    usuario_id  BIGINT      PRIMARY KEY,
    empresa_id  BIGINT      NOT NULL,
    rol         VARCHAR(25) NOT NULL,
    CONSTRAINT fk_personal_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT ck_personal_rol CHECK (rol IN ('RRHH', 'COORDINADOR_BIENESTAR'))
);

CREATE TABLE administrador_plataforma (
    usuario_id  BIGINT PRIMARY KEY,
    CONSTRAINT fk_admin_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);
