package com.xanpan.incident.functional;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;
import com.xanpan.incident.repository.InMemoryIncidentRepository;
import com.xanpan.incident.service.ExpediteService;
import com.xanpan.incident.service.IncidentService;
import com.xanpan.incident.service.MetricsService;
import com.xanpan.incident.service.PriorityCalculator;
import com.xanpan.incident.service.StateTransitionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("functional")
class IncidentWorkflowFunctionalTest {

    private IncidentService incidentService;
    private ExpediteService expediteService;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        InMemoryIncidentRepository repository = new InMemoryIncidentRepository();
        incidentService = new IncidentService(
                repository,
                new PriorityCalculator(),
                new StateTransitionValidator()
        );
        expediteService = new ExpediteService(repository);
        metricsService = new MetricsService(repository);
    }

    @Test
    void completesFullWorkflowOnlyAfterRegisteringSolution() {
        Incident incident = incidentService.createIncident(
                "Servicio de pagos caido",
                "Los pagos no pueden ser procesados.",
                Impact.ALTO,
                Urgency.ALTA,
                "Software"
        );
        expediteService.markExpedited(incident.getId());

        assertTrue(incidentService.transitionState(incident.getId(), IncidentState.LISTA).isPresent());
        assertTrue(incidentService.transitionState(incident.getId(), IncidentState.EN_DESARROLLO).isPresent());
        assertTrue(incidentService.transitionState(incident.getId(), IncidentState.EN_VALIDACION).isPresent());
        assertFalse(incidentService.transitionState(incident.getId(), IncidentState.FINALIZADA).isPresent());

        assertTrue(incidentService.completeIncident(
                incident.getId(),
                "Se restauro la conexion con el proveedor de pagos."
        ).isPresent());
        assertEquals(IncidentState.FINALIZADA, incident.getState());
        assertTrue(incident.isExpedited());
    }

    @Test
    void registersFiltersAndMetricsWhileProtectingSingleActiveExpedite() {
        Incident first = createCriticalIncident("Primera incidencia");
        Incident second = createCriticalIncident("Segunda incidencia");
        expediteService.markExpedited(first.getId());
        expediteService.markExpedited(second.getId());

        moveToDevelopment(first);
        assertTrue(incidentService.transitionState(second.getId(), IncidentState.LISTA).isPresent());
        assertFalse(incidentService.transitionState(second.getId(), IncidentState.EN_DESARROLLO).isPresent());

        assertEquals(2, metricsService.totalIncidents());
        assertEquals(2, metricsService.openIncidents());
        assertEquals(0, metricsService.closedIncidents());
        assertEquals(2L, metricsService.countByPriority().get(Priority.CRITICA));
        assertEquals(1, incidentService.findByState(IncidentState.EN_DESARROLLO).size());

        assertTrue(incidentService.transitionState(first.getId(), IncidentState.EN_VALIDACION).isPresent());
        assertTrue(incidentService.completeIncident(first.getId(), "Servicio restaurado.").isPresent());
        assertTrue(incidentService.transitionState(second.getId(), IncidentState.EN_DESARROLLO).isPresent());
    }

    private Incident createCriticalIncident(String title) {
        return incidentService.createIncident(
                title,
                "Descripcion valida para la incidencia.",
                Impact.ALTO,
                Urgency.ALTA,
                "Software"
        );
    }

    private void moveToDevelopment(Incident incident) {
        assertTrue(incidentService.transitionState(incident.getId(), IncidentState.LISTA).isPresent());
        assertTrue(incidentService.transitionState(incident.getId(), IncidentState.EN_DESARROLLO).isPresent());
    }
}
