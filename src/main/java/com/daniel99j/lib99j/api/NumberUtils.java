package com.daniel99j.lib99j.api;

import java.util.Random;

@SuppressWarnings({"unused"})
public class NumberUtils {
    public static int getRandomInt(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    public static float getRandomFloat(float min, float max) {
        Random random = new Random();
        return random.nextFloat((max - min) + 1) + min;
    }

    public static int getRandomInt(Random random, int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    public static float getRandomFloat(Random random, float min, float max) {
        return random.nextFloat((max - min) + 1) + min;
    }

    public static float makeNotZero(float input) {
        return input == 0 ? 0.00000000000001F : input;
    }

    public static double makeNotZero(double input) {
        return input == 0 ? 0.00000000000001 : input;
    }

    public static Number getMax(Number number) {
        if(number instanceof Integer) return Integer.MAX_VALUE;
        else if(number instanceof Float) return Float.MAX_VALUE;
        else if(number instanceof Short) return Short.MAX_VALUE;
        else if(number instanceof Long) return Long.MAX_VALUE;
        else if(number instanceof Double) return Double.MAX_VALUE;
        throw new IllegalStateException("Not a number");
    }

    public static Number getMin(Number number) {
        if(number instanceof Integer) return Integer.MIN_VALUE;
        else if(number instanceof Float) return Float.MIN_VALUE;
        else if(number instanceof Short) return Short.MIN_VALUE;
        else if(number instanceof Long) return Long.MIN_VALUE;
        else if(number instanceof Double) return Double.MIN_VALUE;
        throw new IllegalStateException("Not a number");
    }

    public static Number convertInto(Number number, Number type) {
        if(type instanceof Integer) {
            return number.intValue();
        }
        else if(type instanceof Float) {
            return number.floatValue();
        }
        else if(type instanceof Short) {
            return number.shortValue();
        }
        else if(type instanceof Long) {
            return number.longValue();
        }
        else if(type instanceof Double) {
            return number.doubleValue();
        }
        throw new IllegalStateException("Not a number");
    }
}
