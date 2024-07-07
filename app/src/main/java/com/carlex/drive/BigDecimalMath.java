package com.carlex.drive;

import java.math.BigDecimal;
import java.math.MathContext;

public class BigDecimalMath {
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    public static BigDecimal sin(BigDecimal value) {
        return new BigDecimal(Math.sin(value.doubleValue()), MATH_CONTEXT);
    }

    public static BigDecimal cos(BigDecimal value) {
        return new BigDecimal(Math.cos(value.doubleValue()), MATH_CONTEXT);
    }

    public static BigDecimal atan2(BigDecimal y, BigDecimal x) {
        return new BigDecimal(Math.atan2(y.doubleValue(), x.doubleValue()), MATH_CONTEXT);
    }

    public static BigDecimal sqrt(BigDecimal value, MathContext context) {
        BigDecimal x0 = new BigDecimal(0);
        BigDecimal x1 = new BigDecimal(Math.sqrt(value.doubleValue()));
        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = value.divide(x0, context);
            x1 = x1.add(x0);
            x1 = x1.divide(new BigDecimal(2), context);
        }
        return x1;
    }
}
