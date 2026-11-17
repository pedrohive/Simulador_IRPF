package calculos;

import java.math.BigDecimal;
import java.math.RoundingMode;

class Arredondamento {

    static double dinheiro(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
