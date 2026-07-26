package com.xanpan.incident.service;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.repository.IncidentRepository;

import java.util.List;

public class ExpediteService {

    private final IncidentRepository repository;

    public ExpediteService(IncidentRepository repository) {
        this.repository = repository;
    }

    public void markExpedited(String incidentId) {
        Incident incident = repository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incidencia no encontrada"));

        if (incident.getPriority() != Priority.CRITICA) {
            throw new IllegalArgumentException("Solo incidencias criticas pueden marcarse como EXPEDITE");
        }

        if (hasActiveExpedited(incidentId)) {
            throw new IllegalStateException("Ya existe una incidencia EXPEDITE en desarrollo o validacion");
        }

        incident.setExpedited(true);
        repository.save(incident);
    }

    private boolean hasActiveExpedited(String excludeId) {
        List<Incident> all = repository.findAll();
        return all.stream()
                .filter(inc -> !inc.getId().equals(excludeId))
                .filter(Incident::isExpedited)
                .anyMatch(inc -> inc.getState() == IncidentState.EN_DESARROLLO
                        || inc.getState() == IncidentState.EN_VALIDACION);
    }
}
