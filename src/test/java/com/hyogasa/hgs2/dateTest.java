package com.hyogasa.hgs2;

import java.math.BigDecimal;
import java.util.Date;

public class dateTest {
    public static void main(String[] args) {
        Date now = new Date();

        System.out.println("JHJ" + now.getTime());

        System.out.println(new BigDecimal("1005").multiply(new BigDecimal("0.99")).setScale(1, BigDecimal.ROUND_HALF_DOWN));

        String ss = "";

        for(int i=0; i<100; i++) {
            ss += ",\"" + i + "\"";
        }

        System.out.println(ss.substring(1));


    }
}
