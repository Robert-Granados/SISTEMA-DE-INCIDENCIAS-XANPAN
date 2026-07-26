package com.xanpan.incidencias;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    @Test
    void devuelveElNombreDeLaAplicacion() {
        assertEquals("Sistema de Incidencias Xanpan", App.nombreAplicacion());
    }
}
