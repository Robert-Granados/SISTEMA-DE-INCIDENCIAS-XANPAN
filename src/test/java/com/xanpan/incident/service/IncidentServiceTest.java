package com.xanpan.incident.service;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;
import com.xanpan.incident.repository.InMemoryIncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentServiceTest {

    private InMemoryIncidentRepository repository;
    private IncidentService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryIncidentRepository();
        service = new IncidentService(repository, new PriorityCalculator(), new StateTransitionValidator());
    }

    @Test
    void shouldCreateIncidentWithGeneratedIdAndCorrectPriority() {
        Incident incident = service.createIncident("Title", "Description text", Impact.ALTO, Urgency.ALTA, "Software");

        assertNotNull(incident.getId());
        assertEquals(Priority.CRITICA, incident.getPriority());
        assertEquals(IncidentState.REGISTRADA, incident.getState());
    }

    @Test
    void shouldPersistCreatedIncident() {
        Incident incident = service.createIncident("Title", "Description text", Impact.BAJO, Urgency.BAJA, "Hardware");

        Optional<Incident> found = service.findById(incident.getId());
        assertTrue(found.isPresent());
        assertEquals("Title", found.get().getTitle());
    }

    @Test
    void shouldTransitionToNextValidState() {
        Incident incident = service.createIncident("Title", "Description text", Impact.MEDIO, Urgency.MEDIA, "Red");

        Optional<Incident> result = service.transitionState(incident.getId(), IncidentState.LISTA);

        assertTrue(result.isPresent());
        assertEquals(IncidentState.LISTA, result.get().getState());
    }

    @Test
    void shouldRejectInvalidTransition() {
        Incident incident = service.createIncident("Title", "Description text", Impact.MEDIO, Urgency.MEDIA, "Red");

        Optional<Incident> result = service.transitionState(incident.getId(), IncidentState.EN_DESARROLLO);

        assertFalse(result.isPresent());
        assertEquals(IncidentState.REGISTRADA, incident.getState());
    }

    @Test
    void shouldReturnEmptyWhenIncidentNotFound() {
        Optional<Incident> result = service.transitionState("nonexistent-id", IncidentState.LISTA);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldCompleteFullValidFlow() {
        Incident incident = service.createIncident("Title", "Description text", Impact.ALTO, Urgency.MEDIA, "Software");

        service.transitionState(incident.getId(), IncidentState.LISTA);
        service.transitionState(incident.getId(), IncidentState.EN_DESARROLLO);
        service.transitionState(incident.getId(), IncidentState.EN_VALIDACION);
        service.transitionState(incident.getId(), IncidentState.FINALIZADA);

        Optional<Incident> finalIncident = service.findById(incident.getId());
        assertTrue(finalIncident.isPresent());
        assertEquals(IncidentState.FINALIZADA, finalIncident.get().getState());
    }

    @Test
    void shouldRejectFinalizationWithNullSolution() {
        Incident incident = createIncidentInState(IncidentState.EN_VALIDACION);

        assertThrows(IllegalArgumentException.class, () ->
                service.completeIncident(incident.getId(), null));
    }

    @Test
    void shouldRejectFinalizationWithEmptySolution() {
        Incident incident = createIncidentInState(IncidentState.EN_VALIDACION);

        assertThrows(IllegalArgumentException.class, () ->
                service.completeIncident(incident.getId(), ""));
    }

    @Test
    void shouldRejectFinalizationWithBlankSolution() {
        Incident incident = createIncidentInState(IncidentState.EN_VALIDACION);

        assertThrows(IllegalArgumentException.class, () ->
                service.completeIncident(incident.getId(), "   "));
    }

    @Test
    void shouldCompleteIncidentWithValidSolution() {
        Incident incident = createIncidentInState(IncidentState.EN_VALIDACION);

        Optional<Incident> result = service.completeIncident(incident.getId(), "Driver reinstalled successfully");

        assertTrue(result.isPresent());
        assertEquals(IncidentState.FINALIZADA, result.get().getState());
        assertEquals("Driver reinstalled successfully", result.get().getSolutionDescription());
    }

    @Test
    void shouldRecordClosingDateWhenCompleted() {
        Incident incident = createIncidentInState(IncidentState.EN_VALIDACION);

        Optional<Incident> result = service.completeIncident(incident.getId(), "Solution applied");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUpdatedAt());
    }

    @Test
    void shouldNotAllowCompleteFromRegistradaState() {
        Incident incident = service.createIncident("Title", "Description text enough", Impact.ALTO, Urgency.ALTA, "Software");

        assertThrows(IllegalStateException.class, () ->
                service.completeIncident(incident.getId(), "Solution"));
    }

    private Incident createIncidentInState(IncidentState state) {
        Incident incident = service.createIncident("Title", "Description text enough", Impact.ALTO, Urgency.MEDIA, "Software");
        IncidentState current = IncidentState.REGISTRADA;
        while (current != state) {
            IncidentState next = nextValidState(current);
            service.transitionState(incident.getId(), next);
            current = next;
        }
        return incident;
    }

    private IncidentState nextValidState(IncidentState current) {
        return switch (current) {
            case REGISTRADA -> IncidentState.LISTA;
            case LISTA -> IncidentState.EN_DESARROLLO;
            case EN_DESARROLLO -> IncidentState.EN_VALIDACION;
            case EN_VALIDACION -> IncidentState.FINALIZADA;
            case FINALIZADA -> throw new IllegalStateException("Cannot advance from FINALIZADA");
        };
    }
}
