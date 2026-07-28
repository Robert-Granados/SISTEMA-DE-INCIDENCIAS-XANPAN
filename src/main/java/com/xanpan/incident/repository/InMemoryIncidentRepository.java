package com.xanpan.incident.repository;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Priority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryIncidentRepository implements IncidentRepository {

    private final Map<String, Incident> store = new HashMap<>();

    @Override
    public void save(Incident incident) {
        store.put(incident.getId(), incident);
    }

    @Override
    public Optional<Incident> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Incident> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Incident> findByState(IncidentState state) {
        return store.values().stream()
                .filter(inc -> inc.getState() == state)
                .collect(Collectors.toList());
    }

    @Override
    public List<Incident> findByPriority(Priority priority) {
        return store.values().stream()
                .filter(inc -> inc.getPriority() == priority)
                .collect(Collectors.toList());
    }

    @Override
    public List<Incident> findAllOpen() {
        return store.values().stream()
                .filter(inc -> inc.getState() != IncidentState.FINALIZADA)
                .collect(Collectors.toList());
    }

    @Override
    public List<Incident> findAllClosed() {
        return store.values().stream()
                .filter(inc -> inc.getState() == IncidentState.FINALIZADA)
                .collect(Collectors.toList());
    }
}
