package com.xanpan.incidencias;

public final class App {

    private App() {
    }

    public static String nombreAplicacion() {
        return "Sistema de Incidencias Xanpan";
    }

    public static void main(String[] args) {
        System.out.println(nombreAplicacion());
    }
}
