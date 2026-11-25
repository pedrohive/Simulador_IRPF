package br.com.simulador.irpf;

import br.com.simulador.irpf.calculos.DecimoTerceiro;
import br.com.simulador.irpf.calculos.Simulacao;

import static br.com.simulador.irpf.calculos.Arredondamento.duasCasas;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Scanner;

public class ConsoleApp {

    public static void executar() {
        Scanner sc = new Scanner(System.in).useLocale(Locale.of("pt", "BR"));

        System.out.println("=== Sistema Receita Federal - Cálculo de IRRF (mensal e 13º) ===");

        boolean continuar = true;
        while (continuar) {
            Pessoa p = lerPessoa(sc);
            System.out.print("Calcular 1) salário mensal ou 2) 13º salário? (1/2): ");
            if (sc.next().equals("2")) {
                exibirDecimoTerceiro(p, lerAdmissao(sc));
            } else {
                System.out.print("Descontar INSS (CLT)? (s/n): ");
                boolean descontarInss = !sc.next().equalsIgnoreCase("n");
                exibirResultado(p, descontarInss);
            }

            System.out.print("\nCalcular outra pessoa? (s/n): ");
            continuar = sc.next().equalsIgnoreCase("s");
            System.out.println();
        }
    }

    /** Lê a linha inteira (nomes têm espaço), ignorando o fim de linha que sobrou da leitura anterior. */
    private static String lerNome(Scanner sc) {
        String linha;
        do {
            linha = sc.nextLine();
        } while (linha.isBlank() && sc.hasNextLine());
        return linha;
    }

    private static Pessoa lerPessoa(Scanner sc) {
        Pessoa p = new Pessoa();

        while (true) {
            try {
                System.out.print("Nome: ");
                p.setNome(lerNome(sc));
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

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    /** Pede a data de admissão até vir uma data válida que dê direito a algum 13º proporcional. */
    private static LocalDate lerAdmissao(Scanner sc) {
        while (true) {
            System.out.print("Data de admissão (dd/mm/aaaa): ");
            try {
                LocalDate admissao = LocalDate.parse(sc.next(), DATA);
                DecimoTerceiro.mesesTrabalhados(admissao, LocalDate.now());
                return admissao;
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato dd/mm/aaaa.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void exibirDecimoTerceiro(Pessoa p, LocalDate admissao) {
        DecimoTerceiro d = DecimoTerceiro.calcular(p.getSalario(), admissao, LocalDate.now(), p.getDependentes());
        int meses = d.mesesTrabalhados();
        Simulacao s = d.calculo();

        System.out.println("\n--- Resultado: 13º salário ---");
        System.out.printf("Contribuinte:      %s (CPF %s)%n", p.getNome(), p.getCpf());
        System.out.printf("Admissão:          %s (%d meses no ano)%n", admissao.format(DATA), meses);
        System.out.printf("13º bruto:         R$ %,.2f (%d/12 de R$ %,.2f)%n",
                duasCasas(d.valorBruto()), meses, duasCasas(d.salarioMensal()));
        System.out.printf("INSS:              R$ %,.2f%n", duasCasas(s.inss()));
        System.out.printf("Dependentes:       %d (dedução R$ %,.2f)%n", s.dependentes(), duasCasas(s.deducaoDependentes()));
        System.out.printf("Dedução usada:     R$ %,.2f (INSS + dependentes; 13º não tem desconto simplificado)%n",
                duasCasas(s.deducaoUtilizada()));
        System.out.printf("Base de cálculo:   R$ %,.2f%n", duasCasas(s.base()));
        System.out.printf("Imposto de Renda:  R$ %,.2f%n", duasCasas(s.ir()));
        System.out.printf("13º líquido:       R$ %,.2f%n", duasCasas(s.liquido()));
        System.out.printf("1ª parcela:        R$ %,.2f (até 30/11, sem descontos)%n", duasCasas(d.primeiraParcela()));
        System.out.printf("2ª parcela:        R$ %,.2f (até 20/12, com todo o INSS e IR)%n", duasCasas(d.segundaParcela()));
    }
}
