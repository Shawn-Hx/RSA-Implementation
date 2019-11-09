package cn.edu.tsinghua;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KeyGenerator {

    private static final int UPPER_BOUND = 1000;
    private static final List<Integer> SMALL_PRIMES;

    static {
        boolean[] isComposite = new boolean[UPPER_BOUND];
        SMALL_PRIMES = new ArrayList<>();
        for (int i = 2; i < UPPER_BOUND; i++) {
            if (!isComposite[i])
                SMALL_PRIMES.add(i);
            for (int prime : SMALL_PRIMES) {
                int tmp = i * prime;
                if (tmp >= UPPER_BOUND)
                    break;
                isComposite[tmp] = true;
            }
        }
    }

    private static boolean millerRabin(final BigNumber n, long s) {
        BigNumber nMinusOne = n.minusOne();
        BigNumber u = new BigNumber(nMinusOne);
        int t = u.getLowestZeroBits();
        u.shiftRight(t);
        for (long i = 0; i < s; i++) {
            BigNumber a = BigNumber.random(nMinusOne, new Random());
            BigNumber x = a.montExp(u, n);
            int j = 0;
            while (!((j == 0 && x.equalsTo(BigNumber.ONE)) || x.equalsTo(nMinusOne))) {
                if (j > 0 && x.equalsTo(BigNumber.ONE) || ++j == t)
                    return false;
                x = x.multiply(x).mod(n);
            }
        }
        return true;
    }

    private static BigNumber generatePrime(int bitLen, Random random) {
        BigNumber pseudoPrime;
        int countSmallPrimeCheck = 0;
        int countMillerRabin = 0;
        do {
            pseudoPrime = BigNumber.randomOdd(bitLen, new Random());
            boolean isCandidate = true;
            for (int i = 1; i < SMALL_PRIMES.size(); i++) {
                if (pseudoPrime.mod(SMALL_PRIMES.get(i)) == 0) {
                    isCandidate = false;
                    break;
                }
            }
            if (!isCandidate) {
                countSmallPrimeCheck ++;
                continue;
            }
            countMillerRabin ++;
        } while (!millerRabin(pseudoPrime, 2));
        System.out.println("Random " + countSmallPrimeCheck + " times cannot pass small primes check.");
        System.out.println("Random " + countMillerRabin + " times miller rabin to find a prime.");
        return pseudoPrime;
    }

    public static Keys generate(int bitLen, long smallE) {
        Random random1 = new Random(), random2 = new Random();
        BigNumber p, q, n, e, d;
        e = BigNumber.valueOf(smallE);
        do {
            p = generatePrime(bitLen, random1);
            q = generatePrime(bitLen, random2);
            n = p.multiply(q);
            d = e.modInverse(p.minusOne().multiply(q.minusOne()));
        } while (d == null);
        return new Keys(p, q, n, e, d);
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        BigNumber prime = generatePrime(384, new Random());
        long end = System.currentTimeMillis();
        System.out.println("Time elapse: " + (end - start) + "ms");

        System.out.println(prime.toString());
        BigInteger bigInt = new BigInteger(prime.toString(), 16);
        System.out.println(bigInt.isProbablePrime(100));
    }
}
