\set ON_ERROR_STOP on

CREATE TYPE user_role AS ENUM ('USUARIO', 'TECNICO', 'ENCARGADO', 'GESTOR');
CREATE TYPE incident_impact AS ENUM ('BAJO', 'MEDIO', 'ALTO');
CREATE TYPE incident_urgency AS ENUM ('BAJA', 'MEDIA', 'ALTA');
CREATE TYPE incident_priority AS ENUM ('NORMAL', 'ALTA', 'CRITICA');
CREATE TYPE incident_status AS ENUM (
    'REGISTRADA',
    'LISTA',
    'EN_DESARROLLO',
    'EN_VALIDACION',
    'FINALIZADA'
);
CREATE TYPE service_class AS ENUM ('NORMAL', 'EXPEDITE');

CREATE TABLE app_user (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(120) NOT NULL CHECK (btrim(name) <> ''),
    email varchar(254) NOT NULL,
    role user_role NOT NULL DEFAULT 'USUARIO',
    active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT current_timestamp,
    CONSTRAINT app_user_email_not_blank CHECK (btrim(email) <> '')
);

CREATE UNIQUE INDEX uq_app_user_email_lower ON app_user (lower(email));

CREATE TABLE category (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(80) NOT NULL,
    description varchar(250),
    active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT current_timestamp,
    CONSTRAINT category_name_not_blank CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX uq_category_name_lower ON category (lower(name));

CREATE SEQUENCE incident_number_seq START WITH 1;

CREATE TABLE incident (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    number bigint NOT NULL DEFAULT nextval('incident_number_seq'),
    title varchar(200) NOT NULL,
    description text NOT NULL,
    impact incident_impact NOT NULL,
    urgency incident_urgency NOT NULL,
    priority incident_priority GENERATED ALWAYS AS (
        CASE
            WHEN impact = 'ALTO' AND urgency = 'ALTA' THEN 'CRITICA'::incident_priority
            WHEN impact = 'ALTO' OR urgency = 'ALTA' THEN 'ALTA'::incident_priority
            ELSE 'NORMAL'::incident_priority
        END
    ) STORED,
    status incident_status NOT NULL DEFAULT 'REGISTRADA',
    service_class service_class NOT NULL DEFAULT 'NORMAL',
    category_id bigint NOT NULL REFERENCES category(id),
    reporter_id uuid NOT NULL REFERENCES app_user(id),
    assignee_id uuid REFERENCES app_user(id),
    solution_description text,
    created_at timestamptz NOT NULL DEFAULT current_timestamp,
    updated_at timestamptz NOT NULL DEFAULT current_timestamp,
    finished_at timestamptz,
    CONSTRAINT uq_incident_number UNIQUE (number),
    CONSTRAINT incident_title_not_blank CHECK (btrim(title) <> ''),
    CONSTRAINT incident_description_min_length CHECK (char_length(btrim(description)) >= 10),
    CONSTRAINT incident_expedite_only_critical CHECK (
        service_class = 'NORMAL' OR priority = 'CRITICA'
    ),
    CONSTRAINT incident_finished_data_consistent CHECK (
        (status = 'FINALIZADA'
            AND solution_description IS NOT NULL
            AND btrim(solution_description) <> ''
            AND finished_at IS NOT NULL)
        OR
        (status <> 'FINALIZADA' AND finished_at IS NULL)
    )
);

COMMENT ON COLUMN incident.number IS 'Identificador secuencial legible para búsquedas y listados.';
COMMENT ON COLUMN incident.priority IS 'Prioridad calculada automáticamente a partir del impacto y la urgencia.';

-- Protege HU-06 también cuando dos transacciones intentan activar EXPEDITE a la vez.
CREATE UNIQUE INDEX uq_single_active_expedite
    ON incident ((service_class))
    WHERE service_class = 'EXPEDITE'
      AND status IN ('EN_DESARROLLO', 'EN_VALIDACION');

CREATE INDEX ix_incident_status ON incident (status);
CREATE INDEX ix_incident_priority ON incident (priority);
CREATE INDEX ix_incident_category ON incident (category_id);
CREATE INDEX ix_incident_created_at ON incident (created_at);
CREATE INDEX ix_incident_finished_at ON incident (finished_at)
    WHERE finished_at IS NOT NULL;
CREATE INDEX ix_incident_open ON incident (updated_at DESC)
    WHERE status <> 'FINALIZADA';

CREATE TABLE incident_status_history (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    incident_id uuid NOT NULL REFERENCES incident(id) ON DELETE CASCADE,
    previous_status incident_status,
    new_status incident_status NOT NULL,
    changed_by uuid REFERENCES app_user(id),
    changed_at timestamptz NOT NULL DEFAULT current_timestamp
);

CREATE INDEX ix_status_history_incident
    ON incident_status_history (incident_id, changed_at);

CREATE FUNCTION validate_incident_change()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.status IS DISTINCT FROM OLD.status THEN
        IF NOT (
            (OLD.status = 'REGISTRADA' AND NEW.status = 'LISTA')
            OR (OLD.status = 'LISTA' AND NEW.status = 'EN_DESARROLLO')
            OR (OLD.status = 'EN_DESARROLLO' AND NEW.status = 'EN_VALIDACION')
            OR (OLD.status = 'EN_VALIDACION' AND NEW.status = 'FINALIZADA')
        ) THEN
            RAISE EXCEPTION 'Transición de estado inválida: % -> %', OLD.status, NEW.status
                USING ERRCODE = 'check_violation';
        END IF;

        IF NEW.status = 'FINALIZADA' THEN
            IF NEW.solution_description IS NULL OR btrim(NEW.solution_description) = '' THEN
                RAISE EXCEPTION 'Una incidencia finalizada requiere la descripción de la solución'
                    USING ERRCODE = 'check_violation';
            END IF;
            NEW.finished_at := current_timestamp;
        END IF;
    END IF;

    NEW.updated_at := current_timestamp;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validate_incident_change
BEFORE UPDATE ON incident
FOR EACH ROW
EXECUTE FUNCTION validate_incident_change();

CREATE FUNCTION record_incident_status()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    actor_id uuid;
BEGIN
    actor_id := nullif(current_setting('xanpan.changed_by', true), '')::uuid;

    IF TG_OP = 'INSERT' THEN
        INSERT INTO incident_status_history (incident_id, previous_status, new_status, changed_by)
        VALUES (NEW.id, NULL, NEW.status, actor_id);
    ELSIF NEW.status IS DISTINCT FROM OLD.status THEN
        INSERT INTO incident_status_history (incident_id, previous_status, new_status, changed_by)
        VALUES (NEW.id, OLD.status, NEW.status, actor_id);
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_record_initial_status
AFTER INSERT ON incident
FOR EACH ROW
EXECUTE FUNCTION record_incident_status();

CREATE TRIGGER trg_record_status_change
AFTER UPDATE OF status ON incident
FOR EACH ROW
EXECUTE FUNCTION record_incident_status();

CREATE VIEW incident_list AS
SELECT
    i.id,
    i.number,
    i.title,
    i.description,
    i.impact,
    i.urgency,
    i.priority,
    i.status,
    i.service_class,
    c.name AS category,
    reporter.name AS reporter,
    assignee.name AS assignee,
    i.solution_description,
    i.created_at,
    i.updated_at,
    i.finished_at
FROM incident i
JOIN category c ON c.id = i.category_id
JOIN app_user reporter ON reporter.id = i.reporter_id
LEFT JOIN app_user assignee ON assignee.id = i.assignee_id;

CREATE VIEW incident_metrics AS
SELECT
    count(*) AS total,
    count(*) FILTER (WHERE status = 'FINALIZADA') AS finished,
    count(*) FILTER (WHERE status <> 'FINALIZADA') AS open,
    avg(finished_at - created_at)
        FILTER (WHERE status = 'FINALIZADA') AS average_lead_time
FROM incident;

CREATE VIEW incidents_by_priority AS
SELECT priorities.priority, count(i.id) AS quantity
FROM unnest(enum_range(NULL::incident_priority)) AS priorities(priority)
LEFT JOIN incident i ON i.priority = priorities.priority
GROUP BY priorities.priority;

CREATE FUNCTION incident_throughput(period_start timestamptz, period_end timestamptz)
RETURNS bigint
LANGUAGE sql
STABLE
AS $$
    SELECT count(*)
    FROM incident
    WHERE finished_at >= period_start
      AND finished_at < period_end;
$$;
