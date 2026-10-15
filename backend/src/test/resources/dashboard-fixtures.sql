
CREATE TABLE IF NOT EXISTS equipo (
    id BIGINT PRIMARY KEY,
    empresa_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS agregado_item (
    equipo_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    configuracion_ldp_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    suma_reportada NUMERIC(14,4) NOT NULL,
    total_respuestas INT NOT NULL
);

CREATE TABLE IF NOT EXISTS configuracion_ldp (
    id BIGINT PRIMARY KEY,
    mecanismo VARCHAR(25) NOT NULL,
    probabilidad_verdad DECIMAL(4,3) NOT NULL,
    epsilon DECIMAL(6,3) NOT NULL
);

CREATE TABLE IF NOT EXISTS item_cuestionario (
    id BIGINT PRIMARY KEY,
    tema VARCHAR(100) NOT NULL,
    escala_id BIGINT NOT NULL,
    configuracion_ldp_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS escala_respuesta (
    id BIGINT PRIMARY KEY,
    valor_min SMALLINT NOT NULL,
    valor_max SMALLINT NOT NULL
);

DELETE FROM agregado_item;
DELETE FROM item_cuestionario;
DELETE FROM escala_respuesta;
DELETE FROM configuracion_ldp;
DELETE FROM equipo;

INSERT INTO equipo (id, empresa_id)
VALUES (100, 10);

INSERT INTO configuracion_ldp
    (id, mecanismo, probabilidad_verdad, epsilon)
VALUES
    (5, 'RANDOMIZED_RESPONSE', 0.750, 1.000);

INSERT INTO escala_respuesta
    (id, valor_min, valor_max)
VALUES
    (2, 0, 1);

INSERT INTO item_cuestionario
    (id, tema, escala_id, configuracion_ldp_id)
VALUES
    (1, 'Bienestar', 2, 5);

INSERT INTO agregado_item
    (equipo_id, item_id, configuracion_ldp_id,
     fecha, suma_reportada, total_respuestas)
VALUES
    (100, 1, 5, DATE '2026-10-12', 12, 15),
    (100, 1, 5, DATE '2026-10-13', 10, 15);
