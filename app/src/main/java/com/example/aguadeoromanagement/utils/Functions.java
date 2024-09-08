package com.example.aguadeoromanagement.utils;

public class Functions {

    public static Double roundWithNDecimal(double num, int n) {
        return Math.round((Math.pow(10.0, n) * num)) / Math.pow(10.0, n);
//        return (10.0.pow(n.toDouble()) * num).roundToInt() / 10.0.pow(n.toDouble())


    }

    public static Double toPrice(double num) {
        return roundWithNDecimal(num, 2);
    }
}
