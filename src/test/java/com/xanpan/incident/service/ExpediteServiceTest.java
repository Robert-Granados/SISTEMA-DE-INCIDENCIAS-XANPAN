package com.xanpan.incident.service;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;
import com.xanpan.incident.repository.InMemoryIncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpediteServiceTest {

    private InMemoryIncidentRepository repository;
    private IncidentService incidentService;
    private ExpediteService expediteService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryIncidentRepository();
        incidentService = new IncidentService(repository, new PriorityCalculator(), new StateTransitionValidator());
        expediteService = new ExpediteService(repository);
    }

    @Test
    void shouldMarkCriticalIncidentAsExpedited() {
        Incident incident = incidentService.createIncident("Critical", "Description for critical issue", Impact.ALTO, Urgency.ALTA, "Software");

        expediteService.markExpedited(incident.getId());

        var found = repository.findById(incident.getId());
        assertTrue(found.isPresent());
        assertTrue(found.get().isExpedited());
    }

    @Test
    void shouldRejectNonCriticalIncidentForExpedite() {
        Incident incident = incidentService.createIncident("Normal", "Description for normal issue", Impact.BAJO, Urgency.BAJA, "Hardware");

        assertThrows(IllegalArgumentException.class, () ->
                expediteService.markExpedited(incident.getId()));
    }

    @Test
    void shouldRejectHighPriorityNonCriticalForExpedite() {
        Incident incident = incidentService.createIncident("Alta", "Description for high issue", Impact.ALTO, Urgency.MEDIA, "Software");

        assertThrows(IllegalArgumentException.class, () ->
                expediteService.markExpedited(incident.getId()));
    }

    @Test
    void shouldAllowSecondExpediteWhenFirstIsFinished() {
        Incident first = incidentService.createIncident("First", "First critical incident desc", Impact.ALTO, Urgency.ALTA, "Software");
        moveToEndValidacion(first.getId());
        expediteService.markExpedited(first.getId());
        incidentService.completeIncident(first.getId(), "Solution 1");

        Incident second = incidentService.createIncident("Second", "Second critical incident", Impact.ALTO, Urgency.ALTA, "Software");
        expediteService.markExpedited(second.getId());

        assertTrue(repository.findById(first.getId()).get().isExpedited());
        assertTrue(repository.findById(second.getId()).get().isExpedited());
    }

    @Test
    void shouldRejectSecondExpediteWhileOneIsActive() {
        Incident first = incidentService.createIncident("First", "First critical incident desc", Impact.ALTO, Urgency.ALTA, "Software");
        moveToEndValidacion(first.getId());
        expediteService.markExpedited(first.getId());

        Incident second = incidentService.createIncident("Second", "Second critical incident", Impact.ALTO, Urgency.ALTA, "Software");
        moveToEndValidacion(second.getId());

        assertThrows(IllegalStateException.class, () ->
                expediteService.markExpedited(second.getId()));
    }

    @Test
    void shouldAllowExpediteWhenFirstIsRegistrada() {
        Incident first = incidentService.createIncident("First", "First critical incident desc", Impact.ALTO, Urgency.ALTA, "Software");
        expediteService.markExpedited(first.getId());

        Incident second = incidentService.createIncident("Second", "Second critical incident", Impact.ALTO, Urgency.ALTA, "Software");
        moveToEndValidacion(second.getId());
        expediteService.markExpedited(second.getId());

        assertTrue(repository.findById(first.getId()).get().isExpedited());
        assertTrue(repository.findById(second.getId()).get().isExpedited());
    }

    @Test
    void shouldIdentifyExpeditedInQueries() {
        Incident incident = incidentService.createIncident("Critical", "Description for critical issue", Impact.ALTO, Urgency.ALTA, "Software");
        expediteService.markExpedited(incident.getId());

        var found = repository.findById(incident.getId());
        assertTrue(found.isPresent());
        assertTrue(found.get().isExpedited());
    }

    @Test
    void shouldNotAlterNormalIncidentBehavior() {
        Incident normal = incidentService.createIncident("Normal", "Description for normal issue", Impact.BAJO, Urgency.BAJA, "Hardware");

        assertFalse(normal.isExpedited());
        assertEquals(Priority.NORMAL, normal.getPriority());
        assertEquals(IncidentState.REGISTRADA, normal.getState());
    }

    private void moveToEndValidacion(String incidentId) {
        incidentService.transitionState(incidentId, IncidentState.LISTA);
        incidentService.transitionState(incidentId, IncidentState.EN_DESARROLLO);
        incidentService.transitionState(incidentId, IncidentState.EN_VALIDACION);
    }
}
