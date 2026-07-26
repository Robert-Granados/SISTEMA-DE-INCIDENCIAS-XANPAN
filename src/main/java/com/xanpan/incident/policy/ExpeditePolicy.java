package com.xanpan.incident.policy;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.repository.IncidentRepository;

public interface ExpeditePolicy {

    boolean canExpedite(Incident incident);

    boolean isExpeditable(IncidentRepository repository);
}
