package calculos;

/**
 * Contribuição ao INSS do empregado (CLT) - alíquotas progressivas, tabela 2026.
 * Cada alíquota incide apenas sobre a parcela do salário dentro da respectiva faixa.
 */
public class CalculadoraInss {

    // Limite superior de cada faixa (o último é o teto de contribuição)
    private static final double[] LIMITES = {1621.00, 2902.84, 4354.27, 8475.55};
    private static final double[] ALIQUOTAS = {0.075, 0.09, 0.12, 0.14};

    public static double calcular(double salario) {
        double contribuicao = 0;
        double limiteAnterior = 0;

        for (int i = 0; i < LIMITES.length; i++) {
            if (salario > limiteAnterior) {
                double parcela = Math.min(salario, LIMITES[i]) - limiteAnterior;
                contribuicao += parcela * ALIQUOTAS[i];
            }
            limiteAnterior = LIMITES[i];
        }
        return Arredondamento.dinheiro(contribuicao);
    }
}
