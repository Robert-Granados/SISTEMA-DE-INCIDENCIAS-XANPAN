package com.xanpan.incident.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class IncidentTest {

    @Test
    void shouldCreateIncidentWithDefaultStateRegistrada() {
        Incident incident = new Incident("1", "Title", "Description", Impact.ALTO, Urgency.ALTA, Priority.CRITICA, "Software");

        assertEquals(IncidentState.REGISTRADA, incident.getState());
    }

    @Test
    void shouldUpdateStateAndTimestamp() {
        Incident incident = new Incident("1", "Title", "Description", Impact.ALTO, Urgency.ALTA, Priority.CRITICA, "Software");
        LocalDateTime beforeUpdate = incident.getUpdatedAt();

        incident.setState(IncidentState.LISTA);

        assertEquals(IncidentState.LISTA, incident.getState());
        assertNotNull(incident.getUpdatedAt());
    }

    @Test
    void shouldSetSolutionDescription() {
        Incident incident = new Incident("1", "Title", "Description", Impact.ALTO, Urgency.ALTA, Priority.CRITICA, "Software");

        incident.setSolutionDescription("Reinstall driver");

        assertEquals("Reinstall driver", incident.getSolutionDescription());
    }
}
