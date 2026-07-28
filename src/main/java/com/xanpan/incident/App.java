package com.xanpan.incident;

import com.xanpan.incident.ui.IncidentFrame;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;

public final class App {

    private App() {
    }

    public static void main(String[] args) {
        boolean consoleMode = Arrays.asList(args).contains("--console");
        if (consoleMode || GraphicsEnvironment.isHeadless()) {
            HelpDeskDemo.run(System.out);
            return;
        }
        IncidentFrame.launch();
    }
}
