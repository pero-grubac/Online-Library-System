package org.unibl.etf.mdp.supplier.services;

import java.util.Random;

/** Unit prices are set per order, at random, between PRICE_MIN and PRICE_MAX. */
public class Pricing {
    private final int min;
    private final int max;
    private final Random random;

    public Pricing(int min, int max, Random random) {
        if (min < 0 || max < min) {
            throw new IllegalArgumentException("Invalid price range " + min + ".." + max);
        }
        this.min = min;
        this.max = max;
        this.random = random;
    }

    public int nextPrice() {
        return min + random.nextInt(max - min + 1);
    }
}
