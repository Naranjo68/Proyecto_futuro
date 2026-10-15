-- usuario_id y equipo_id sin FK: las agrega LeoSle4 al final (V7)
CREATE TABLE consulta_dashboard (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id     BIGINT      NOT NULL,
    equipo_id      BIGINT      NOT NULL,
    fecha_desde    DATE        NOT NULL,
    fecha_hasta    DATE        NOT NULL,
    resultado      VARCHAR(25) NOT NULL,
    consultado_en  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_consulta_rango CHECK (fecha_hasta >= fecha_desde),
    CONSTRAINT ck_consulta_resultado CHECK (
        resultado IN ('OK', 'DATOS_INSUFICIENTES')
    )
);
