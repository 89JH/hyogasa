package com.hyogasa.hgs2;

import java.math.BigDecimal;

//최소 호가 (이상)	최대 호가 (미만)	주문 가격 단위 (원)
//        2,000,000		1,000
//        1,000,000	2,000,000	500
//        500,000	1,000,000	100
//        100,000	500,000	50
//        10,000	100,000	10
//        1,000	10,000	5
//        100	1,000	1
//        10	100	0.1
//        1	10	0.01
//        0.1	1	0.001
//        0	0.1	0.0001

public class scaleTest {
    public static void main(String[] args) {
        System.out.println("200만원 이상 - " + new BigDecimal("2000500").setScale(-3, BigDecimal.ROUND_HALF_UP).toPlainString());
        System.out.println("100만원 이상 - " + new BigDecimal("1000500").setScale(-3, BigDecimal.ROUND_HALF_UP).toPlainString());
        System.out.println("200만원 이상 - " + new BigDecimal("2000500").setScale(-3, BigDecimal.ROUND_HALF_UP).toPlainString());
        System.out.println("200만원 이상 - " + new BigDecimal("2000500").setScale(-3, BigDecimal.ROUND_HALF_UP).toPlainString());
        System.out.println("200만원 이상 - " + new BigDecimal("2000500").setScale(-3, BigDecimal.ROUND_HALF_UP).toPlainString());
        System.out.println("200만원 이상 - " + new BigDecimal("2000500").setScale(-3, BigDecimal.ROUND_HALF_UP).toPlainString());
    }
}
