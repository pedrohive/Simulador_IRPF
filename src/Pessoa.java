public class Pessoa {

    private String nome;
    private String cpf;
    private double salario;
    private int dependentes;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        if (salario < 0) {
            throw new IllegalArgumentException("Salário não pode ser negativo.");
        }
        this.salario = salario;
    }

    public int getDependentes() {
        return dependentes;
    }

    public void setDependentes(int dependentes) {
        if (dependentes < 0) {
            throw new IllegalArgumentException("Número de dependentes não pode ser negativo.");
        }
        this.dependentes = dependentes;
    }

    public void setCpf(String cpf) {
        String digitos = cpf == null ? "" : cpf.replaceAll("[.\\-\\s]", "");
        if (!digitos.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos numéricos.");
        }
        this.cpf = digitos;
    }
}
