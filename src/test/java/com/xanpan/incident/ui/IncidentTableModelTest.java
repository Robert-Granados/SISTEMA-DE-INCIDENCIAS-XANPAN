package com.xanpan.incident.ui;

import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidentTableModelTest {

    @Test
    void shouldExposeIncidentValuesWithoutChangingTheDomainObject() {
        Incident incident = new Incident(
                "12345678-abcdef",
                "Network outage",
                "The main office has no connectivity",
                Impact.ALTO,
                Urgency.ALTA,
                Priority.CRITICA,
                "Network"
        );
        IncidentTableModel model = new IncidentTableModel();

        model.setIncidents(List.of(incident));

        assertEquals(1, model.getRowCount());
        assertEquals("12345678", model.getValueAt(0, 0));
        assertEquals("Network outage", model.getValueAt(0, 1));
        assertEquals("CRITICA", model.getValueAt(0, 3));
        assertEquals("REGISTRADA", model.getValueAt(0, 4));
        assertEquals(false, model.getValueAt(0, 5));
        assertSame(incident, model.getIncidentAt(0));
    }

    @Test
    void shouldRejectInvalidRows() {
        IncidentTableModel model = new IncidentTableModel();

        assertThrows(IllegalArgumentException.class, () -> model.getIncidentAt(-1));
        assertThrows(IllegalArgumentException.class, () -> model.getIncidentAt(0));
    }
}
