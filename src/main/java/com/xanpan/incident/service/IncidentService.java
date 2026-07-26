package com.xanpan.incident.service;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;
import com.xanpan.incident.repository.IncidentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IncidentService {

    private final IncidentRepository repository;
    private final PriorityCalculator priorityCalculator;
    private final StateTransitionValidator stateValidator;

    public IncidentService(IncidentRepository repository, PriorityCalculator priorityCalculator, StateTransitionValidator stateValidator) {
        this.repository = repository;
        this.priorityCalculator = priorityCalculator;
        this.stateValidator = stateValidator;
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
        Incident incident = new Incident(id, title, description, impact, urgency, priority, category);
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
