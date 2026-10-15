-- Claves foraneas entre modulos que las migraciones anteriores dejaron sin declarar.
-- Privacidad (R1, R6): sesion_checkin y agregado_item NUNCA referencian a usuario ni a empleado_equipo.
-- agregado_item solo apunta a equipo, item y configuracion; sesion_checkin solo a cuestionario.

ALTER TABLE empleado_equipo
    ADD CONSTRAINT fk_empleado_equipo FOREIGN KEY (equipo_id) REFERENCES equipo(id);

ALTER TABLE personal_empresa
    ADD CONSTRAINT fk_personal_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id);

ALTER TABLE empresa_cuestionario
    ADD CONSTRAINT fk_ec_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id);

ALTER TABLE presupuesto_privacidad
    ADD CONSTRAINT fk_presupuesto_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id);

ALTER TABLE agregado_item
    ADD CONSTRAINT fk_agregado_equipo FOREIGN KEY (equipo_id) REFERENCES equipo(id),
    ADD CONSTRAINT fk_agregado_item FOREIGN KEY (item_id) REFERENCES item_cuestionario(id),
    ADD CONSTRAINT fk_agregado_config FOREIGN KEY (configuracion_ldp_id) REFERENCES configuracion_ldp(id);

ALTER TABLE sesion_checkin
    ADD CONSTRAINT fk_sesion_cuestionario FOREIGN KEY (cuestionario_id) REFERENCES cuestionario(id);

ALTER TABLE consulta_dashboard
    ADD CONSTRAINT fk_consulta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    ADD CONSTRAINT fk_consulta_equipo FOREIGN KEY (equipo_id) REFERENCES equipo(id);
