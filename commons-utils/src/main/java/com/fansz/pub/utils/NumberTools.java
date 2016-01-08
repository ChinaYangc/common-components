package com.fansz.pub.utils;

public final class NumberTools {
    public static boolean isGreater(Integer a, Integer b) {
        int a1 = (a == null ? 0 : a);
        int b1 = (b == null ? 0 : b);
        return a1 > b1;
    }
}
