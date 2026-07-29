package com.xanpan.incident.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentRestoreTest {

    @Test
    void restoresPersistedStateWithoutChangingTimestamps() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 1, 8, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 2, 9, 30);

        Incident incident = Incident.restore(
                "9f54c58d-3f4d-430b-9515-468019a8750d",
                "Servicio caido",
                "El servicio no responde",
                Impact.ALTO,
                Urgency.ALTA,
                Priority.CRITICA,
                IncidentState.EN_DESARROLLO,
                "Software",
                null,
                true,
                createdAt,
                updatedAt,
                null
        );

        assertEquals(IncidentState.EN_DESARROLLO, incident.getState());
        assertEquals(createdAt, incident.getCreatedAt());
        assertEquals(updatedAt, incident.getUpdatedAt());
        assertTrue(incident.isExpedited());
    }
}
