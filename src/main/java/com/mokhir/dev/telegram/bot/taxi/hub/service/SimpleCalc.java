package com.mokhir.dev.telegram.bot.taxi.hub.service;

public class SimpleCalc {

    public static int eval(String expr) {
        int result = 0;
        int sign = 1;
        int num = 0;

        for (char c : expr.toCharArray()) {
            if (c == '+') {
                result += sign * num;
                num = 0;
                sign = 1;
            } else if (c == '-') {
                result += sign * num;
                num = 0;
                sign = -1;
            } else {
                num = num * 10 + (c - '0'); // поддержка многозначных чисел
            }
        }
        result += sign * num;
        return result;
    }
}
