package com.xanpan.incident.service;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.repository.IncidentRepository;

import java.time.Duration;
import java.time.LocalDateTime;
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

    public long throughput(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        if (startInclusive == null || endExclusive == null) {
            throw new IllegalArgumentException("El periodo de throughput es obligatorio");
        }
        if (!startInclusive.isBefore(endExclusive)) {
            throw new IllegalArgumentException("El inicio debe ser anterior al fin del periodo");
        }
        return repository.findAllClosed().stream()
                .map(Incident::getClosedAt)
                .filter(closedAt -> closedAt != null
                        && !closedAt.isBefore(startInclusive)
                        && closedAt.isBefore(endExclusive))
                .count();
    }

    public double averageLeadTimeMinutes() {
        List<Incident> closed = repository.findAllClosed().stream()
                .filter(incident -> incident.getClosedAt() != null)
                .toList();
        if (closed.isEmpty()) {
            return 0.0;
        }
        double totalMinutes = closed.stream()
                .mapToDouble(incident -> Duration.between(
                        incident.getCreatedAt(),
                        incident.getClosedAt()
                ).toMillis() / 60_000.0)
                .sum();
        return totalMinutes / closed.size();
    }

    public Map<Priority, Long> countByPriority() {
        Map<Priority, Long> counts = new EnumMap<>(Priority.class);
        for (Priority p : Priority.values()) {
            counts.put(p, (long) repository.findByPriority(p).size());
        }
        return counts;
    }
}
