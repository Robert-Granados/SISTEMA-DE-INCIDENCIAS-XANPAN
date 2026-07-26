package com.xanpan.incident.service;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;
import com.xanpan.incident.repository.InMemoryIncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentQueryTest {

    private InMemoryIncidentRepository repository;
    private IncidentService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryIncidentRepository();
        service = new IncidentService(repository, new PriorityCalculator(), new StateTransitionValidator());
    }

    @Test
    void shouldShowAllRegisteredIncidents() {
        service.createIncident("Title 1", "Description one with enough chars", Impact.ALTO, Urgency.ALTA, "Software");
        service.createIncident("Title 2", "Description two with enough chars", Impact.BAJO, Urgency.BAJA, "Hardware");

        List<Incident> all = service.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoIncidents() {
        List<Incident> all = service.findAll();

        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void shouldFindIncidentById() {
        Incident created = service.createIncident("Title", "Description text valid", Impact.MEDIO, Urgency.MEDIA, "Red");

        var found = service.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("Title", found.get().getTitle());
    }

    @Test
    void shouldReturnEmptyForNonexistentId() {
        var found = service.findById("nonexistent-id-12345");

        assertFalse(found.isPresent());
    }

    @Test
    void shouldReturnConsistentOrder() {
        Incident first = service.createIncident("First", "First description valid", Impact.ALTO, Urgency.ALTA, "Software");
        Incident second = service.createIncident("Second", "Second description valid", Impact.BAJO, Urgency.BAJA, "Hardware");

        List<Incident> firstCall = service.findAll();
        List<Incident> secondCall = service.findAll();

        assertEquals(firstCall.size(), secondCall.size());
        for (int i = 0; i < firstCall.size(); i++) {
            assertEquals(firstCall.get(i).getId(), secondCall.get(i).getId());
        }
    }

    @Test
    void shouldFilterByState() {
        Incident i1 = service.createIncident("Title 1", "Description one valid text", Impact.ALTO, Urgency.ALTA, "Software");
        Incident i2 = service.createIncident("Title 2", "Description two valid text", Impact.BAJO, Urgency.BAJA, "Hardware");
        service.transitionState(i2.getId(), IncidentState.LISTA);

        List<Incident> registradas = service.findByState(IncidentState.REGISTRADA);
        List<Incident> listas = service.findByState(IncidentState.LISTA);

        assertEquals(1, registradas.size());
        assertEquals(i1.getId(), registradas.get(0).getId());
        assertEquals(1, listas.size());
        assertEquals(i2.getId(), listas.get(0).getId());
    }

    @Test
    void shouldReturnEmptyWhenFilterByStateWithNoMatches() {
        service.createIncident("Title", "Description text valid", Impact.ALTO, Urgency.ALTA, "Software");

        List<Incident> finalizadas = service.findByState(IncidentState.FINALIZADA);

        assertNotNull(finalizadas);
        assertTrue(finalizadas.isEmpty());
    }

    @Test
    void shouldFilterByPriority() {
        service.createIncident("Critica", "Description for critical issue", Impact.ALTO, Urgency.ALTA, "Software");
        service.createIncident("Normal", "Description for normal issue", Impact.BAJO, Urgency.BAJA, "Hardware");

        List<Incident> criticas = service.findByPriority(Priority.CRITICA);
        List<Incident> normales = service.findByPriority(Priority.NORMAL);

        assertEquals(1, criticas.size());
        assertEquals("Critica", criticas.get(0).getTitle());
        assertEquals(1, normales.size());
        assertEquals("Normal", normales.get(0).getTitle());
    }

    @Test
    void shouldReturnEmptyWhenFilterByPriorityWithNoMatches() {
        service.createIncident("Normal", "Description text for normal", Impact.BAJO, Urgency.BAJA, "Hardware");

        List<Incident> criticas = service.findByPriority(Priority.CRITICA);

        assertNotNull(criticas);
        assertTrue(criticas.isEmpty());
    }

    @Test
    void shouldNotModifyIncidentsWhenFiltering() {
        Incident i1 = service.createIncident("Title 1", "Description one valid text", Impact.ALTO, Urgency.ALTA, "Software");
        Incident i2 = service.createIncident("Title 2", "Description two valid text", Impact.BAJO, Urgency.BAJA, "Hardware");

        service.findByState(IncidentState.REGISTRADA);
        service.findByPriority(Priority.ALTA);

        var all = service.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void shouldShowOpenIncidents() {
        Incident i1 = service.createIncident("Open", "Description for open issue", Impact.ALTO, Urgency.ALTA, "Software");
        Incident i2 = service.createIncident("Closed", "Description for closed issue", Impact.BAJO, Urgency.BAJA, "Hardware");
        moveToEndValidacion(i2.getId());
        service.completeIncident(i2.getId(), "Solution applied");

        List<Incident> open = service.findOpen();

        assertEquals(1, open.size());
        assertEquals("Open", open.get(0).getTitle());
    }

    @Test
    void shouldShowClosedIncidents() {
        Incident i1 = service.createIncident("Open", "Description for open issue", Impact.ALTO, Urgency.ALTA, "Software");
        Incident i2 = service.createIncident("Closed", "Description for closed issue", Impact.BAJO, Urgency.BAJA, "Hardware");
        moveToEndValidacion(i2.getId());
        service.completeIncident(i2.getId(), "Solution applied");

        List<Incident> closed = service.findClosed();

        assertEquals(1, closed.size());
        assertEquals("Closed", closed.get(0).getTitle());
    }

    private void moveToEndValidacion(String incidentId) {
        service.transitionState(incidentId, IncidentState.LISTA);
        service.transitionState(incidentId, IncidentState.EN_DESARROLLO);
        service.transitionState(incidentId, IncidentState.EN_VALIDACION);
    }
}
