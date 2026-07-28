package com.xanpan.incident.service;

import com.xanpan.incident.model.IncidentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateTransitionValidatorTest {

    private StateTransitionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StateTransitionValidator();
    }

    @Test
    void shouldAllowRegistradaToLista() {
        assertTrue(validator.isValidTransition(IncidentState.REGISTRADA, IncidentState.LISTA));
    }

    @Test
    void shouldAllowListaToEnDesarrollo() {
        assertTrue(validator.isValidTransition(IncidentState.LISTA, IncidentState.EN_DESARROLLO));
    }

    @Test
    void shouldAllowEnDesarrolloToEnValidacion() {
        assertTrue(validator.isValidTransition(IncidentState.EN_DESARROLLO, IncidentState.EN_VALIDACION));
    }

    @Test
    void shouldAllowEnValidacionToFinalizada() {
        assertTrue(validator.isValidTransition(IncidentState.EN_VALIDACION, IncidentState.FINALIZADA));
    }

    @Test
    void shouldNotAllowRegistradaToFinalizada() {
        assertFalse(validator.isValidTransition(IncidentState.REGISTRADA, IncidentState.FINALIZADA));
    }

    @Test
    void shouldNotAllowFinalizadaToEnDesarrollo() {
        assertFalse(validator.isValidTransition(IncidentState.FINALIZADA, IncidentState.EN_DESARROLLO));
    }

    @Test
    void shouldNotAllowRegistradaToEnDesarrollo() {
        assertFalse(validator.isValidTransition(IncidentState.REGISTRADA, IncidentState.EN_DESARROLLO));
    }

    @Test
    void shouldNotAllowListaToFinalizada() {
        assertFalse(validator.isValidTransition(IncidentState.LISTA, IncidentState.FINALIZADA));
    }

    @Test
    void shouldNotAllowBackwardTransitionEnDesarrolloToLista() {
        assertFalse(validator.isValidTransition(IncidentState.EN_DESARROLLO, IncidentState.LISTA));
    }

    @Test
    void shouldNotAllowBackwardTransitionFinalizadaToRegistrada() {
        assertFalse(validator.isValidTransition(IncidentState.FINALIZADA, IncidentState.REGISTRADA));
    }

    @Test
    void shouldNotAllowEnDesarrolloToRegistrada() {
        assertFalse(validator.isValidTransition(IncidentState.EN_DESARROLLO, IncidentState.REGISTRADA));
    }

    @Test
    void shouldNotAllowListaToEnValidacion() {
        assertFalse(validator.isValidTransition(IncidentState.LISTA, IncidentState.EN_VALIDACION));
    }

    @Test
    void shouldNotAllowSameStateRegistradaToRegistrada() {
        assertFalse(validator.isValidTransition(IncidentState.REGISTRADA, IncidentState.REGISTRADA));
    }

    @Test
    void shouldNotAllowSameStateEnDesarrolloToEnDesarrollo() {
        assertFalse(validator.isValidTransition(IncidentState.EN_DESARROLLO, IncidentState.EN_DESARROLLO));
    }

    @Test
    void shouldNotAllowSameStateFinalizadaToFinalizada() {
        assertFalse(validator.isValidTransition(IncidentState.FINALIZADA, IncidentState.FINALIZADA));
    }
}
