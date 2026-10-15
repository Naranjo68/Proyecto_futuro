-- El CHECK de V3 acepta LAPLACE sin epsilon y RANDOMIZED_RESPONSE sin probabilidad_verdad: en SQL un CHECK cuyo
-- resultado es NULL se considera cumplido, y "epsilon > 0" con epsilon NULL da NULL. Se exige NOT NULL de forma explicita.
ALTER TABLE configuracion_ldp DROP CONSTRAINT ck_ldp_mecanismo;
ALTER TABLE configuracion_ldp ADD CONSTRAINT ck_ldp_mecanismo CHECK (
    (mecanismo = 'RANDOMIZED_RESPONSE'
        AND probabilidad_verdad IS NOT NULL AND probabilidad_verdad > 0.5 AND probabilidad_verdad < 1
        AND epsilon IS NULL)
 OR (mecanismo = 'LAPLACE'
        AND epsilon IS NOT NULL AND epsilon > 0
        AND probabilidad_verdad IS NULL)
);
