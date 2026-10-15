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
        (mecanismo = 'RANDOMIZED_RESPONSE'
            AND probabilidad_verdad > 0.5 AND probabilidad_verdad < 1
            AND epsilon IS NULL)
     OR (mecanismo = 'LAPLACE'
            AND epsilon > 0
            AND probabilidad_verdad IS NULL)
    )
);

CREATE TABLE cuestionario (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo           VARCHAR(20)  NOT NULL UNIQUE,
    nombre           VARCHAR(100) NOT NULL,
    cadencia_dias    SMALLINT     NOT NULL,
    items_por_envio  SMALLINT     NULL,
    CONSTRAINT ck_cuestionario_cadencia CHECK (cadencia_dias > 0),
    CONSTRAINT ck_cuestionario_k CHECK (items_por_envio IS NULL OR items_por_envio > 0)
);

CREATE TABLE item_cuestionario (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cuestionario_id       BIGINT       NOT NULL,
    orden                 SMALLINT     NOT NULL,
    tema                  VARCHAR(100) NOT NULL,
    texto                 TEXT         NOT NULL,
    escala_id             BIGINT       NOT NULL,
    configuracion_ldp_id  BIGINT       NOT NULL,
    CONSTRAINT fk_item_cuestionario FOREIGN KEY (cuestionario_id)      REFERENCES cuestionario(id),
    CONSTRAINT fk_item_escala       FOREIGN KEY (escala_id)            REFERENCES escala_respuesta(id),
    CONSTRAINT fk_item_config       FOREIGN KEY (configuracion_ldp_id) REFERENCES configuracion_ldp(id),
    CONSTRAINT uk_item_orden UNIQUE (cuestionario_id, orden)
);

CREATE TABLE empresa_cuestionario (
    empresa_id       BIGINT  NOT NULL,
    cuestionario_id  BIGINT  NOT NULL,
    activo           BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (empresa_id, cuestionario_id),
    CONSTRAINT fk_ec_cuestionario FOREIGN KEY (cuestionario_id) REFERENCES cuestionario(id)
);

CREATE TABLE presupuesto_privacidad (
    empresa_id              BIGINT       NOT NULL,
    periodo                 CHAR(7)      NOT NULL,
    epsilon_maximo_persona  DECIMAL(6,3) NOT NULL,
    PRIMARY KEY (empresa_id, periodo),
    CONSTRAINT ck_presupuesto_eps CHECK (epsilon_maximo_persona > 0)
);
