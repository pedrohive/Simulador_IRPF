package br.com.simulador.irpf.calculos;

/**
 * Resultado completo do cálculo mensal. Console e interface gráfica usam esta mesma classe,
 * assim as duas formas de uso sempre mostram os mesmos números.
 */
public record Simulacao(double salario, int dependentes, double inss, double deducaoDependentes,
                        double base, double aliquotaFaixa, double ir, double liquido) {

    public static Simulacao calcular(double salario, int dependentes) {
        return calcular(salario, dependentes, 0);
    }

    /** Igual ao cálculo padrão, mas abatendo da base do IR outras deduções legais mensais. */
    public static Simulacao calcular(double salario, int dependentes, double outrasDeducoes) {
        double inss = CalculadoraInss.calcular(salario);
        double base = CalculadoraIrpf.baseCalculo(salario, inss, dependentes, outrasDeducoes);
        double ir = CalculadoraIrpf.calcular(salario, inss, dependentes, outrasDeducoes);
        return new Simulacao(salario, dependentes, inss,
                dependentes * CalculadoraIrpf.DEDUCAO_DEPENDENTE,
                base, CalculadoraIrpf.aliquotaDaFaixa(base), ir, salario - inss - ir);
    }

    public double aliquotaEfetiva() {
        return salario > 0 ? ir / salario : 0;
    }
}
