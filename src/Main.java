import calculos.CalculadoraInss;
import calculos.CalculadoraIrpf;

import java.util.Locale;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in).useLocale(Locale.of("pt", "BR"));

        System.out.println("=== Sistema Receita Federal - Cálculo de IRRF mensal ===");

        boolean continuar = true;
        while (continuar) {
            Pessoa p = lerPessoa(sc);
            exibirResultado(p);

            System.out.print("\nCalcular outra pessoa? (s/n): ");
            continuar = sc.next().equalsIgnoreCase("s");
            System.out.println();
        }
    }

    private static Pessoa lerPessoa(Scanner sc) {
        Pessoa p = new Pessoa();

        System.out.print("Nome: ");
        p.setNome(sc.next());

        System.out.print("CPF: ");
        p.setCpf(sc.next());

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

    private static void exibirResultado(Pessoa p) {
        double inss = CalculadoraInss.calcular(p.getSalario());
        double base = CalculadoraIrpf.baseCalculo(p.getSalario(), inss, p.getDependentes());
        double ir = CalculadoraIrpf.calcular(p.getSalario(), inss, p.getDependentes());
        double liquido = p.getSalario() - inss - ir;

        System.out.println("\n--- Resultado ---");
        System.out.printf("Contribuinte:      %s (CPF %s)%n", p.getNome(), p.getCpf());
        System.out.printf("Salário bruto:     R$ %,.2f%n", p.getSalario());
        System.out.printf("INSS:              R$ %,.2f%n", inss);
        System.out.printf("Dependentes:       %d (dedução R$ %,.2f)%n",
                p.getDependentes(), p.getDependentes() * CalculadoraIrpf.DEDUCAO_DEPENDENTE);
        System.out.printf("Base de cálculo:   R$ %,.2f%n", base);
        System.out.printf("Imposto de Renda:  R$ %,.2f%n", ir);
        System.out.printf("Salário líquido:   R$ %,.2f%n", liquido);
    }
}
