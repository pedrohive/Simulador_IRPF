package br.com.simulador.irpf.calculos;

import java.math.BigDecimal;

/**
 * Contribuição ao INSS do empregado (CLT) - alíquotas progressivas, tabela 2026.
 * Cada alíquota incide apenas sobre a parcela do salário dentro da respectiva faixa.
 */
public class CalculadoraInss {

    // Limite superior de cada faixa (o último é o teto de contribuição)
    private static final double[] LIMITES = {1621.00, 2902.84, 4354.27, 8475.55};
    private static final double[] ALIQUOTAS = {0.075, 0.09, 0.12, 0.14};

    /** Limites das faixas (cópia, para exibir na interface sem permitir alteração). */
    public static double[] limites() { return LIMITES.clone(); }

    /** Alíquotas de cada faixa (cópia). */
    public static double[] aliquotas() { return ALIQUOTAS.clone(); }

    /** Contribuição do mês, com 2 casas decimais cortadas (sem arredondar). */
    public static double calcular(double salario) {
        BigDecimal valor = Arredondamento.exato(salario);
        BigDecimal contribuicao = BigDecimal.ZERO;
        BigDecimal limiteAnterior = BigDecimal.ZERO;

        for (int i = 0; i < LIMITES.length; i++) {
            BigDecimal limite = Arredondamento.exato(LIMITES[i]);
            if (valor.compareTo(limiteAnterior) > 0) {
                BigDecimal parcela = valor.min(limite).subtract(limiteAnterior);
                contribuicao = contribuicao.add(parcela.multiply(Arredondamento.exato(ALIQUOTAS[i])));
            }
            limiteAnterior = limite;
        }
        return Arredondamento.dinheiro(contribuicao);
    }
}
