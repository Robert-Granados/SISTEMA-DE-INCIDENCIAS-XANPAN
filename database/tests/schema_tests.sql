\set ON_ERROR_STOP on

BEGIN;

INSERT INTO app_user (id, name, email, role) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Usuario de prueba', 'usuario@test.local', 'USUARIO'),
    ('00000000-0000-0000-0000-000000000002', 'Técnico de prueba', 'tecnico@test.local', 'TECNICO');

INSERT INTO incident (
    id, title, description, impact, urgency, category_id, reporter_id
) VALUES (
    '10000000-0000-0000-0000-000000000001',
    'Incidencia crítica',
    'Descripción suficientemente larga',
    'ALTO',
    'ALTA',
    (SELECT id FROM category WHERE name = 'Software'),
    '00000000-0000-0000-0000-000000000001'
);

DO $$
DECLARE
    actual_priority incident_priority;
BEGIN
    SELECT priority INTO actual_priority
    FROM incident
    WHERE id = '10000000-0000-0000-0000-000000000001';

    IF actual_priority <> 'CRITICA' THEN
        RAISE EXCEPTION 'Prioridad esperada CRITICA, obtenida %', actual_priority;
    END IF;
END;
$$;

UPDATE incident SET status = 'LISTA'
WHERE id = '10000000-0000-0000-0000-000000000001';
UPDATE incident SET status = 'EN_DESARROLLO', service_class = 'EXPEDITE'
WHERE id = '10000000-0000-0000-0000-000000000001';
UPDATE incident SET status = 'EN_VALIDACION'
WHERE id = '10000000-0000-0000-0000-000000000001';

-- No se permite saltar ni retroceder estados.
DO $$
BEGIN
    BEGIN
        UPDATE incident SET status = 'LISTA'
        WHERE id = '10000000-0000-0000-0000-000000000001';
        RAISE EXCEPTION 'La transición inválida no fue rechazada';
    EXCEPTION
        WHEN check_violation THEN NULL;
    END;
END;
$$;

-- Solo puede haber un EXPEDITE en desarrollo o validación.
INSERT INTO incident (
    id, title, description, impact, urgency, service_class, category_id, reporter_id
) VALUES (
    '10000000-0000-0000-0000-000000000002',
    'Segunda incidencia crítica',
    'Otra descripción suficientemente larga',
    'ALTO',
    'ALTA',
    'EXPEDITE',
    (SELECT id FROM category WHERE name = 'Software'),
    '00000000-0000-0000-0000-000000000001'
);
UPDATE incident SET status = 'LISTA'
WHERE id = '10000000-0000-0000-0000-000000000002';

DO $$
BEGIN
    BEGIN
        UPDATE incident SET status = 'EN_DESARROLLO'
        WHERE id = '10000000-0000-0000-0000-000000000002';
        RAISE EXCEPTION 'Una segunda incidencia EXPEDITE activa no fue rechazada';
    EXCEPTION
        WHEN unique_violation THEN NULL;
    END;
END;
$$;

UPDATE incident
SET status = 'FINALIZADA', solution_description = 'Se reinstaló la aplicación afectada.'
WHERE id = '10000000-0000-0000-0000-000000000001';

DO $$
DECLARE
    history_count bigint;
    finished_date timestamptz;
BEGIN
    SELECT count(*) INTO history_count
    FROM incident_status_history
    WHERE incident_id = '10000000-0000-0000-0000-000000000001';

    SELECT finished_at INTO finished_date
    FROM incident
    WHERE id = '10000000-0000-0000-0000-000000000001';

    IF history_count <> 5 THEN
        RAISE EXCEPTION 'Se esperaban 5 registros de historial, obtenidos %', history_count;
    END IF;
    IF finished_date IS NULL THEN
        RAISE EXCEPTION 'La fecha de finalización no fue registrada';
    END IF;
END;
$$;

ROLLBACK;

\echo 'Pruebas SQL completadas correctamente.'
