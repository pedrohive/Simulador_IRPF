package br.com.simulador.irpf.calculos;

import java.math.BigDecimal;

/**
 * Imposto de Renda Retido na Fonte (IRRF) mensal - tabela progressiva 2026.
 *
 * Base de cálculo = salário - dedução usada, onde a dedução usada é a MAIOR entre
 * as deduções legais (INSS + dependentes x dedução por dependente) e o desconto
 * simplificado mensal, como faz o simulador da Receita Federal.
 * IR = soma, faixa por faixa, de (parte da base na faixa x alíquota), menos o redutor da Lei 15.270/2025
 * (isenção efetiva até R$ 5.000,00 e redução gradual até R$ 7.350,00).
 */
public class CalculadoraIrpf {

    public static final double DEDUCAO_DEPENDENTE = 189.59;

    /** Desconto simplificado mensal (25% do limite da faixa isenta), usado quando supera as deduções legais. */
    public static final double DESCONTO_SIMPLIFICADO = 607.20;

    // Limite superior da base de cada faixa, alíquota e parcela a deduzir (esta só para exibir na tabela)
    private static final double[] LIMITES = {2428.80, 2826.65, 3751.05, 4664.68, Double.MAX_VALUE};
    private static final double[] ALIQUOTAS = {0.0, 0.075, 0.15, 0.225, 0.275};
    private static final double[] DEDUCOES = {0.0, 182.16, 394.16, 675.49, 908.73};

    // Redutor para rendimentos mensais até 7.350,00
    public static final double ISENTO_ATE = 5000.00;
    public static final double REDUTOR_ATE = 7350.00;
    private static final double REDUTOR_FIXO = 978.62;
    private static final double REDUTOR_COEF = 0.133145;

    /** Limites das faixas da base de cálculo (cópia; o último é "sem limite"). */
    public static double[] limites() { return LIMITES.clone(); }

    /** Alíquotas de cada faixa (cópia). */
    public static double[] aliquotas() { return ALIQUOTAS.clone(); }

    /** Parcela a deduzir de cada faixa (cópia). */
    public static double[] deducoes() { return DEDUCOES.clone(); }

    /** INSS + dedução por dependente. */
    public static double deducoesLegais(double inss, int dependentes) {
        return Arredondamento.exato(inss).add(deducaoDependentes(dependentes)).doubleValue();
    }

    /** Dependentes x dedução por dependente, em decimal exato. */
    static BigDecimal deducaoDependentes(int dependentes) {
        return Arredondamento.exato(DEDUCAO_DEPENDENTE).multiply(BigDecimal.valueOf(dependentes));
    }

    /** Dedução abatida do salário: a maior entre as deduções legais e o desconto simplificado. */
    public static double deducaoUtilizada(double inss, int dependentes) {
        return Math.max(deducoesLegais(inss, dependentes), DESCONTO_SIMPLIFICADO);
    }

    /** true quando o desconto simplificado é mais vantajoso que as deduções legais. */
    public static boolean usaDescontoSimplificado(double inss, int dependentes) {
        return DESCONTO_SIMPLIFICADO > deducoesLegais(inss, dependentes);
    }

    public static double baseCalculo(double salario, double inss, int dependentes) {
        BigDecimal base = Arredondamento.exato(salario)
                .subtract(Arredondamento.exato(deducaoUtilizada(inss, dependentes)));
        return base.max(BigDecimal.ZERO).doubleValue();
    }

    /** IR do mês, com 2 casas decimais cortadas (sem arredondar). */
    public static double calcular(double salario, double inss, int dependentes) {
        if (salario <= ISENTO_ATE) {
            return 0; // redutor da Lei 15.270/2025 zera o imposto
        }
        double base = baseCalculo(salario, inss, dependentes);

        // Faixa por faixa, como o simulador da Receita: cada alíquota incide só sobre a parte da base
        // dentro da sua faixa. Não usa a "parcela a deduzir" porque ela vem arredondada na tabela
        // (908,73 em vez de 908,7215) e, cortando sem arredondar, isso tiraria 1 centavo do imposto.
        BigDecimal valorBase = Arredondamento.exato(base);
        BigDecimal imposto = BigDecimal.ZERO;
        BigDecimal limiteAnterior = BigDecimal.ZERO;
        for (int i = 0; i < LIMITES.length && valorBase.compareTo(limiteAnterior) > 0; i++) {
            BigDecimal parteNaFaixa = i == LIMITES.length - 1
                    ? valorBase.subtract(limiteAnterior)
                    : valorBase.min(Arredondamento.exato(LIMITES[i])).subtract(limiteAnterior);
            imposto = imposto.add(parteNaFaixa.multiply(Arredondamento.exato(ALIQUOTAS[i])));
            if (i < LIMITES.length - 1) {
                limiteAnterior = Arredondamento.exato(LIMITES[i]);
            }
        }
        imposto = imposto.subtract(redutor(salario));

        return Arredondamento.dinheiro(imposto.max(BigDecimal.ZERO));
    }

    public static double aliquotaDaFaixa(double base) {
        for (int i = 0; i < LIMITES.length; i++) {
            if (base <= LIMITES[i]) {
                return ALIQUOTAS[i];
            }
        }
        return 0;
    }

    /** Redução gradual para salários acima de ISENTO_ATE e até REDUTOR_ATE (Lei 15.270/2025). */
    private static BigDecimal redutor(double salario) {
        if (salario <= REDUTOR_ATE) {
            return Arredondamento.exato(REDUTOR_FIXO)
                    .subtract(Arredondamento.exato(REDUTOR_COEF).multiply(Arredondamento.exato(salario)));
        }
        return BigDecimal.ZERO;
    }
}
