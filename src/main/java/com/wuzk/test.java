package com.wuzk;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class test {
    public static void main(String[] args) {
        RandomGenerator generator = RandomGeneratorFactory.of("Xoshiro256PlusPlus").create();

        System.out.println("Random int: " + generator.nextInt());
        System.out.println("Random long: " + generator.nextLong());
        System.out.println("Random double: " + generator.nextDouble());
    }
}
