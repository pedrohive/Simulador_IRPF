package br.com.simulador.irpf.calculos;

/**
 * Imposto de Renda Retido na Fonte (IRRF) mensal - tabela progressiva 2026.
 *
 * Base de cálculo = salário - INSS - (dependentes x dedução por dependente).
 * IR = base x alíquota - parcela a deduzir, menos o redutor da Lei 15.270/2025
 * (isenção efetiva até R$ 5.000,00 e redução gradual até R$ 7.350,00).
 */
public class CalculadoraIrpf {

    public static final double DEDUCAO_DEPENDENTE = 189.59;

    // Limite superior da base de cada faixa, alíquota e parcela a deduzir
    private static final double[] LIMITES = {2428.80, 2826.65, 3751.05, 4664.68, Double.MAX_VALUE};
    private static final double[] ALIQUOTAS = {0.0, 0.075, 0.15, 0.225, 0.275};
    private static final double[] DEDUCOES = {0.0, 182.16, 394.16, 675.49, 908.73};

    // Redutor para rendimentos mensais até 7.350,00
    private static final double ISENTO_ATE = 5000.00;
    private static final double REDUTOR_ATE = 7350.00;
    private static final double REDUTOR_FIXO = 978.62;
    private static final double REDUTOR_COEF = 0.133145;

    public static double baseCalculo(double salario, double inss, int dependentes) {
        double base = salario - inss - dependentes * DEDUCAO_DEPENDENTE;
        return Math.max(base, 0);
    }

    public static double calcular(double salario, double inss, int dependentes) {
        double base = baseCalculo(salario, inss, dependentes);

        double imposto = 0;
        for (int i = 0; i < LIMITES.length; i++) {
            if (base <= LIMITES[i]) {
                imposto = base * ALIQUOTAS[i] - DEDUCOES[i];
                break;
            }
        }
        imposto = imposto - redutor(salario);

        return Arredondamento.dinheiro(Math.max(imposto, 0));
    }

    public static double aliquotaDaFaixa(double base) {
        for (int i = 0; i < LIMITES.length; i++) {
            if (base <= LIMITES[i]) {
                return ALIQUOTAS[i];
            }
        }
        return 0;
    }

    private static double redutor(double salario) {
        if (salario <= ISENTO_ATE) {
            return Double.MAX_VALUE; // imposto zerado
        }
        if (salario <= REDUTOR_ATE) {
            return REDUTOR_FIXO - REDUTOR_COEF * salario;
        }
        return 0;
    }
}
