package br.com.simulador.irpf.calculos;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 13º salário (CLT) - tributação exclusiva na fonte, calculada separada do salário do mês.
 *
 * <ul>
 *   <li>Valor bruto = salário mensal x meses trabalhados / 12 (proporcional; mês com 15 dias ou mais conta).</li>
 *   <li>INSS próprio, com a mesma tabela progressiva do mês.</li>
 *   <li>IR com a mesma tabela mensal e o redutor da Lei 15.270/2025, deduzindo só INSS + dependentes
 *       (o desconto simplificado não vale para o 13º).</li>
 *   <li>1ª parcela (até 30/11): metade do valor bruto, sem descontos.
 *       2ª parcela (até 20/12): a outra metade, menos todo o INSS e todo o IR.</li>
 * </ul>
 *
 * {@code calculo} traz INSS, base, IR e líquido do 13º inteiro (o "salário" dele é o valor bruto do 13º).
 */
public record DecimoTerceiro(double salarioMensal, int mesesTrabalhados, Simulacao calculo,
                             double primeiraParcela, double segundaParcela) {

    public static final int MESES_NO_ANO = 12;

    public static DecimoTerceiro calcular(double salarioMensal, int mesesTrabalhados, int dependentes) {
        if (mesesTrabalhados < 1 || mesesTrabalhados > MESES_NO_ANO) {
            throw new IllegalArgumentException("Meses trabalhados deve ser de 1 a 12.");
        }
        BigDecimal bruto = Arredondamento.exato(salarioMensal)
                .multiply(BigDecimal.valueOf(mesesTrabalhados))
                .divide(BigDecimal.valueOf(MESES_NO_ANO), 2, RoundingMode.DOWN);

        Simulacao calculo = Simulacao.calcularDecimoTerceiro(bruto.doubleValue(), dependentes);

        BigDecimal primeira = bruto.divide(BigDecimal.valueOf(2), 2, RoundingMode.DOWN);
        BigDecimal segunda = bruto.subtract(primeira)
                .subtract(Arredondamento.exato(calculo.inss()))
                .subtract(Arredondamento.exato(calculo.ir()));

        return new DecimoTerceiro(salarioMensal, mesesTrabalhados, calculo,
                primeira.doubleValue(), segunda.doubleValue());
    }

    /** Valor bruto do 13º (proporcional aos meses trabalhados). */
    public double valorBruto() {
        return calculo.salario();
    }
}
