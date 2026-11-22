package br.com.simulador.irpf;

import br.com.simulador.irpf.calculos.Simulacao;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class SimuladorController {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(PT_BR);
    private static final NumberFormat PERCENTUAL = NumberFormat.getPercentInstance(PT_BR);

    static {
        PERCENTUAL.setMinimumFractionDigits(1);
        PERCENTUAL.setMaximumFractionDigits(1);
    }

    @FXML private TextField campoNome;
    @FXML private TextField campoCpf;
    @FXML private TextField campoSalario;
    @FXML private Spinner<Integer> campoDependentes;
    @FXML private Label erro;

    @FXML private VBox painelResultado;
    @FXML private Label textoContribuinte;
    @FXML private Label resBruto;
    @FXML private Label resInss;
    @FXML private Label resDependentes;
    @FXML private Label resBase;
    @FXML private Label resAliquotaFaixa;
    @FXML private Label resIr;
    @FXML private Label resAliquotaEfetiva;
    @FXML private Label resLiquido;
    @FXML private HBox barraComposicao;
    @FXML private Region segLiquido;
    @FXML private Region segInss;
    @FXML private Region segIr;

    @FXML
    private void initialize() {
        campoDependentes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99, 0));
        campoDependentes.setEditable(true);
        // Confirma o valor digitado ao sair do campo, senão o Spinner ignora o texto
        campoDependentes.focusedProperty().addListener((obs, antes, agora) -> {
            if (!agora) {
                campoDependentes.getValueFactory().setValue(lerDependentes());
            }
        });
        painelResultado.setVisible(false);
        painelResultado.setManaged(false);
    }

    @FXML
    private void calcular() {
        try {
            Pessoa p = new Pessoa();
            p.setNome(campoNome.getText());
            p.setCpf(campoCpf.getText());
            p.setSalario(lerSalario());
            p.setDependentes(lerDependentes());

            exibirResultado(p);
            erro.setText("");
        } catch (IllegalArgumentException e) {
            erro.setText(e.getMessage());
            painelResultado.setVisible(false);
            painelResultado.setManaged(false);
        }
    }

    @FXML
    private void limpar() {
        campoNome.clear();
        campoCpf.clear();
        campoSalario.clear();
        campoDependentes.getValueFactory().setValue(0);
        erro.setText("");
        painelResultado.setVisible(false);
        painelResultado.setManaged(false);
        campoNome.requestFocus();
    }

    private double lerSalario() {
        String texto = campoSalario.getText() == null ? "" : campoSalario.getText().strip()
                .replace("R$", "").strip();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Informe o salário bruto.");
        }
        NumberFormat formato = NumberFormat.getNumberInstance(PT_BR);
        ParsePosition pos = new ParsePosition(0);
        Number valor = formato.parse(texto, pos);
        if (valor == null || pos.getIndex() != texto.length()) {
            throw new IllegalArgumentException("Salário inválido. Use o formato 4500,00.");
        }
        return valor.doubleValue();
    }

    private int lerDependentes() {
        String texto = campoDependentes.getEditor().getText();
        try {
            return Integer.parseInt(texto.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número de dependentes inválido.");
        }
    }

    private void exibirResultado(Pessoa p) {
        Simulacao s = Simulacao.calcular(p.getSalario(), p.getDependentes());

        textoContribuinte.setText(p.getNome() + "  ·  CPF " + formatarCpf(p.getCpf()));
        resBruto.setText(MOEDA.format(s.salario()));
        resInss.setText("- " + MOEDA.format(s.inss()));
        resDependentes.setText(s.dependentes() + " (dedução " + MOEDA.format(s.deducaoDependentes()) + ")");
        resBase.setText(MOEDA.format(s.base()));
        resAliquotaFaixa.setText(PERCENTUAL.format(s.aliquotaFaixa()));
        resIr.setText("- " + MOEDA.format(s.ir()));
        resAliquotaEfetiva.setText(PERCENTUAL.format(s.aliquotaEfetiva()));
        resLiquido.setText(MOEDA.format(s.liquido()));

        atualizarBarra(s.salario(), s.inss(), s.ir(), s.liquido());

        painelResultado.setManaged(true);
        painelResultado.setVisible(true);
    }

    /** Divide a largura da barra proporcionalmente entre líquido, INSS e IR. */
    private void atualizarBarra(double salario, double inss, double ir, double liquido) {
        double total = salario > 0 ? salario : 1;
        segLiquido.prefWidthProperty().bind(barraComposicao.widthProperty().multiply(liquido / total));
        segInss.prefWidthProperty().bind(barraComposicao.widthProperty().multiply(inss / total));
        segIr.prefWidthProperty().bind(barraComposicao.widthProperty().multiply(ir / total));
    }

    private static String formatarCpf(String cpf) {
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}
