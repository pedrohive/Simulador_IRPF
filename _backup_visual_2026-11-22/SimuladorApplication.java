package br.com.simulador.irpf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SimuladorApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(SimuladorApplication.class.getResource("simulador-view.fxml"));
        Scene scene = new Scene(loader.load(), 920, 640);
        scene.getStylesheets().add(SimuladorApplication.class.getResource("estilo.css").toExternalForm());
        stage.setTitle("Simulador IRPF - Cálculo de IRRF mensal");
        stage.setMinWidth(820);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }
}
