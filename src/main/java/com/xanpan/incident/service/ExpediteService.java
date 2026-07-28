package com.xanpan.incident.service;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.policy.ExpeditePolicy;
import com.xanpan.incident.repository.IncidentRepository;

public class ExpediteService {

    private final IncidentRepository repository;
    private final ExpeditePolicy expeditePolicy;

    public ExpediteService(IncidentRepository repository) {
        this(repository, new ExpeditePolicy());
    }

    public ExpediteService(IncidentRepository repository, ExpeditePolicy expeditePolicy) {
        this.repository = repository;
        this.expeditePolicy = expeditePolicy;
    }

    public void markExpedited(String incidentId) {
        Incident incident = repository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incidencia no encontrada"));

        if (!expeditePolicy.canMark(incident)) {
            throw new IllegalArgumentException("Solo incidencias criticas pueden marcarse como EXPEDITE");
        }

        if (!expeditePolicy.canEnterActiveState(incident, incident.getState(), repository)) {
            throw new IllegalStateException("Ya existe una incidencia EXPEDITE en desarrollo o validacion");
        }

        incident.setExpedited(true);
        repository.save(incident);
    }
}
