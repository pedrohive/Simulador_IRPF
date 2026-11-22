package br.com.simulador.irpf;

import javafx.application.Application;

import java.util.Arrays;

/**
 * Ponto de entrada único: abre a interface gráfica por padrão
 * ou o modo terminal com o argumento --console.
 */
public class Launcher {
    public static void main(String[] args) {
        if (Arrays.asList(args).contains("--console")) {
            ConsoleApp.executar();
        } else {
            Application.launch(SimuladorApplication.class, args);
        }
    }
}
