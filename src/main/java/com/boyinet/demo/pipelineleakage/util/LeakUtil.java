package com.boyinet.demo.pipelineleakage.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author lengchunyun
 */
public class LeakUtil {

    public static BigDecimal calcMain(BigDecimal k, BigDecimal diff, BigDecimal r, BigDecimal l) {

        BigDecimal molecular = k.multiply(diff)
                .multiply(BigDecimal.valueOf(3.14))
                .multiply(r)
                .multiply(r)
                .multiply(r)
                .multiply(r);

        BigDecimal denominator = BigDecimal.valueOf(8)
                .multiply(l).multiply(BigDecimal.valueOf(0.001005));

        return molecular.divide(denominator, 6, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcBranch(BigDecimal diff) {

        BigDecimal r = BigDecimal.valueOf(0.0225).multiply(BigDecimal.valueOf(0.0225))
                .add(BigDecimal.valueOf(0.013).multiply(BigDecimal.valueOf(0.013)))
                .divide(BigDecimal.valueOf(0.0225 + 0.013), 6, RoundingMode.HALF_UP);
        BigDecimal molecular = diff
                .multiply(BigDecimal.valueOf(3.14))
                .multiply(r)
                .multiply(r)
                .multiply(r)
                .multiply(r);
        BigDecimal denominator = BigDecimal.valueOf(8)
                .multiply(BigDecimal.valueOf(24 + 12).multiply(BigDecimal.valueOf(0.001005)));

        return molecular.divide(denominator, 6, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcDistance(BigDecimal k, BigDecimal diff, BigDecimal r, BigDecimal q) {

        BigDecimal molecular = k.multiply(diff)
                .multiply(BigDecimal.valueOf(3.14))
                .multiply(r)
                .multiply(r)
                .multiply(r)
                .multiply(r);

        BigDecimal denominator = BigDecimal.valueOf(8)
                .multiply(q).multiply(BigDecimal.valueOf(0.001005));

        return molecular.divide(denominator, 6, RoundingMode.HALF_UP);
    }


    public static void main(String[] args) {
        System.out.println(calcMain(BigDecimal.valueOf(25*1000),BigDecimal.valueOf(5),
                BigDecimal.valueOf(0.0224),BigDecimal.valueOf(24)));

        System.out.println(calcDistance(BigDecimal.valueOf(25*1000),BigDecimal.valueOf(13),
                BigDecimal.valueOf(0.0224),BigDecimal.valueOf(0.627)));
    }
}
