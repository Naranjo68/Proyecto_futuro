CREATE TABLE escala_respuesta (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo     VARCHAR(30) NOT NULL UNIQUE,
    valor_min  SMALLINT    NOT NULL,
    valor_max  SMALLINT    NOT NULL,
    CONSTRAINT ck_escala_rango CHECK (valor_max > valor_min)
);

CREATE TABLE opcion_escala (
    escala_id  BIGINT       NOT NULL,
    valor      SMALLINT     NOT NULL,
    etiqueta   VARCHAR(100) NOT NULL,
    PRIMARY KEY (escala_id, valor),
    CONSTRAINT fk_opcion_escala FOREIGN KEY (escala_id) REFERENCES escala_respuesta(id)
);

CREATE TABLE configuracion_ldp (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mecanismo            VARCHAR(25)  NOT NULL,
    probabilidad_verdad  DECIMAL(4,3) NULL,
    epsilon              DECIMAL(6,3) NULL,
    creado_en            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_ldp_mecanismo CHECK (
        (mecanismo = 'RANDOMIZED_RESPONSE' AND probabilidad_verdad > 0.5 AND probabilidad_verdad < 1.0 AND epsilon IS NULL) OR
        (mecanismo = 'LAPLACE' AND epsilon > 0 AND probabilidad_verdad IS NULL)
    )
);

CREATE TABLE cuestionario (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo           VARCHAR(30)  NOT NULL UNIQUE,
    nombre           VARCHAR(100) NOT NULL,
    cadencia_dias    INT          NOT NULL,
    items_por_envio  INT          NULL
);

CREATE TABLE item_cuestionario (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cuestionario_id      BIGINT       NOT NULL,
    orden                INT          NOT NULL,
    tema                 VARCHAR(50)  NOT NULL,
    texto                VARCHAR(255) NOT NULL,
    escala_id            BIGINT       NOT NULL,
    configuracion_ldp_id BIGINT       NOT NULL,
    CONSTRAINT fk_item_cuestionario FOREIGN KEY (cuestionario_id) REFERENCES cuestionario(id),
    CONSTRAINT fk_item_escala FOREIGN KEY (escala_id) REFERENCES escala_respuesta(id),
    CONSTRAINT fk_item_ldp FOREIGN KEY (configuracion_ldp_id) REFERENCES configuracion_ldp(id),
    CONSTRAINT uk_cuestionario_orden UNIQUE (cuestionario_id, orden)
);

CREATE TABLE empresa_cuestionario (
    empresa_id      BIGINT    NOT NULL,
    cuestionario_id BIGINT    NOT NULL,
    activo          BOOLEAN   NOT NULL DEFAULT TRUE,
    activado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (empresa_id, cuestionario_id),
    CONSTRAINT fk_empresa_cuestionario_cuestionario FOREIGN KEY (cuestionario_id) REFERENCES cuestionario(id)
);

CREATE TABLE presupuesto_privacidad (
    empresa_id      BIGINT       NOT NULL,
    periodo         VARCHAR(7)   NOT NULL,
    epsilon_maximo  DECIMAL(6,3) NOT NULL,
    PRIMARY KEY (empresa_id, periodo)
);
