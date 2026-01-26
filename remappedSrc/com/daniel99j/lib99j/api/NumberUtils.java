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
}
