package br.com.simulador.irpf;

import br.com.simulador.irpf.calculos.Arredondamento;
import br.com.simulador.irpf.calculos.CalculadoraInss;
import br.com.simulador.irpf.calculos.CalculadoraIrpf;
import br.com.simulador.irpf.calculos.DecimoTerceiro;
import br.com.simulador.irpf.calculos.Simulacao;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.util.Duration;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.Locale;

public class SimuladorController {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(PT_BR);
    private static final NumberFormat NUMERO = NumberFormat.getNumberInstance(PT_BR);
    private static final String CLASSE_ERRO = "campo-erro";
    private static final String CLASSE_ATIVO = "menu-ativo";
    private static final String CLASSE_FAIXA_ATUAL = "faixa-atual";

    static {
        NUMERO.setMinimumFractionDigits(2);
        NUMERO.setMaximumFractionDigits(2);
        // Os valores já chegam cortados em 2 casas; DOWN garante que nunca haja arredondamento
        NUMERO.setRoundingMode(RoundingMode.DOWN);
        MOEDA.setRoundingMode(RoundingMode.DOWN);
    }

    // Todos os números da tela têm 2 casas decimais, cortadas e não arredondadas (como na Receita)

    /** R$ com 2 casas, sem arredondar. Ex.: 4.738,019 -> R$ 4.738,01. */
    private static String moeda(double valor) {
        return MOEDA.format(Arredondamento.duasCasas(valor));
    }

    /** Número com 2 casas, sem arredondar. Ex.: 86,159 -> 86,15. */
    private static String numero(double valor) {
        return NUMERO.format(Arredondamento.duasCasas(valor));
    }

    /** Fração como porcentagem com 2 casas, sem arredondar. Ex.: 0,161767 -> 16,17%. */
    private static String percentual(double fracao) {
        return NUMERO.format(Arredondamento.percentualDuasCasas(fracao)) + "%";
    }

    // Navegação
    @FXML private Label menuSimular;
    @FXML private Label menuTabela;
    @FXML private Label menuComo;
    @FXML private ScrollPane paginaSimular;
    @FXML private ScrollPane paginaTabela;
    @FXML private ScrollPane paginaComo;
    @FXML private Label textoDica;

    // Formulário
    @FXML private TextField campoNome;
    @FXML private TextField campoCpf;
    @FXML private TextField campoSalario;
    @FXML private Spinner<Integer> campoDependentes;
    @FXML private CheckBox campoDescontarInss;
    @FXML private RadioButton opcaoMensal;
    @FXML private RadioButton opcaoDecimoTerceiro;
    @FXML private HBox grupoMeses;
    @FXML private Spinner<Integer> campoMeses;
    @FXML private Label erro;

    // Resultado
    @FXML private Pane painelVazio;
    @FXML private Pane painelResultado;
    @FXML private Label textoContribuinte;
    @FXML private Label resLiquido;
    @FXML private Label resPercLiquido;
    @FXML private Label resCadaCem;
    @FXML private Label legLiquido;
    @FXML private Label legInss;
    @FXML private Label legIr;
    @FXML private Arc arcLiquido;
    @FXML private Arc arcInss;
    @FXML private Arc arcIr;
    @FXML private Label kpiInss;
    @FXML private Label kpiInssPerc;
    @FXML private Label kpiIr;
    @FXML private Label kpiIrPerc;
    @FXML private Label kpiEfetiva;
    @FXML private Label kpiFaixa;
    @FXML private Label resBruto;
    @FXML private Label resInss;
    @FXML private Label resDependentes;
    @FXML private Label resDeducao;
    @FXML private Label resBase;
    @FXML private Label resAliquotaFaixa;
    @FXML private Label resIr;
    @FXML private Label resAliquotaEfetiva;
    @FXML private Label resLiquidoTabela;
    @FXML private Label itemBruto;
    @FXML private Label itemLiquido;
    @FXML private HBox linhaParcela1;
    @FXML private HBox linhaParcela2;
    @FXML private Label resParcela1;
    @FXML private Label resParcela2;

