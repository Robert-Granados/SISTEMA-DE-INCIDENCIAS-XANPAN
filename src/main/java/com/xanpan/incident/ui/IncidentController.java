package com.xanpan.incident.ui;

import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;
import com.xanpan.incident.repository.InMemoryIncidentRepository;
import com.xanpan.incident.repository.IncidentRepository;
import com.xanpan.incident.service.ExpediteService;
import com.xanpan.incident.service.IncidentService;
import com.xanpan.incident.service.MetricsService;
import com.xanpan.incident.service.PriorityCalculator;
import com.xanpan.incident.service.StateTransitionValidator;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Presentation controller used by the Swing interface.
 *
 * <p>It coordinates the domain services and converts user-oriented operations
 * into calls to the existing application rules. It deliberately contains no
 * Swing classes, which keeps it deterministic and easy to test.</p>
 */
public final class IncidentController {

    private final IncidentService incidentService;
    private final ExpediteService expediteService;
    private final MetricsService metricsService;

    public IncidentController(IncidentRepository repository) {
        this(
                new IncidentService(
                        repository,
                        new PriorityCalculator(),
                        new StateTransitionValidator()
                ),
                new ExpediteService(repository),
                new MetricsService(repository)
        );
    }

    public IncidentController(
            IncidentService incidentService,
            ExpediteService expediteService,
            MetricsService metricsService
    ) {
        this.incidentService = incidentService;
        this.expediteService = expediteService;
        this.metricsService = metricsService;
    }

    public static IncidentController inMemory() {
        return new IncidentController(new InMemoryIncidentRepository());
    }

    public Incident registerIncident(
            String title,
            String description,
            Impact impact,
            Urgency urgency,
            String category
    ) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("La categoria no puede estar vacia");
        }
        if (impact == null || urgency == null) {
            throw new IllegalArgumentException("Impacto y urgencia son obligatorios");
        }
        return incidentService.createIncident(
                title,
                description,
                impact,
                urgency,
                category.trim()
        );
    }

    public List<Incident> listIncidents(
            String search,
            Scope scope,
            IncidentState state,
            Priority priority
    ) {
        String normalizedSearch = search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);
        Scope selectedScope = scope == null ? Scope.ALL : scope;

        return incidentService.findAll().stream()
                .filter(incident -> selectedScope.matches(incident))
                .filter(incident -> state == null || incident.getState() == state)
                .filter(incident -> priority == null || incident.getPriority() == priority)
                .filter(incident -> normalizedSearch.isEmpty()
                        || contains(incident.getId(), normalizedSearch)
                        || contains(incident.getTitle(), normalizedSearch)
                        || contains(incident.getDescription(), normalizedSearch)
                        || contains(incident.getCategory(), normalizedSearch))
                .sorted(Comparator.comparing(Incident::getCreatedAt).reversed())
                .toList();
    }

    public Incident findRequired(String incidentId) {
        return incidentService.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incidencia no encontrada"));
    }

    public Incident advanceIncident(String incidentId) {
        Incident incident = findRequired(incidentId);
        IncidentState nextState = switch (incident.getState()) {
            case REGISTRADA -> IncidentState.LISTA;
            case LISTA -> IncidentState.EN_DESARROLLO;
            case EN_DESARROLLO -> IncidentState.EN_VALIDACION;
            case EN_VALIDACION ->
                    throw new IllegalStateException("Use Finalizar para registrar la solucion");
            case FINALIZADA ->
                    throw new IllegalStateException("La incidencia ya esta finalizada");
        };

        return incidentService.transitionState(incidentId, nextState)
                .orElseThrow(() -> new IllegalStateException(
                        "La transicion a " + nextState + " no esta permitida"
                ));
    }

    public Incident markExpedited(String incidentId) {
        expediteService.markExpedited(incidentId);
        return findRequired(incidentId);
    }

    public Incident completeIncident(String incidentId, String solution) {
        return incidentService.completeIncident(incidentId, solution)
                .orElseThrow(() -> new IllegalArgumentException("Incidencia no encontrada"));
    }

    public MetricsSnapshot metrics() {
        return new MetricsSnapshot(
                metricsService.totalIncidents(),
                metricsService.openIncidents(),
                metricsService.closedIncidents(),
                metricsService.throughput(),
                metricsService.averageLeadTimeMinutes(),
                metricsService.countByPriority()
        );
    }

    private static boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    public enum Scope {
        ALL("Todas"),
        OPEN("Abiertas"),
        CLOSED("Finalizadas");

        private final String label;

        Scope(String label) {
            this.label = label;
        }

        private boolean matches(Incident incident) {
            return switch (this) {
                case ALL -> true;
                case OPEN -> incident.getState() != IncidentState.FINALIZADA;
                case CLOSED -> incident.getState() == IncidentState.FINALIZADA;
            };
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public record MetricsSnapshot(
            int total,
            int open,
            int closed,
            long throughput,
            double averageLeadTimeMinutes,
            Map<Priority, Long> byPriority
    ) {
        public MetricsSnapshot {
            byPriority = Map.copyOf(byPriority);
        }
    }
}
