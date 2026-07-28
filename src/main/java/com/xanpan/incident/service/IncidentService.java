package com.xanpan.incident.service;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;
import com.xanpan.incident.policy.ExpeditePolicy;
import com.xanpan.incident.repository.IncidentRepository;
import java.time.Clock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IncidentService {

    private final IncidentRepository repository;
    private final PriorityCalculator priorityCalculator;
    private final StateTransitionValidator stateValidator;
    private final ExpeditePolicy expeditePolicy;
    private final Clock clock;

    public IncidentService(IncidentRepository repository, PriorityCalculator priorityCalculator, StateTransitionValidator stateValidator) {
        this(repository, priorityCalculator, stateValidator, new ExpeditePolicy(), Clock.systemDefaultZone());
    }

    public IncidentService(
            IncidentRepository repository,
            PriorityCalculator priorityCalculator,
            StateTransitionValidator stateValidator,
            ExpeditePolicy expeditePolicy
    ) {
        this(repository, priorityCalculator, stateValidator, expeditePolicy, Clock.systemDefaultZone());
    }

    public IncidentService(
            IncidentRepository repository,
            PriorityCalculator priorityCalculator,
            StateTransitionValidator stateValidator,
            Clock clock
    ) {
        this(repository, priorityCalculator, stateValidator, new ExpeditePolicy(), clock);
    }

    public IncidentService(
            IncidentRepository repository,
            PriorityCalculator priorityCalculator,
            StateTransitionValidator stateValidator,
            ExpeditePolicy expeditePolicy,
            Clock clock
    ) {
        this.repository = repository;
        this.priorityCalculator = priorityCalculator;
        this.stateValidator = stateValidator;
        this.expeditePolicy = expeditePolicy;
        this.clock = clock;
    }

    public Incident createIncident(String title, String description, Impact impact, Urgency urgency, String category) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El titulo no puede estar vacio");
        }
        if (description == null || description.length() < 10) {
            throw new IllegalArgumentException("La descripcion debe contener al menos diez caracteres");
        }

        String id = UUID.randomUUID().toString();
        Priority priority = priorityCalculator.calculate(impact, urgency);
        Incident incident = new Incident(
                id,
                title,
                description,
                impact,
                urgency,
                priority,
                category,
                clock
        );
        repository.save(incident);
        return incident;
    }

    public Optional<Incident> transitionState(String incidentId, IncidentState newState) {
        Optional<Incident> optIncident = repository.findById(incidentId);
        if (optIncident.isEmpty()) {
            return Optional.empty();
        }
        Incident incident = optIncident.get();
        if (!stateValidator.isValidTransition(incident.getState(), newState)) {
            return Optional.empty();
        }
        if (newState == IncidentState.FINALIZADA
                && (incident.getSolutionDescription() == null
                || incident.getSolutionDescription().isBlank())) {
            return Optional.empty();
        }
        if (incident.isExpedited()
                && !expeditePolicy.canEnterActiveState(incident, newState, repository)) {
            return Optional.empty();
        }
        incident.setState(newState);
        repository.save(incident);
        return Optional.of(incident);
    }

    public Optional<Incident> completeIncident(String incidentId, String solutionDescription) {
        Optional<Incident> optIncident = repository.findById(incidentId);
        if (optIncident.isEmpty()) {
            return Optional.empty();
        }
        Incident incident = optIncident.get();
        if (incident.getState() != IncidentState.EN_VALIDACION) {
            throw new IllegalStateException("Solo se puede finalizar desde EN_VALIDACION");
        }
        if (solutionDescription == null || solutionDescription.isBlank()) {
            throw new IllegalArgumentException("La solucion no puede estar vacia");
        }
        incident.setSolutionDescription(solutionDescription);
        incident.setState(IncidentState.FINALIZADA);
        repository.save(incident);
        return Optional.of(incident);
    }

    public Optional<Incident> findById(String id) {
        return repository.findById(id);
    }

    public List<Incident> findAll() {
        return repository.findAll();
    }

    public List<Incident> findByState(IncidentState state) {
        return repository.findByState(state);
    }

    public List<Incident> findByPriority(Priority priority) {
        return repository.findByPriority(priority);
    }

    public List<Incident> findOpen() {
        return repository.findAllOpen();
    }

    public List<Incident> findClosed() {
        return repository.findAllClosed();
    }
}
