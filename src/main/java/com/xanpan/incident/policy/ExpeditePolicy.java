package com.xanpan.incident.policy;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.repository.IncidentRepository;

public final class ExpeditePolicy {

    public boolean canMark(Incident incident) {
        return incident.getPriority() == Priority.CRITICA;
    }

    public boolean canEnterActiveState(
            Incident incident,
            IncidentState targetState,
            IncidentRepository repository
    ) {
        if (!isActiveState(targetState)) {
            return true;
        }
        return repository.findAll().stream()
                .filter(other -> !other.getId().equals(incident.getId()))
                .filter(Incident::isExpedited)
                .noneMatch(other -> isActiveState(other.getState()));
    }

    public boolean isActiveState(IncidentState state) {
        return state == IncidentState.EN_DESARROLLO
                || state == IncidentState.EN_VALIDACION;
    }
}
