package com.xanpan.incident.service;

import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriorityCalculatorTest {

    private PriorityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriorityCalculator();
    }

    @Test
    void shouldReturnCriticaWhenHighImpactAndHighUrgency() {
        assertEquals(Priority.CRITICA, calculator.calculate(Impact.ALTO, Urgency.ALTA));
    }

    @Test
    void shouldReturnAltaWhenHighImpactAndMediumUrgency() {
        assertEquals(Priority.ALTA, calculator.calculate(Impact.ALTO, Urgency.MEDIA));
    }

    @Test
    void shouldReturnAltaWhenHighImpactAndLowUrgency() {
        assertEquals(Priority.ALTA, calculator.calculate(Impact.ALTO, Urgency.BAJA));
    }

    @Test
    void shouldReturnAltaWhenMediumImpactAndHighUrgency() {
        assertEquals(Priority.ALTA, calculator.calculate(Impact.MEDIO, Urgency.ALTA));
    }

    @Test
    void shouldReturnAltaWhenLowImpactAndHighUrgency() {
        assertEquals(Priority.ALTA, calculator.calculate(Impact.BAJO, Urgency.ALTA));
    }

    @Test
    void shouldReturnNormalWhenMediumImpactAndMediumUrgency() {
        assertEquals(Priority.NORMAL, calculator.calculate(Impact.MEDIO, Urgency.MEDIA));
    }

    @Test
    void shouldReturnNormalWhenLowImpactAndLowUrgency() {
        assertEquals(Priority.NORMAL, calculator.calculate(Impact.BAJO, Urgency.BAJA));
    }

    @Test
    void shouldReturnNormalWhenMediumImpactAndLowUrgency() {
        assertEquals(Priority.NORMAL, calculator.calculate(Impact.MEDIO, Urgency.BAJA));
    }

    @Test
    void shouldReturnNormalWhenLowImpactAndMediumUrgency() {
        assertEquals(Priority.NORMAL, calculator.calculate(Impact.BAJO, Urgency.MEDIA));
    }

    @Test
    void shouldThrowExceptionWhenImpactIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(null, Urgency.ALTA));
    }

    @Test
    void shouldThrowExceptionWhenUrgencyIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(Impact.ALTO, null));
    }

    @Test
    void shouldThrowExceptionWhenBothAreNull() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(null, null));
    }
}
