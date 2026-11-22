package br.com.simulador.irpf;

import br.com.simulador.irpf.calculos.CalculadoraIrpf;
import br.com.simulador.irpf.calculos.Simulacao;
import javafx.scene.web.WebEngine;

import java.util.Locale;

/**
 * Bridge exposed to the dashboard page as {@code window.javaBridge}.
 * JS calls {@link #calculateFromWeb}; the result goes back through
 * {@code window.applySimulationFromJava(payload)}.
 *
 * The class and its method must stay public: WebKit reflects on them through JSObject.
 */
public class IrpfBridge {

    private final WebEngine engine;

    public IrpfBridge(WebEngine engine) {
        this.engine = engine;
    }

    /**
     * @param income      monthly taxable income (gross salary), in BRL
     * @param deductions  other monthly legal deductions (alimony, private pension...), in BRL
     * @param dependents  number of dependents
     */
    public void calculateFromWeb(double income, double deductions, int dependents) {
        String payload;
        try {
            validate(income, deductions, dependents);
            Simulacao s = Simulacao.calcular(income, dependents, deductions);
            payload = toJson(s, deductions);
        } catch (IllegalArgumentException e) {
            payload = "{\"ok\":false,\"message\":\"" + escape(e.getMessage()) + "\"}";
        }
        engine.executeScript("window.applySimulationFromJava(" + payload + ")");
    }

    private static void validate(double income, double deductions, int dependents) {
        if (Double.isNaN(income) || Double.isInfinite(income) || income < 0) {
            throw new IllegalArgumentException("Informe uma renda válida (não negativa).");
        }
        if (Double.isNaN(deductions) || Double.isInfinite(deductions) || deductions < 0) {
            throw new IllegalArgumentException("Deduções não podem ser negativas.");
        }
        if (dependents < 0) {
            throw new IllegalArgumentException("Número de dependentes não pode ser negativo.");
        }
    }

    private static String toJson(Simulacao s, double deductions) {
        return String.format(Locale.ROOT,
                "{\"ok\":true,\"gross\":%.2f,\"inss\":%.2f,\"dependents\":%d,\"dependentsDeduction\":%.2f,"
                        + "\"otherDeductions\":%.2f,\"taxableBase\":%.2f,\"bracketRate\":%.4f,"
                        + "\"tax\":%.2f,\"net\":%.2f,\"effectiveRate\":%.6f,\"deductionPerDependent\":%.2f}",
                s.salario(), s.inss(), s.dependentes(), s.deducaoDependentes(),
                deductions, s.base(), s.aliquotaFaixa(), s.ir(), s.liquido(), s.aliquotaEfetiva(),
                CalculadoraIrpf.DEDUCAO_DEPENDENTE);
    }

    private static String escape(String text) {
        return text == null ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