    // Página de tabelas
    @FXML private Label avisoTabela;
    @FXML private VBox tabelaInss;
    @FXML private VBox tabelaIr;
    @FXML private Label regrasExtras;

    // Página "Como funciona"
    @FXML private Label passo1;
    @FXML private Label passo2;
    @FXML private Label passo3;
    @FXML private Label passo4;

    /** Animação da rosca e do valor líquido: vai de 0 a 1. */
    private final DoubleProperty progresso = new SimpleDoubleProperty(0);
    private Timeline animacao;
    private Simulacao ultima;

    @FXML
    private void initialize() {
        configurarCampos();
        montarTabelas();
        preencherTextos();

        progresso.addListener((obs, antes, agora) -> desenhar(agora.doubleValue()));

        esconderErro();
        mostrarResultado(false);
        mostrarPagina(paginaSimular, menuSimular);
    }

    // =====================================================================
    // Navegação
    // =====================================================================

    @FXML private void irSimular() { mostrarPagina(paginaSimular, menuSimular); }
    @FXML private void irTabela()  { mostrarPagina(paginaTabela, menuTabela); }
    @FXML private void irComo()    { mostrarPagina(paginaComo, menuComo); }

    private void mostrarPagina(ScrollPane pagina, Label menu) {
        for (ScrollPane p : List.of(paginaSimular, paginaTabela, paginaComo)) {
            p.setVisible(p == pagina);
        }
        for (Label m : List.of(menuSimular, menuTabela, menuComo)) {
            m.getStyleClass().remove(CLASSE_ATIVO);
        }
        menu.getStyleClass().add(CLASSE_ATIVO);
        pagina.setVvalue(0);

        FadeTransition fade = new FadeTransition(Duration.millis(220), pagina);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    // =====================================================================
    // Formulário
    // =====================================================================

    private void configurarCampos() {
        campoDependentes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99, 0));
        campoDependentes.setEditable(true);
        // Confirma o valor digitado ao sair do campo, senão o Spinner ignora o texto
        campoDependentes.focusedProperty().addListener((obs, antes, agora) -> {
            if (!agora) {
                try {
                    campoDependentes.getValueFactory().setValue(lerDependentes());
                } catch (IllegalArgumentException e) {
                    campoDependentes.getValueFactory().setValue(0);
                }
            }
        });

        // CPF: aceita só dígitos (máx. 11) e aplica a máscara 000.000.000-00 enquanto digita
        campoCpf.setTextFormatter(new TextFormatter<String>(mudanca -> {
            if (!mudanca.isContentChange()) {
                return mudanca;
            }
            String digitos = mudanca.getControlNewText().replaceAll("\\D", "");
            if (digitos.length() > 11) {
                return null;
            }
            String formatado = mascaraCpf(digitos);
            mudanca.setRange(0, mudanca.getControlText().length());
            mudanca.setText(formatado);
            mudanca.setCaretPosition(formatado.length());
            mudanca.setAnchor(formatado.length());
            return mudanca;
        }));

        // Salário: ao sair do campo, formata como 7.500,00
        campoSalario.focusedProperty().addListener((obs, antes, agora) -> {
            if (!agora && !campoSalario.getText().isBlank()) {
                try {
                    campoSalario.setText(numero(lerSalario()));
                } catch (IllegalArgumentException ignorado) {
                    // deixa como está; o erro aparece ao calcular
                }
            }
        });

        // Ao digitar de novo num campo com erro, tira o destaque vermelho
        campoNome.textProperty().addListener((o, a, n) -> desmarcar(campoNome));
        campoCpf.textProperty().addListener((o, a, n) -> desmarcar(campoCpf));
        campoSalario.textProperty().addListener((o, a, n) -> desmarcar(campoSalario));
        campoDependentes.getEditor().textProperty().addListener((o, a, n) -> desmarcar(campoDependentes));

        // Marcar/desmarcar o INSS refaz a conta na hora, se já houver um resultado na tela
        campoDescontarInss.selectedProperty().addListener((o, a, n) -> {
            if (ultima != null) calcular();
        });

