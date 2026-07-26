package com.xanpan.incident.service;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.repository.IncidentRepository;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MetricsService {

    private final IncidentRepository repository;

    public MetricsService(IncidentRepository repository) {
        this.repository = repository;
    }

    public int totalIncidents() {
        return repository.findAll().size();
    }

    public int closedIncidents() {
        return repository.findAllClosed().size();
    }

    public int openIncidents() {
        return repository.findAllOpen().size();
    }

    public long throughput() {
        return repository.findAllClosed().size();
    }

    public double averageLeadTimeMinutes() {
        List<Incident> closed = repository.findAllClosed();
        if (closed.isEmpty()) {
            return 0.0;
        }
        long totalMinutes = closed.stream()
                .mapToLong(inc -> Duration.between(inc.getCreatedAt(), inc.getUpdatedAt()).toMinutes())
                .sum();
        return (double) totalMinutes / closed.size();
    }

    public Map<Priority, Long> countByPriority() {
        Map<Priority, Long> counts = new EnumMap<>(Priority.class);
        for (Priority p : Priority.values()) {
            counts.put(p, (long) repository.findByPriority(p).size());
        }
        return counts;
    }
}
