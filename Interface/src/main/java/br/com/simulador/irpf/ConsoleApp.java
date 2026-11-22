package br.com.simulador.irpf;

import br.com.simulador.irpf.calculos.Simulacao;

import static br.com.simulador.irpf.calculos.Arredondamento.duasCasas;

import java.util.Locale;
import java.util.Scanner;

public class ConsoleApp {

    public static void executar() {
        Scanner sc = new Scanner(System.in).useLocale(Locale.of("pt", "BR"));

        System.out.println("=== Sistema Receita Federal - Cálculo de IRRF mensal ===");

        boolean continuar = true;
        while (continuar) {
            Pessoa p = lerPessoa(sc);
            System.out.print("Descontar INSS (CLT)? (s/n): ");
            boolean descontarInss = !sc.next().equalsIgnoreCase("n");
            exibirResultado(p, descontarInss);

            System.out.print("\nCalcular outra pessoa? (s/n): ");
            continuar = sc.next().equalsIgnoreCase("s");
            System.out.println();
        }
    }

    private static Pessoa lerPessoa(Scanner sc) {
        Pessoa p = new Pessoa();

        while (true) {
            try {
                System.out.print("Nome: ");
                p.setNome(sc.next());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        while (true) {
            try {
                System.out.print("CPF (11 dígitos): ");
                p.setCpf(sc.next());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        while (true) {
            try {
                System.out.print("Salário bruto mensal (ex: 4500,00): ");
                p.setSalario(sc.nextDouble());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (java.util.InputMismatchException e) {
                sc.next();
                System.out.println("Valor inválido.");
            }
        }

        while (true) {
            try {
                System.out.print("Número de dependentes: ");
                p.setDependentes(sc.nextInt());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (java.util.InputMismatchException e) {
                sc.next();
                System.out.println("Valor inválido.");
            }
        }
        return p;
    }

    private static void exibirResultado(Pessoa p, boolean descontarInss) {
        Simulacao s = Simulacao.calcular(p.getSalario(), p.getDependentes(), descontarInss);

        System.out.println("\n--- Resultado ---");
        System.out.printf("Contribuinte:      %s (CPF %s)%n", p.getNome(), p.getCpf());
        System.out.printf("Salário bruto:     R$ %,.2f%n", duasCasas(s.salario()));
        if (s.inssDescontado()) {
            System.out.printf("INSS:              R$ %,.2f%n", duasCasas(s.inss()));
        } else {
            System.out.println("INSS:              não descontado");
        }
        System.out.printf("Dependentes:       %d (dedução R$ %,.2f)%n", s.dependentes(), duasCasas(s.deducaoDependentes()));
        System.out.printf("Dedução usada:     R$ %,.2f (%s)%n", duasCasas(s.deducaoUtilizada()),
                s.descontoSimplificado() ? "desconto simplificado"
                        : s.inssDescontado() ? "INSS + dependentes" : "dependentes");
        System.out.printf("Base de cálculo:   R$ %,.2f%n", duasCasas(s.base()));
        System.out.printf("Imposto de Renda:  R$ %,.2f%n", duasCasas(s.ir()));
        System.out.printf("Salário líquido:   R$ %,.2f%n", duasCasas(s.liquido()));
    }
}
