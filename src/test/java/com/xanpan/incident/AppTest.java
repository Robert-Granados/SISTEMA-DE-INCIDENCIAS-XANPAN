package com.xanpan.incident;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @Test
    void shouldDemonstrateRegistrationFlowExpediteAndMetrics() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        HelpDeskDemo.run(new PrintStream(bytes, true, StandardCharsets.UTF_8));

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("prioridad=CRITICA"));
        assertTrue(output.contains("Transicion -> EN_DESARROLLO"));
        assertTrue(output.contains("estado=FINALIZADA"));
        assertTrue(output.contains("expedite=true"));
        assertTrue(output.contains("total=2"));
        assertTrue(output.contains("abiertas=1"));
        assertTrue(output.contains("finalizadas=1"));
    }

    @Test
    void shouldPreserveExplicitConsoleMode() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try {
            System.setOut(new PrintStream(bytes, true, StandardCharsets.UTF_8));
            App.main(new String[]{"--console"});
        } finally {
            System.setOut(originalOutput);
        }

        assertTrue(bytes.toString(StandardCharsets.UTF_8)
                .contains("HelpDesk Flow - demostracion tecnica"));
    }
}
