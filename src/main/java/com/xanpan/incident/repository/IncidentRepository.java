package com.xanpan.incident.repository;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Priority;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository {

    void save(Incident incident);

    Optional<Incident> findById(String id);

    List<Incident> findAll();

    List<Incident> findByState(IncidentState state);

    List<Incident> findByPriority(Priority priority);

    List<Incident> findAllOpen();

    List<Incident> findAllClosed();
}
