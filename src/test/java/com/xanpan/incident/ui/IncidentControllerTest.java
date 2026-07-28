package com.xanpan.incident.ui;

import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentControllerTest {

    private IncidentController controller;

    @BeforeEach
    void setUp() {
        controller = IncidentController.inMemory();
    }

    @Test
    void shouldRegisterIncidentAndExposeUpdatedMetrics() {
        Incident incident = controller.registerIncident(
                "Payments unavailable",
                "Users cannot complete their payments",
                Impact.ALTO,
                Urgency.ALTA,
                "Software"
        );

        assertEquals(Priority.CRITICA, incident.getPriority());
        assertEquals(IncidentState.REGISTRADA, incident.getState());
        assertEquals(1, controller.metrics().total());
        assertEquals(1, controller.metrics().open());
        assertEquals(1L, controller.metrics().byPriority().get(Priority.CRITICA));
    }

    @Test
    void shouldCoordinateFullWorkflowAndSolution() {
        Incident incident = registerNormal("Printer unavailable", "Hardware");

        assertEquals(IncidentState.LISTA,
                controller.advanceIncident(incident.getId()).getState());
        assertEquals(IncidentState.EN_DESARROLLO,
                controller.advanceIncident(incident.getId()).getState());
        assertEquals(IncidentState.EN_VALIDACION,
                controller.advanceIncident(incident.getId()).getState());
        assertThrows(IllegalStateException.class,
                () -> controller.advanceIncident(incident.getId()));

        Incident completed = controller.completeIncident(
                incident.getId(),
                "Replaced the damaged cable"
        );

        assertEquals(IncidentState.FINALIZADA, completed.getState());
        assertEquals("Replaced the damaged cable", completed.getSolutionDescription());
        assertEquals(1, controller.metrics().closed());
        assertEquals(0, controller.metrics().open());
    }

    @Test
    void shouldFilterByTextScopeStateAndPriority() {
        Incident critical = controller.registerIncident(
                "Network outage",
                "The main office has no connectivity",
                Impact.ALTO,
                Urgency.ALTA,
                "Network"
        );
        Incident closed = registerNormal("Replace keyboard", "Hardware");
        controller.advanceIncident(closed.getId());
        controller.advanceIncident(closed.getId());
        controller.advanceIncident(closed.getId());
        controller.completeIncident(closed.getId(), "Keyboard replaced");

        List<Incident> byText = controller.listIncidents(
                "network",
                IncidentController.Scope.ALL,
                null,
                null
        );
        List<Incident> closedOnly = controller.listIncidents(
                "",
                IncidentController.Scope.CLOSED,
                IncidentState.FINALIZADA,
                Priority.NORMAL
        );

        assertEquals(List.of(critical), byText);
        assertEquals(List.of(closed), closedOnly);
    }

    @Test
    void shouldApplyExpediteRuleThroughPresentationController() {
        Incident critical = controller.registerIncident(
                "Critical outage",
                "All payment services are unavailable",
                Impact.ALTO,
                Urgency.ALTA,
                "Software"
        );
        Incident normal = registerNormal("Update mouse driver", "Hardware");

        assertTrue(controller.markExpedited(critical.getId()).isExpedited());
        assertThrows(IllegalArgumentException.class,
                () -> controller.markExpedited(normal.getId()));
    }

    @Test
    void shouldRejectIncompleteRegistrationFromTheInterface() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerIncident(
                        "Missing category",
                        "Description with enough characters",
                        Impact.MEDIO,
                        Urgency.MEDIA,
                        " "
                )
        );
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerIncident(
                        "Missing impact",
                        "Description with enough characters",
                        null,
                        Urgency.MEDIA,
                        "Software"
                )
        );
    }

    private Incident registerNormal(String title, String category) {
        return controller.registerIncident(
                title,
                "Description with enough characters",
                Impact.BAJO,
                Urgency.BAJA,
                category
        );
    }
}
