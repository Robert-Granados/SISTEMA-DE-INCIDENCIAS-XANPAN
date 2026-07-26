package com.xanpan.incident.service;

import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.Urgency;
import com.xanpan.incident.repository.InMemoryIncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentRegistrationTest {

    private InMemoryIncidentRepository repository;
    private IncidentService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryIncidentRepository();
        service = new IncidentService(repository, new PriorityCalculator(), new StateTransitionValidator());
    }

    @Test
    void shouldRejectEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createIncident("", "Descripcion valida con mas de diez caracteres", Impact.ALTO, Urgency.ALTA, "Software"));
    }

    @Test
    void shouldRejectNullTitle() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createIncident(null, "Descripcion valida con mas de diez caracteres", Impact.ALTO, Urgency.ALTA, "Software"));
    }

    @Test
    void shouldRejectBlankTitle() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createIncident("   ", "Descripcion valida con mas de diez caracteres", Impact.ALTO, Urgency.ALTA, "Software"));
    }

    @Test
    void shouldRejectDescriptionWithLessThanTenCharacters() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createIncident("Titulo valido", "Corta", Impact.ALTO, Urgency.ALTA, "Software"));
    }

    @Test
    void shouldRejectNullDescription() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createIncident("Titulo valido", null, Impact.ALTO, Urgency.ALTA, "Software"));
    }

    @Test
    void shouldAcceptDescriptionWithExactlyTenCharacters() {
        Incident incident = service.createIncident("Titulo valido", "1234567890", Impact.ALTO, Urgency.ALTA, "Software");
        assertNotNull(incident);
        assertEquals("1234567890", incident.getDescription());
    }

    @Test
    void shouldAcceptImpactBajo() {
        Incident incident = service.createIncident("Titulo", "Descripcion valida con mas de diez caracteres", Impact.BAJO, Urgency.ALTA, "Software");
        assertEquals(Impact.BAJO, incident.getImpact());
    }

    @Test
    void shouldAcceptImpactMedio() {
        Incident incident = service.createIncident("Titulo", "Descripcion valida con mas de diez caracteres", Impact.MEDIO, Urgency.ALTA, "Software");
        assertEquals(Impact.MEDIO, incident.getImpact());
    }

    @Test
    void shouldAcceptImpactAlto() {
        Incident incident = service.createIncident("Titulo", "Descripcion valida con mas de diez caracteres", Impact.ALTO, Urgency.ALTA, "Software");
        assertEquals(Impact.ALTO, incident.getImpact());
    }

    @Test
    void shouldAcceptUrgencyBaja() {
        Incident incident = service.createIncident("Titulo", "Descripcion valida con mas de diez caracteres", Impact.ALTO, Urgency.BAJA, "Software");
        assertEquals(Urgency.BAJA, incident.getUrgency());
    }

    @Test
    void shouldAcceptUrgencyMedia() {
        Incident incident = service.createIncident("Titulo", "Descripcion valida con mas de diez caracteres", Impact.ALTO, Urgency.MEDIA, "Software");
        assertEquals(Urgency.MEDIA, incident.getUrgency());
    }

    @Test
    void shouldAcceptUrgencyAlta() {
        Incident incident = service.createIncident("Titulo", "Descripcion valida con mas de diez caracteres", Impact.ALTO, Urgency.ALTA, "Software");
        assertEquals(Urgency.ALTA, incident.getUrgency());
    }

    @Test
    void shouldGenerateUniqueId() {
        Incident first = service.createIncident("Titulo 1", "Descripcion valida con mas de diez caracteres", Impact.ALTO, Urgency.ALTA, "Software");
        Incident second = service.createIncident("Titulo 2", "Descripcion valida con mas de diez caracteres", Impact.ALTO, Urgency.ALTA, "Software");

        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertTrue(!first.getId().equals(second.getId()), "IDs should be unique");
    }

    @Test
    void shouldRecordCreationDate() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Incident incident = service.createIncident("Titulo", "Descripcion valida con mas de diez caracteres", Impact.ALTO, Urgency.ALTA, "Software");
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(incident.getCreatedAt());
        assertTrue(incident.getCreatedAt().isAfter(before) || incident.getCreatedAt().isEqual(before));
        assertTrue(incident.getCreatedAt().isBefore(after) || incident.getCreatedAt().isEqual(after));
    }
}
