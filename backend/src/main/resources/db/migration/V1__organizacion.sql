CREATE TABLE empresa (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ruc        CHAR(11)     NOT NULL UNIQUE,
    nombre     VARCHAR(150) NOT NULL,
    rubro      VARCHAR(100) NOT NULL,
    creado_en  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE equipo (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(150) NOT NULL,
    creado_en   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipo_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT uk_equipo_empresa_nombre UNIQUE (empresa_id, nombre)
);
