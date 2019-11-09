package cn.edu.tsinghua;

import java.math.BigInteger;

public class Test {

    private static void testMultiply() {
        BigInteger bigInt = new BigInteger("9eeffab00013", 16);
        BigInteger bigInt2 = new BigInteger("a5902abcdef008f1", 16);
        BigNumber num1 = new BigNumber(bigInt);
        BigNumber num2 = new BigNumber(bigInt2);

        System.out.println(bigInt.multiply(bigInt2).toString(16));
        System.out.println(num1.multiply(num2).toString());
    }

    private static void testAdd() {
        BigInteger bigInt = new BigInteger("ab230504045969", 16);
        BigInteger bigInt2 = new BigInteger("12ab45002300", 16);
        BigNumber num1 = new BigNumber(bigInt);
        BigNumber num2 = new BigNumber(bigInt2);

        System.out.println(bigInt.mod(bigInt2).toString(16));
        System.out.println(num1.mod(num2).toString());
    }

    private static void testMod() {
        BigInteger bigInt = new BigInteger("5792734391", 10);
        BigInteger bigInt2 = new BigInteger("3958138", 10);
        BigNumber num1 = new BigNumber(bigInt);
        BigNumber num2 = new BigNumber(bigInt2);

        System.out.println(bigInt.mod(bigInt2).toString(16));
        System.out.println(num1.mod(num2).toString());
    }

    private static void testPowerAndMod() {
        BigInteger bigInt = new BigInteger("266b", 16);
        BigInteger bigInt2 = new BigInteger("ffff", 16);
        BigInteger bigInt3 = new BigInteger("7908163ce7d64e5f383f54f4ab2ae8e232ce7d5985540ca041dfed3111e54cd9", 16);
        BigNumber num1 = new BigNumber(bigInt);
        BigNumber num2 = new BigNumber(bigInt2);
        BigNumber num3 = new BigNumber(bigInt3);

        System.out.println(bigInt.modPow(bigInt2, bigInt3).toString(16));
        System.out.println(num1.powerAndMod(num2, num3).toString());
    }

    private static void testDivide() {
        BigInteger bigInt = new BigInteger("100ab40ab0fffffff04", 16);
        BigInteger bigInt2 = new BigInteger("1fff0000fcc444cbccfffc", 16);
        BigNumber num = new BigNumber(bigInt);
        BigNumber num2 = new BigNumber(bigInt2);

        BigInteger[] bigIntRes = bigInt.divideAndRemainder(bigInt2);

        System.out.println(bigIntRes[0].toString(16));
        System.out.println(bigIntRes[1].toString(16));
        BigNumber[] bigNumRes = num.divide(num2);

        System.out.println(bigNumRes[0].toString());
        System.out.println(bigNumRes[1].toString());

    }

    private static void testMinus() {
        BigInteger bigInt = new BigInteger("f0010000f00000000", 16);
        BigInteger bigInt2 = new BigInteger("abcdef", 16);
        BigNumber num = new BigNumber(bigInt);
        BigNumber num2 = new BigNumber(bigInt2);
        System.out.println(bigInt.subtract(bigInt2).toString(16));
        System.out.println(num.minus(num2).toString());
    }

    private static void testShift() {
        BigInteger bigInt = new BigInteger("ab902cfe9354234535421954024bb0", 16);
        BigNumber num = new BigNumber(bigInt);
        System.out.println(bigInt.shiftRight(35).toString(16));
        System.out.println(num.shiftRight(35).toString());
    }

    private static void testMontgomery() {
        BigInteger a = new BigInteger("12428947334");
        BigInteger b = new BigInteger("1945456783");
        BigInteger n = new BigInteger("2522453343759");

        BigNumber aa = new BigNumber(a);
        BigNumber bb = new BigNumber(b);
        BigNumber nn = new BigNumber(n);

        System.out.println(a.modPow(b, n).toString(16));
        System.out.println(aa.montExp(bb, nn));
    }

    public static void main(String[] args) {
        testMontgomery();
        testMultiply();
        testAdd();
        testMod();
        testShift();
        testMinus();
        testDivide();
        testPowerAndMod();
    }

}
