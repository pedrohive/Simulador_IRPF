package br.com.simulador.irpf.calculos;

/**
 * Resultado completo do cálculo mensal. Console e interface gráfica usam esta mesma classe,
 * assim as duas formas de uso sempre mostram os mesmos números.
 *
 * {@code inssDescontado} = false simula rendimentos sem contribuição ao INSS (autônomo, aluguel...),
 * equivalente a deixar "Previdência oficial" vazia no simulador da Receita.
 */
public record Simulacao(double salario, int dependentes, boolean inssDescontado, double inss,
                        double deducaoDependentes, boolean descontoSimplificado, double deducaoUtilizada,
                        double base, double aliquotaFaixa, double ir, double liquido) {

    /** Cálculo padrão de quem é CLT: o INSS é descontado do salário e da base do IR. */
    public static Simulacao calcular(double salario, int dependentes) {
        return calcular(salario, dependentes, true);
    }

    public static Simulacao calcular(double salario, int dependentes, boolean descontarInss) {
        double inss = descontarInss ? CalculadoraInss.calcular(salario) : 0;
        double base = CalculadoraIrpf.baseCalculo(salario, inss, dependentes);
        double ir = CalculadoraIrpf.calcular(salario, inss, dependentes);
        // líquido em decimal exato: com double, 5500 - 571,51 - 190,47 pode virar 4738,0199...
        double liquido = Arredondamento.exato(salario)
                .subtract(Arredondamento.exato(inss))
                .subtract(Arredondamento.exato(ir))
                .doubleValue();
        return new Simulacao(salario, dependentes, descontarInss, inss,
                CalculadoraIrpf.deducaoDependentes(dependentes).doubleValue(),
                CalculadoraIrpf.usaDescontoSimplificado(inss, dependentes),
                CalculadoraIrpf.deducaoUtilizada(inss, dependentes),
                base, CalculadoraIrpf.aliquotaDaFaixa(base), ir, liquido);
    }

    public double aliquotaEfetiva() {
        return salario > 0 ? ir / salario : 0;
    }
}
