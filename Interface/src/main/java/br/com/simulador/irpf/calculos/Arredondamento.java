package br.com.simulador.irpf.calculos;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Valores com 2 casas decimais SEM arredondar: as casas extras são cortadas (truncadas),
 * como faz o simulador da Receita Federal. Ex.: 1.536,7899 -> 1.536,78 e 16,1767% -> 16,17%.
 *
 * As contas de dinheiro usam BigDecimal (decimal exato): um double guarda 8.892,80 como
 * 8.892,7999..., e cortar esse valor daria 8.892,79.
 */
public final class Arredondamento {

    private Arredondamento() {
    }

    /** Corta um valor em 2 casas decimais, sem arredondar. */
    public static BigDecimal duasCasas(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.DOWN);
    }

    /** Converte uma fração em porcentagem com 2 casas, sem arredondar. Ex.: 0,161767 -> 16,17. */
    public static BigDecimal percentualDuasCasas(double fracao) {
        return BigDecimal.valueOf(fracao).movePointRight(2).setScale(2, RoundingMode.DOWN);
    }

    /** Valor decimal exato de um double (usa a mesma representação que aparece ao imprimi-lo). */
    static BigDecimal exato(double valor) {
        return BigDecimal.valueOf(valor);
    }

    /** Resultado final em reais: cortado em 2 casas, sem arredondar. */
    static double dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.DOWN).doubleValue();
    }
}
