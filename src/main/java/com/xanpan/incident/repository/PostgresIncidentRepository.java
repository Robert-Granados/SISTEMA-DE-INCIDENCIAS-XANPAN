package com.xanpan.incident.repository;

import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PostgresIncidentRepository implements IncidentRepository {

    private static final String SELECT_COLUMNS = """
            SELECT i.id, i.title, i.description, i.impact, i.urgency, i.priority,
                   i.status, i.service_class, c.name AS category,
                   i.solution_description, i.created_at, i.updated_at, i.finished_at
            FROM incident i
            JOIN category c ON c.id = i.category_id
            """;
    private static final String SYSTEM_REPORTER_EMAIL = "desktop@xanpan.local";

    private final DatabaseConfig config;

    public PostgresIncidentRepository(DatabaseConfig config) {
        this.config = config;
    }

    @Override
    public void save(Incident incident) {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                long categoryId = findOrCreateCategory(connection, incident.getCategory());
                UUID reporterId = findOrCreateSystemReporter(connection);
                if (exists(connection, incident.getId())) {
                    update(connection, incident, categoryId);
                } else {
                    insert(connection, incident, categoryId, reporterId);
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw persistenceFailure("guardar", exception);
        }
    }

    @Override
    public Optional<Incident> findById(String id) {
        return queryOne(SELECT_COLUMNS + " WHERE i.id = ?::uuid", id);
    }

    @Override
    public List<Incident> findAll() {
        return query(SELECT_COLUMNS + " ORDER BY i.created_at DESC");
    }

    @Override
    public List<Incident> findByState(IncidentState state) {
        return query(SELECT_COLUMNS + " WHERE i.status = ?::incident_status", state.name());
    }

    @Override
    public List<Incident> findByPriority(Priority priority) {
        return query(SELECT_COLUMNS + " WHERE i.priority = ?::incident_priority", priority.name());
    }

    @Override
    public List<Incident> findAllOpen() {
        return query(SELECT_COLUMNS + " WHERE i.status <> 'FINALIZADA'");
    }

    @Override
    public List<Incident> findAllClosed() {
        return query(SELECT_COLUMNS + " WHERE i.status = 'FINALIZADA'");
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }

    private Optional<Incident> queryOne(String sql, String parameter) {
        List<Incident> incidents = query(sql, parameter);
        return incidents.stream().findFirst();
    }

    private List<Incident> query(String sql, String... parameters) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) {
                statement.setString(index + 1, parameters[index]);
            }
            try (ResultSet result = statement.executeQuery()) {
                List<Incident> incidents = new ArrayList<>();
                while (result.next()) {
                    incidents.add(map(result));
                }
                return incidents;
            }
        } catch (SQLException exception) {
            throw persistenceFailure("consultar", exception);
        }
    }

    private static boolean exists(Connection connection, String id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM incident WHERE id = ?::uuid")) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static long findOrCreateCategory(Connection connection, String category)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO category (name)
                VALUES (?)
                ON CONFLICT ((lower(name))) DO UPDATE SET name = EXCLUDED.name
                RETURNING id
                """)) {
            statement.setString(1, category);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    private static UUID findOrCreateSystemReporter(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO app_user (name, email, role)
                VALUES ('Aplicacion de escritorio', ?, 'USUARIO')
                ON CONFLICT ((lower(email))) DO UPDATE SET active = true
                RETURNING id
                """)) {
            statement.setString(1, SYSTEM_REPORTER_EMAIL);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getObject(1, UUID.class);
            }
        }
    }

    private static void insert(
            Connection connection,
            Incident incident,
            long categoryId,
            UUID reporterId
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO incident (
                    id, title, description, impact, urgency, status, service_class,
                    category_id, reporter_id, solution_description, created_at,
                    updated_at, finished_at
                ) VALUES (
                    ?::uuid, ?, ?, ?::incident_impact, ?::incident_urgency,
                    ?::incident_status, ?::service_class, ?, ?, ?, ?, ?, ?
                )
                """)) {
            setIncidentValues(statement, incident, categoryId);
            statement.setObject(9, reporterId);
            statement.setString(10, incident.getSolutionDescription());
            statement.setTimestamp(11, Timestamp.valueOf(incident.getCreatedAt()));
            statement.setTimestamp(12, Timestamp.valueOf(incident.getUpdatedAt()));
            setTimestamp(statement, 13, incident.getClosedAt());
            statement.executeUpdate();
        }
    }

    private static void update(Connection connection, Incident incident, long categoryId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE incident SET
                    title = ?, description = ?, impact = ?::incident_impact,
                    urgency = ?::incident_urgency, status = ?::incident_status,
                    service_class = ?::service_class, category_id = ?,
                    solution_description = ?
                WHERE id = ?::uuid
                """)) {
            statement.setString(1, incident.getTitle());
            statement.setString(2, incident.getDescription());
            statement.setString(3, incident.getImpact().name());
            statement.setString(4, incident.getUrgency().name());
            statement.setString(5, incident.getState().name());
            statement.setString(6, serviceClass(incident));
            statement.setLong(7, categoryId);
            statement.setString(8, incident.getSolutionDescription());
            statement.setString(9, incident.getId());
            statement.executeUpdate();
        }
    }

    private static void setIncidentValues(
            PreparedStatement statement,
            Incident incident,
            long categoryId
    ) throws SQLException {
        statement.setString(1, incident.getId());
        statement.setString(2, incident.getTitle());
        statement.setString(3, incident.getDescription());
        statement.setString(4, incident.getImpact().name());
        statement.setString(5, incident.getUrgency().name());
        statement.setString(6, incident.getState().name());
        statement.setString(7, serviceClass(incident));
        statement.setLong(8, categoryId);
    }

    private static String serviceClass(Incident incident) {
        return incident.isExpedited() ? "EXPEDITE" : "NORMAL";
    }

    private static void setTimestamp(
            PreparedStatement statement,
            int index,
            LocalDateTime value
    ) throws SQLException {
        statement.setTimestamp(index, value == null ? null : Timestamp.valueOf(value));
    }

    private static Incident map(ResultSet result) throws SQLException {
        Timestamp finishedAt = result.getTimestamp("finished_at");
        return Incident.restore(
                result.getString("id"),
                result.getString("title"),
                result.getString("description"),
                Impact.valueOf(result.getString("impact")),
                Urgency.valueOf(result.getString("urgency")),
                Priority.valueOf(result.getString("priority")),
                IncidentState.valueOf(result.getString("status")),
                result.getString("category"),
                result.getString("solution_description"),
                "EXPEDITE".equals(result.getString("service_class")),
                result.getTimestamp("created_at").toLocalDateTime(),
                result.getTimestamp("updated_at").toLocalDateTime(),
                finishedAt == null ? null : finishedAt.toLocalDateTime()
        );
    }

    private static IllegalStateException persistenceFailure(
            String operation,
            SQLException cause
    ) {
        return new IllegalStateException(
                "No se pudo " + operation + " incidencias en PostgreSQL", cause
        );
    }
}