        // 13º: meses trabalhados (1 a 12) só aparecem quando o 13º está escolhido
        campoMeses.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, DecimoTerceiro.MESES_NO_ANO, DecimoTerceiro.MESES_NO_ANO));
        grupoMeses.visibleProperty().bind(opcaoDecimoTerceiro.selectedProperty());
        grupoMeses.managedProperty().bind(opcaoDecimoTerceiro.selectedProperty());
        opcaoDecimoTerceiro.selectedProperty().addListener((o, a, n) -> {
            atualizarTipoCalculo();
            if (ultima != null) calcular();
        });
        campoMeses.valueProperty().addListener((o, a, n) -> {
            if (ultima != null) calcular();
        });
        atualizarTipoCalculo();
    }

    /** O 13º é direito de quem é CLT: nele o INSS é sempre descontado, então a opção fica travada. */
    private void atualizarTipoCalculo() {
        boolean decimo = opcaoDecimoTerceiro.isSelected();
        if (decimo) {
            campoDescontarInss.setSelected(true);
        }
        campoDescontarInss.setDisable(decimo);
    }

    private static String mascaraCpf(String d) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            if (i == 3 || i == 6) sb.append('.');
            if (i == 9) sb.append('-');
            sb.append(d.charAt(i));
        }
        return sb.toString();
    }

    @FXML
    private void calcular() {
        limparMarcacoes();
        Pessoa p = new Pessoa();
        try {
            validar(campoNome, () -> p.setNome(campoNome.getText()));
            validar(campoCpf, () -> p.setCpf(campoCpf.getText()));
            validar(campoSalario, () -> p.setSalario(lerSalario()));
            validar(campoDependentes, () -> p.setDependentes(lerDependentes()));

            exibirResultado(p);
            esconderErro();
        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
            ultima = null;
            mostrarResultado(false);
            destacarFaixas(null);
        }
    }

    @FXML
    private void limpar() {
        campoNome.clear();
        campoCpf.clear();
        campoSalario.clear();
        campoDependentes.getValueFactory().setValue(0);
        ultima = null; // antes de mexer nas opções, para não recalcular
        opcaoMensal.setSelected(true);
        campoMeses.getValueFactory().setValue(DecimoTerceiro.MESES_NO_ANO);
        campoDescontarInss.setSelected(true);
        limparMarcacoes();
        esconderErro();
        mostrarResultado(false);
        destacarFaixas(null);
        campoNome.requestFocus();
    }

    /** Executa a validação e, se falhar, marca o campo em vermelho e leva o foco até ele. */
    private void validar(Node campo, Runnable acao) {
        try {
            acao.run();
        } catch (IllegalArgumentException e) {
            if (!campo.getStyleClass().contains(CLASSE_ERRO)) {
                campo.getStyleClass().add(CLASSE_ERRO);
            }
            campo.requestFocus();
            throw e;
        }
    }

    private void desmarcar(Node campo) { campo.getStyleClass().remove(CLASSE_ERRO); }

    private void limparMarcacoes() {
        desmarcar(campoNome);
        desmarcar(campoCpf);
        desmarcar(campoSalario);
        desmarcar(campoDependentes);
    }

    private void mostrarErro(String mensagem) {
        erro.setText("⚠  " + mensagem);
        erro.setVisible(true);
    }

    private void esconderErro() {
        erro.setText("");
        erro.setVisible(false);
    }

    private double lerSalario() {
        String texto = campoSalario.getText() == null ? "" : campoSalario.getText().strip()
                .replace("R$", "").strip();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Informe o salário bruto.");
        }
        ParsePosition pos = new ParsePosition(0);
        Number valor = NumberFormat.getNumberInstance(PT_BR).parse(texto, pos);
        if (valor == null || pos.getIndex() != texto.length()) {
            throw new IllegalArgumentException("Salário inválido. Use o formato 4.500,00.");
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

    // =====================================================================
    // Resultado
    // =====================================================================

    private void exibirResultado(Pessoa p) {
        boolean decimo = opcaoDecimoTerceiro.isSelected();
        DecimoTerceiro d = decimo
                ? DecimoTerceiro.calcular(p.getSalario(), campoMeses.getValue(), p.getDependentes())
                : null;
        Simulacao s = decimo
                ? d.calculo()
                : Simulacao.calcular(p.getSalario(), p.getDependentes(), campoDescontarInss.isSelected());
        ultima = s;
        double total = total(s);

        textoContribuinte.setText(p.getNome() + "  ·  CPF " + mascaraCpf(p.getCpf()));
        resPercLiquido.setText(percentual(s.liquido() / total) + " do bruto");
        resCadaCem.setText("A cada R$ 100,00 do bruto, R$ "
                + numero(100 * s.liquido() / total) + " ficam com você.");

        legLiquido.setText("●  Líquido   " + percentual(s.liquido() / total));
        legInss.setText("●  INSS   " + percentual(s.inss() / total));
        legIr.setText("●  Imposto de Renda   " + percentual(s.ir() / total));

        kpiInss.setText(moeda(s.inss()));
        kpiInssPerc.setText(s.inssDescontado() ? percentual(s.inss() / total) + " do bruto" : "não descontado");
        kpiIr.setText(moeda(s.ir()));
        kpiIrPerc.setText(s.ir() == 0 ? "isento" : percentual(s.ir() / total) + " do bruto");
        kpiEfetiva.setText(percentual(s.aliquotaEfetiva()));
        kpiFaixa.setText("faixa de " + percentual(s.aliquotaFaixa()));

        resBruto.setText(moeda(s.salario()));
        resInss.setText(s.inssDescontado() ? "- " + moeda(s.inss()) : "não descontado");
        resDependentes.setText(s.dependentes() + " (dedução " + moeda(s.deducaoDependentes()) + ")");
        String deducoesLegais = s.inssDescontado() ? "INSS + dependentes  " : "Dependentes  ";
        resDeducao.setText((s.descontoSimplificado() ? "Desconto simplificado  " : deducoesLegais)
                + "- " + moeda(s.deducaoUtilizada()));
        resBase.setText(moeda(s.base()));
        resAliquotaFaixa.setText(percentual(s.aliquotaFaixa()));
        resIr.setText("- " + moeda(s.ir()));
        resAliquotaEfetiva.setText(percentual(s.aliquotaEfetiva()));
        resLiquidoTabela.setText(moeda(s.liquido()));

        // Ajustes de texto quando o cálculo é do 13º
        textoContribuinte.setText(p.getNome() + "  ·  CPF " + mascaraCpf(p.getCpf())
                + (decimo ? "  ·  13º salário" : ""));
        itemBruto.setText(decimo
                ? "13º bruto (" + d.mesesTrabalhados() + "/12 de " + moeda(d.salarioMensal()) + ")"
                : "Salário bruto");
        itemLiquido.setText(decimo ? "13º líquido" : "Salário líquido");
        if (decimo) {
            // 13º não tem desconto simplificado: só INSS + dependentes
            resDeducao.setText("Só INSS + dependentes  - "
                    + moeda(s.deducaoUtilizada()));
            resParcela1.setText(moeda(d.primeiraParcela()));
            resParcela2.setText(moeda(d.segundaParcela()));
        }
        for (HBox linha : List.of(linhaParcela1, linhaParcela2)) {
            linha.setVisible(decimo);
            linha.setManaged(decimo);
        }

        destacarFaixas(s);
        mostrarResultado(true);
        animarEntrada();
    }

    private static double total(Simulacao s) { return s.salario() > 0 ? s.salario() : 1; }

    private void mostrarResultado(boolean mostrar) {
        painelResultado.setVisible(mostrar);
        painelResultado.setManaged(mostrar);
        painelVazio.setVisible(!mostrar);
        painelVazio.setManaged(!mostrar);
    }

    /** Desliza o painel para cima, faz o fade-in e anima a rosca e o valor líquido. */
    private void animarEntrada() {
        FadeTransition fade = new FadeTransition(Duration.millis(300), painelResultado);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition sobe = new TranslateTransition(Duration.millis(300), painelResultado);
        sobe.setFromY(14);
        sobe.setToY(0);
        new ParallelTransition(fade, sobe).play();

        if (animacao != null) animacao.stop();
        progresso.set(0);
        animacao = new Timeline(new KeyFrame(Duration.millis(900),
                new KeyValue(progresso, 1, Interpolator.EASE_OUT)));
        animacao.play();
    }

    /** Desenha a rosca e o valor líquido proporcionalmente ao progresso da animação (0 a 1). */
    private void desenhar(double t) {
        if (ultima == null) return;
        double total = total(ultima);
        double gLiq = 360 * ultima.liquido() / total * t;
        double gInss = 360 * ultima.inss() / total * t;
        double gIr = 360 * ultima.ir() / total * t;
        // Sentido horário a partir do topo (90°)
        arcLiquido.setStartAngle(90);
        arcLiquido.setLength(-gLiq);
        arcInss.setStartAngle(90 - gLiq);
        arcInss.setLength(-gInss);
        arcIr.setStartAngle(90 - gLiq - gInss);
        arcIr.setLength(-gIr);
        resLiquido.setText(moeda(ultima.liquido() * t));
    }

    /** Só para testes/screenshots: termina a animação imediatamente. */
    void finalizarAnimacao() {
        if (animacao != null) animacao.stop();
        progresso.set(1);
        painelResultado.setOpacity(1);
        painelResultado.setTranslateY(0);
    }

    // =====================================================================
    // Página de tabelas
    // =====================================================================

    private void montarTabelas() {
        // INSS
        double[] limInss = CalculadoraInss.limites();
        double[] aliqInss = CalculadoraInss.aliquotas();
        tabelaInss.getChildren().add(linhaTabela(true, "Salário de contribuição", "Alíquota"));
        double anterior = 0;
        for (int i = 0; i < limInss.length; i++) {
            String faixa = i == 0 ? "Até " + moeda(limInss[i])
                    : "De " + moeda(anterior + 0.01) + " até " + moeda(limInss[i]);
            tabelaInss.getChildren().add(linhaTabela(false, faixa, percentual(aliqInss[i])));
            anterior = limInss[i];
        }

        // IRRF
        double[] limIr = CalculadoraIrpf.limites();
        double[] aliqIr = CalculadoraIrpf.aliquotas();
        double[] dedIr = CalculadoraIrpf.deducoes();
        tabelaIr.getChildren().add(linhaTabela(true, "Base de cálculo", "Alíquota", "Deduzir"));
        anterior = 0;
        for (int i = 0; i < limIr.length; i++) {
            String faixa;
            if (i == 0) faixa = "Até " + moeda(limIr[i]);
            else if (i == limIr.length - 1) faixa = "Acima de " + moeda(anterior);
            else faixa = "De " + moeda(anterior + 0.01) + " até " + moeda(limIr[i]);
            String aliquota = aliqIr[i] == 0 ? "Isento" : percentual(aliqIr[i]);
            String deducao = dedIr[i] == 0 ? "–" : moeda(dedIr[i]);
            tabelaIr.getChildren().add(linhaTabela(false, faixa, aliquota, deducao));
            anterior = limIr[i];
        }
        destacarFaixas(null);
    }

    private HBox linhaTabela(boolean cabecalho, String... colunas) {
        HBox linha = new HBox(12);
        linha.getStyleClass().add(cabecalho ? "tabela-cabecalho" : "tabela-linha");
        for (int i = 0; i < colunas.length; i++) {
            Label l = new Label(colunas[i]);
            l.setMaxWidth(Double.MAX_VALUE);
            // A primeira coluna (faixa) é mais larga que as outras
            l.setPrefWidth(i == 0 ? 320 : 140);
            l.setMinWidth(i == 0 ? 160 : 70);
            HBox.setHgrow(l, i == 0 ? Priority.ALWAYS : Priority.SOMETIMES);
            linha.getChildren().add(l);
        }
        return linha;
    }

    /** Pinta, nas tabelas, a faixa em que a última simulação caiu. */
    private void destacarFaixas(Simulacao s) {
        limparDestaque(tabelaInss);
        limparDestaque(tabelaIr);
        if (s == null) {
            avisoTabela.setText("Faça uma simulação e a faixa em que você se encaixa aparece destacada aqui.");
            return;
        }
        int faixaIr = indiceFaixa(CalculadoraIrpf.limites(), s.base());
        // +1 porque a linha 0 é o cabeçalho
        tabelaIr.getChildren().get(faixaIr + 1).getStyleClass().add(CLASSE_FAIXA_ATUAL);

        String teto;
        if (s.inssDescontado()) {
            int faixaInss = indiceFaixa(CalculadoraInss.limites(), s.salario());
            tabelaInss.getChildren().get(faixaInss + 1).getStyleClass().add(CLASSE_FAIXA_ATUAL);
            teto = s.salario() > CalculadoraInss.limites()[CalculadoraInss.limites().length - 1]
                    ? " (acima do teto: o INSS para de crescer)" : "";
        } else {
            teto = " (sem desconto de INSS)";
        }
        avisoTabela.setText("Sua simulação: salário de " + moeda(s.salario())
                + " e base do IR de " + moeda(s.base()) + teto + ". As faixas estão destacadas em azul.");
    }

    private static void limparDestaque(VBox tabela) {
        tabela.getChildren().forEach(n -> n.getStyleClass().remove(CLASSE_FAIXA_ATUAL));
    }

    private static int indiceFaixa(double[] limites, double valor) {
        for (int i = 0; i < limites.length; i++) {
            if (valor <= limites[i]) return i;
        }
        return limites.length - 1;
    }

    // =====================================================================
    // Textos que dependem das regras de cálculo (assim não ficam desatualizados)
    // =====================================================================

    private void preencherTextos() {
        String deducao = moeda(CalculadoraIrpf.DEDUCAO_DEPENDENTE);
        String isento = moeda(CalculadoraIrpf.ISENTO_ATE);
        String redutor = moeda(CalculadoraIrpf.REDUTOR_ATE);
        String simplificado = moeda(CalculadoraIrpf.DESCONTO_SIMPLIFICADO);

        textoDica.setText("Cada dependente reduz " + deducao + " da base de cálculo do Imposto de Renda.");

        regrasExtras.setText("•  Dedução por dependente: " + deducao + " por mês.\n"
                + "•  Desconto simplificado: " + simplificado + " por mês, usado no lugar de INSS + dependentes quando for maior.\n"
                + "•  Quem ganha até " + isento + " por mês fica isento de IR (Lei 15.270/2025).\n"
                + "•  Entre " + isento + " e " + redutor + " o imposto recebe uma redução gradual.\n"
                + "•  Acima de " + redutor + " vale a tabela progressiva normal.\n"
                + "•  13º salário: calculado separado do mês, com a mesma tabela e o mesmo redutor, mas sem "
                + "desconto simplificado (só INSS + dependentes). A 1ª parcela sai sem descontos e a 2ª leva "
                + "todo o INSS e o IR.");

        passo1.setText("O INSS é progressivo: cada alíquota (7,5% a 14%) incide só sobre a parte do salário "
                + "dentro da sua faixa. Acima do teto, o desconto para de aumentar.");
        passo2.setText("Do salário bruto tiramos o INSS e " + deducao + " por dependente, ou o desconto "
                + "simplificado de " + simplificado + " se ele for maior (como faz a Receita). "
                + "O que sobra é a base de cálculo do Imposto de Renda.");
        passo3.setText("A base entra na tabela do IRRF faixa por faixa: cada alíquota incide só sobre a parte "
                + "da base dentro da sua faixa. "
                + "Até " + isento + " de salário o IR é zero, e até " + redutor + " há um redutor.");
        passo4.setText("Salário líquido = bruto − INSS − IR. A alíquota efetiva mostra quanto do bruto "
                + "foi realmente para o Imposto de Renda.");
    }
}
