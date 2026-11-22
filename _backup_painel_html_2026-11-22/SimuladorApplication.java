package br.com.simulador.irpf;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.net.URL;

/** Hosts the HTML dashboard in a WebView and wires the Java calculation bridge to it. */
public class SimuladorApplication extends Application {

    private static final String DASHBOARD = "/web/irpf-dashboard.html";

    // JSObject members are held weakly by WebKit, so the bridge needs a strong reference here
    private IrpfBridge bridge;

    @Override
    public void start(Stage stage) {
        WebView view = new WebView();
        view.setContextMenuEnabled(false);
        view.setPageFill(Color.web("#05070A"));

        WebEngine engine = view.getEngine();
        bridge = new IrpfBridge(engine);

        engine.getLoadWorker().stateProperty().addListener((obs, before, now) -> {
            if (now == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaBridge", bridge);
                engine.executeScript("if (window.onJavaBridgeReady) window.onJavaBridgeReady();");
            }
        });

        URL page = SimuladorApplication.class.getResource(DASHBOARD);
        if (page == null) {
            throw new IllegalStateException("Resource not found: " + DASHBOARD);
        }
        engine.load(page.toExternalForm());

        Scene scene = new Scene(view, 1180, 820, Color.web("#05070A"));
        stage.setTitle("Simulador IRPF - Cálculo de IRRF mensal");
        stage.setMinWidth(1040);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }
}
