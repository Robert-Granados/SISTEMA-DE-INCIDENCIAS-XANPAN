package com.xanpan.incident.service;

import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;

public final class PriorityCalculator {

    public Priority calculate(Impact impact, Urgency urgency) {
        if (impact == null || urgency == null) {
            throw new IllegalArgumentException("Impact and urgency are required");
        }

        if (impact == Impact.ALTO && urgency == Urgency.ALTA) {
            return Priority.CRITICA;
        }
        if (impact == Impact.ALTO || urgency == Urgency.ALTA) {
            return Priority.ALTA;
        }
        return Priority.NORMAL;
    }
}
