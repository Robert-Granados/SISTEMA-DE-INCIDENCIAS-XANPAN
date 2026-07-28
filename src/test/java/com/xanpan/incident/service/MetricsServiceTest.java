package com.xanpan.incident.service;

import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;
import com.xanpan.incident.repository.InMemoryIncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricsServiceTest {

    private InMemoryIncidentRepository repository;
    private IncidentService incidentService;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryIncidentRepository();
        incidentService = new IncidentService(repository, new PriorityCalculator(), new StateTransitionValidator());
        metricsService = new MetricsService(repository);
    }

    @Test
    void shouldCountTotalIncidents() {
        incidentService.createIncident("Title 1", "Description one valid text", Impact.ALTO, Urgency.ALTA, "Software");
        incidentService.createIncident("Title 2", "Description two valid text", Impact.BAJO, Urgency.BAJA, "Hardware");

        assertEquals(2, metricsService.totalIncidents());
    }

    @Test
    void shouldCountClosedIncidents() {
        Incident i1 = incidentService.createIncident("Open", "Description for open issue", Impact.ALTO, Urgency.ALTA, "Software");
        Incident i2 = incidentService.createIncident("Closed", "Description for closed issue", Impact.BAJO, Urgency.BAJA, "Hardware");
        moveToEndValidacion(i2.getId());
        incidentService.completeIncident(i2.getId(), "Solution applied");

        assertEquals(1, metricsService.closedIncidents());
    }

    @Test
    void shouldCountOpenIncidents() {
        Incident i1 = incidentService.createIncident("Open", "Description for open issue", Impact.ALTO, Urgency.ALTA, "Software");
        Incident i2 = incidentService.createIncident("Closed", "Description for closed issue", Impact.BAJO, Urgency.BAJA, "Hardware");
        moveToEndValidacion(i2.getId());
        incidentService.completeIncident(i2.getId(), "Solution applied");

        assertEquals(1, metricsService.openIncidents());
    }

    @Test
    void shouldReturnZeroThroughputWhenNoClosedIncidents() {
        assertEquals(0, metricsService.throughput());
    }

    @Test
    void shouldCalculateThroughputWithClosedIncidents() {
        Incident i1 = incidentService.createIncident("Open", "Description for open issue", Impact.ALTO, Urgency.ALTA, "Software");
        Incident i2 = incidentService.createIncident("Closed 1", "Description for closed issue", Impact.BAJO, Urgency.BAJA, "Hardware");
        Incident i3 = incidentService.createIncident("Closed 2", "Description for another closed", Impact.MEDIO, Urgency.MEDIA, "Red");
        moveToEndValidacion(i2.getId());
        moveToEndValidacion(i3.getId());
        incidentService.completeIncident(i2.getId(), "Solution 1");
        incidentService.completeIncident(i3.getId(), "Solution 2");

        assertEquals(2, metricsService.throughput());
    }

    @Test
    void shouldReturnZeroAverageLeadTimeWhenNoClosedIncidents() {
        assertEquals(0.0, metricsService.averageLeadTimeMinutes());
    }

    @Test
    void shouldCalculateCountByPriority() {
        incidentService.createIncident("Critica", "Description for critical issue", Impact.ALTO, Urgency.ALTA, "Software");
        incidentService.createIncident("Alta 1", "Description for high issue", Impact.ALTO, Urgency.MEDIA, "Software");
        incidentService.createIncident("Alta 2", "Description for another high", Impact.MEDIO, Urgency.ALTA, "Hardware");
        incidentService.createIncident("Normal", "Description for normal issue", Impact.BAJO, Urgency.BAJA, "Red");

        Map<Priority, Long> counts = metricsService.countByPriority();

        assertNotNull(counts);
        assertEquals(1L, counts.get(Priority.CRITICA));
        assertEquals(2L, counts.get(Priority.ALTA));
        assertEquals(1L, counts.get(Priority.NORMAL));
    }

    @Test
    void shouldShowZeroCountForPrioritiesWithNoIncidents() {
        Map<Priority, Long> counts = metricsService.countByPriority();

        assertNotNull(counts);
        assertEquals(0L, counts.get(Priority.CRITICA));
        assertEquals(0L, counts.get(Priority.ALTA));
        assertEquals(0L, counts.get(Priority.NORMAL));
    }

    @Test
    void shouldNotModifyCollectionWhenCountingByPriority() {
        incidentService.createIncident("Critica", "Description for critical issue", Impact.ALTO, Urgency.ALTA, "Software");

        metricsService.countByPriority();

        assertEquals(1, metricsService.totalIncidents());
    }
    @Test
    void shouldRecordClosureAndMeasureThroughputInsideRequestedPeriod() {
        MutableClock clock = new MutableClock(
                Instant.parse("2026-07-28T14:00:00Z"),
                ZoneOffset.UTC
        );
        incidentService = new IncidentService(
                repository,
                new PriorityCalculator(),
                new StateTransitionValidator(),
                clock
        );

        Incident first = incidentService.createIncident(
                "First closed",
                "Description for first closure",
                Impact.BAJO,
                Urgency.BAJA,
                "Software"
        );
        moveToEndValidacion(first.getId());
        clock.advance(Duration.ofMinutes(30));
        incidentService.completeIncident(first.getId(), "First solution");

        clock.advance(Duration.ofMinutes(30));
        Incident second = incidentService.createIncident(
                "Second closed",
                "Description for second closure",
                Impact.MEDIO,
                Urgency.MEDIA,
                "Hardware"
        );
        moveToEndValidacion(second.getId());
        clock.advance(Duration.ofHours(1));
        incidentService.completeIncident(second.getId(), "Second solution");

        assertEquals(LocalDateTime.of(2026, 7, 28, 14, 30), first.getClosedAt());
        assertEquals(1, metricsService.throughput(
                LocalDateTime.of(2026, 7, 28, 14, 0),
                LocalDateTime.of(2026, 7, 28, 15, 0)
        ));
        assertEquals(1, metricsService.throughput(
                LocalDateTime.of(2026, 7, 28, 15, 0),
                LocalDateTime.of(2026, 7, 28, 17, 0)
        ));
    }

    @Test
    void shouldUseClosureDateInsteadOfLaterUpdatesForAverageLeadTime() {
        MutableClock clock = new MutableClock(
                Instant.parse("2026-07-28T14:00:00Z"),
                ZoneOffset.UTC
        );
        incidentService = new IncidentService(
                repository,
                new PriorityCalculator(),
                new StateTransitionValidator(),
                clock
        );
        Incident incident = incidentService.createIncident(
                "Lead time",
                "Description for lead time",
                Impact.ALTO,
                Urgency.MEDIA,
                "Red"
        );
        moveToEndValidacion(incident.getId());
        clock.advance(Duration.ofMinutes(90));
        incidentService.completeIncident(incident.getId(), "Applied solution");
        clock.advance(Duration.ofMinutes(30));
        incident.setTitle("Updated after closure");

        assertEquals(90.0, metricsService.averageLeadTimeMinutes());
    }

    @Test
    void shouldRejectInvalidThroughputPeriods() {
        LocalDateTime instant = LocalDateTime.of(2026, 7, 28, 14, 0);

        assertThrows(IllegalArgumentException.class,
                () -> metricsService.throughput(instant, instant));
        assertThrows(IllegalArgumentException.class,
                () -> metricsService.throughput(null, instant));
    }

    private void moveToEndValidacion(String incidentId) {
        incidentService.transitionState(incidentId, IncidentState.LISTA);
        incidentService.transitionState(incidentId, IncidentState.EN_DESARROLLO);
        incidentService.transitionState(incidentId, IncidentState.EN_VALIDACION);
    }
    private static final class MutableClock extends Clock {
        private Instant currentInstant;
        private final ZoneId zone;

        private MutableClock(Instant currentInstant, ZoneId zone) {
            this.currentInstant = currentInstant;
            this.zone = zone;
        }

        private void advance(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId requestedZone) {
            return new MutableClock(currentInstant, requestedZone);
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }
    }
}
