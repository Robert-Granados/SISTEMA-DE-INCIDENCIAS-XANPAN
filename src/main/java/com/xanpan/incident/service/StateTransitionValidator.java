package com.xanpan.incident.service;

import com.xanpan.incident.model.IncidentState;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class StateTransitionValidator {

    private final Map<IncidentState, Set<IncidentState>> validTransitions;

    public StateTransitionValidator() {
        validTransitions = new EnumMap<>(IncidentState.class);
        validTransitions.put(IncidentState.REGISTRADA, EnumSet.of(IncidentState.LISTA));
        validTransitions.put(IncidentState.LISTA, EnumSet.of(IncidentState.EN_DESARROLLO));
        validTransitions.put(IncidentState.EN_DESARROLLO, EnumSet.of(IncidentState.EN_VALIDACION));
        validTransitions.put(IncidentState.EN_VALIDACION, EnumSet.of(IncidentState.FINALIZADA));
        validTransitions.put(IncidentState.FINALIZADA, EnumSet.noneOf(IncidentState.class));
    }

    public boolean isValidTransition(IncidentState from, IncidentState to) {
        Set<IncidentState> allowed = validTransitions.get(from);
        return allowed != null && allowed.contains(to);
    }
}
