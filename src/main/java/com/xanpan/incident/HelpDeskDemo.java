package com.xanpan.incident;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Urgency;
import com.xanpan.incident.repository.InMemoryIncidentRepository;
import com.xanpan.incident.repository.IncidentRepository;
import com.xanpan.incident.service.ExpediteService;
import com.xanpan.incident.service.IncidentService;
import com.xanpan.incident.service.MetricsService;
import com.xanpan.incident.service.PriorityCalculator;
import com.xanpan.incident.service.StateTransitionValidator;

import java.io.PrintStream;

public final class HelpDeskDemo {

    private HelpDeskDemo() {
    }

    public static void run(PrintStream output) {
        IncidentRepository repository = new InMemoryIncidentRepository();
        IncidentService incidentService = new IncidentService(
                repository,
                new PriorityCalculator(),
                new StateTransitionValidator()
        );
        ExpediteService expediteService = new ExpediteService(repository);
        MetricsService metricsService = new MetricsService(repository);

        output.println("=== HelpDesk Flow - demostracion tecnica ===");

        Incident critical = incidentService.createIncident(
                "Servicio de pagos no disponible",
                "Los usuarios no pueden completar sus pagos.",
                Impact.ALTO,
                Urgency.ALTA,
                "Software"
        );
        output.printf(
                "Registrada %s | prioridad=%s | estado=%s%n",
                critical.getId(),
                critical.getPriority(),
                critical.getState()
        );

        expediteService.markExpedited(critical.getId());
        transition(incidentService, critical, IncidentState.LISTA, output);
        transition(incidentService, critical, IncidentState.EN_DESARROLLO, output);
        transition(incidentService, critical, IncidentState.EN_VALIDACION, output);
        incidentService.completeIncident(
                critical.getId(),
                "Se restauro la conexion con el proveedor de pagos."
        );
        output.printf(
                "Finalizada | estado=%s | expedite=%s | solucion=%s%n",
                critical.getState(),
                critical.isExpedited(),
                critical.getSolutionDescription()
        );

        Incident normal = incidentService.createIncident(
                "Actualizar controlador",
                "El monitor secundario pierde la senal.",
                Impact.BAJO,
                Urgency.MEDIA,
                "Hardware"
        );
        output.printf(
                "Registrada %s | prioridad=%s | estado=%s%n",
                normal.getId(),
                normal.getPriority(),
                normal.getState()
        );

        output.printf(
                "Metricas | total=%d | abiertas=%d | finalizadas=%d | throughput=%d%n",
                metricsService.totalIncidents(),
                metricsService.openIncidents(),
                metricsService.closedIncidents(),
                metricsService.throughput()
        );
    }

    private static void transition(
            IncidentService service,
            Incident incident,
            IncidentState target,
            PrintStream output
    ) {
        service.transitionState(incident.getId(), target)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo cambiar la incidencia a " + target
                ));
        output.println("Transicion -> " + target);
    }
}
