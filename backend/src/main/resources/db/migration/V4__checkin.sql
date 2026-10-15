-- equipo_id, item_id y configuracion_ldp_id sin FK hasta V7 (la agrega A2-L5)
CREATE TABLE agregado_item (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    equipo_id             BIGINT        NOT NULL,
    item_id               BIGINT        NOT NULL,
    configuracion_ldp_id  BIGINT        NOT NULL,
    fecha                 DATE          NOT NULL,
    suma_reportada        NUMERIC(14,4) NOT NULL DEFAULT 0,
    total_respuestas      INT           NOT NULL DEFAULT 0,
    CONSTRAINT uk_agregado UNIQUE (equipo_id, item_id, configuracion_ldp_id, fecha),
    CONSTRAINT ck_agregado_total CHECK (total_respuestas >= 0)
);

-- Sin FK a usuario ni a equipo, nunca (R1, R6). cuestionario_id sin FK hasta V7.
-- Solo fechas, nunca TIMESTAMP (R5).
CREATE TABLE sesion_checkin (
    session_hash     CHAR(64) PRIMARY KEY,
    cuestionario_id  BIGINT   NOT NULL,
    ventana_inicio   DATE     NOT NULL
);

CREATE INDEX ix_sesion_ventana ON sesion_checkin (ventana_inicio);
