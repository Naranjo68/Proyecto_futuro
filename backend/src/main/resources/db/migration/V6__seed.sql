INSERT INTO escala_respuesta (codigo, valor_min, valor_max) VALUES ('BINARIA', 0, 1);

INSERT INTO opcion_escala (escala_id, valor, etiqueta)
SELECT id, 0, 'No' FROM escala_respuesta WHERE codigo = 'BINARIA';
INSERT INTO opcion_escala (escala_id, valor, etiqueta)
SELECT id, 1, 'Sí' FROM escala_respuesta WHERE codigo = 'BINARIA';

INSERT INTO configuracion_ldp (mecanismo, probabilidad_verdad) VALUES ('RANDOMIZED_RESPONSE', 0.750);

INSERT INTO cuestionario (codigo, nombre, cadencia_dias, items_por_envio)
VALUES ('PULSO', 'Pulso semanal', 7, NULL);

INSERT INTO item_cuestionario (cuestionario_id, orden, tema, texto, escala_id, configuracion_ldp_id)
SELECT c.id, 1, 'Pulso', '¿Hoy te sentiste bien?', e.id, l.id
FROM cuestionario c, escala_respuesta e, configuracion_ldp l
WHERE c.codigo = 'PULSO' AND e.codigo = 'BINARIA' AND l.mecanismo = 'RANDOMIZED_RESPONSE';
