module br.com.simulador.irpf {
    requires javafx.controls;
    requires javafx.fxml;

    opens br.com.simulador.irpf to javafx.fxml;
    exports br.com.simulador.irpf;
}
