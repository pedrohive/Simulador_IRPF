package calculos;

import java.math.BigDecimal;
import java.math.RoundingMode;

class Arredondamento {

    //Sistema para realizar o arredondamento de dizimas periódicas.
    static double dinheiro(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
