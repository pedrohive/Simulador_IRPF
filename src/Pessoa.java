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

    public void setCpf(String cpf) {
        this.cpf = cpf;
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
}
