package cn.edu.tsinghua;


import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class BigNumber implements Comparable<BigNumber> {
    public static long countMulTime = 0, countMulNum = 0;
    public static long countModBigNum = 0, countModBigTime = 0;
    public static long countModSmallNum = 0, countModSmallTime = 0;
    public static long countAddNum = 0, countAddTime = 0;
    public static long countMinusNum = 0, countMinusTime = 0;
    public static long countDivideNum= 0, countDivideTime = 0;
    public static long countShiftRightNum = 0, countShiftRightTime = 0;
    public static long countShiftLeftNum = 0, countShiftLeftTime = 0;
    public static long countInverseNum = 0, countInverseTime = 0;

    private static final int DEFAULT_LENGTH = 64;
    private static final int DEFAULT_LOG_RADIX = 28;    // Must be 4n, 1 <= n <= 7
    private static final int DIGIT_HEX_LENGTH = DEFAULT_LOG_RADIX >> 2;
    private static final long RADIX = 1 << DEFAULT_LOG_RADIX;
    private static final long DIGIT_MASK = (1 << DEFAULT_LOG_RADIX) - 1;

    private int length = DEFAULT_LENGTH;
    private int realLen = 1;
    private long[] number;
    private boolean isPositive = true;

    public static final BigNumber ZERO = new BigNumber();
    public static final BigNumber ONE = BigNumber.valueOf(1);

    public BigNumber() {
        this.number = new long[DEFAULT_LENGTH];
    }

    public BigNumber(int length) {
        this.length = length;
        this.number = new long[length];
    }


    public BigNumber(BigNumber other) {
        this.number = Arrays.copyOf(other.number, other.length);
        this.length = other.length;
        this.realLen = other.realLen;
    }

    private static char[] DECIMAL_TO_HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static BigNumber valueOf(byte[] bytes, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i <= end; i++) {
            sb.append(DECIMAL_TO_HEX[(bytes[i] >> 4) & 0x0F]);
            sb.append(DECIMAL_TO_HEX[bytes[i] & 0x0F]);
        }
        return new BigNumber(sb.toString());
    }

    public BigNumber(String hexStr) {
        this();
        int realLen = 0;
        int i = 0;
        for (int r = hexStr.length() - 1; r >= 0; r -= DIGIT_HEX_LENGTH) {
            int l = Math.max(0, r - DIGIT_HEX_LENGTH + 1);
            number[i] = Long.valueOf(hexStr.substring(l, r + 1), 16);
            i++;
            realLen++;
        }
        this.realLen = realLen;
    }

    public BigNumber(BigInteger bigInt) {
        this(bigInt.toString(16));
    }

    public static BigNumber random(int bitLen, Random random) {
        if (bitLen <= 0)
            return ZERO;
        if (bitLen > DEFAULT_LOG_RADIX * DEFAULT_LENGTH) {
            System.out.println("Bit length should less than " + DEFAULT_LOG_RADIX * DEFAULT_LENGTH + "!");
            bitLen = DEFAULT_LOG_RADIX * DEFAULT_LENGTH;
        }
        BigNumber res = new BigNumber();
        res.realLen = 0;
        for (int i = 0; i < res.length; i++) {
            res.number[i] = random.nextInt((int)RADIX);
            res.realLen++;
            bitLen -= DEFAULT_LOG_RADIX;
            if (bitLen <= 0) {
                bitLen += DEFAULT_LOG_RADIX;
                res.number[i] |= (1 << bitLen - 1); // ensure the highest bit is 1
                res.number[i] &= (1 << bitLen) - 1; // Set the bits higher than bitLen to 0
                break;
            }
        }
        return res;
    }

    public static BigNumber randomOdd(int bitLen, Random random) {
        BigNumber res = random(bitLen, random);
        res.number[0] |= 0x1;   // ensure res is an odd number.
        if (res.number[0] % 5 == 0) {
            res.number[0] -= 2;
        }
        return res;
    }

    // Bound is inclusive
    public static BigNumber random(BigNumber bound, Random random) {
        BigNumber res = new BigNumber(bound.length);
        res.realLen = random.nextInt(bound.realLen + 1);
        for (int i = 0; i < res.realLen; i++) {
            res.number[i] = random.nextInt((int)RADIX);
        }
        if (res.realLen == bound.realLen)
            res.number[res.realLen - 1] = random.nextInt((int)bound.number[bound.realLen - 1]);
        return res;
    }

    public static BigNumber valueOf(long num) {
        BigNumber res = new BigNumber();
        res.number[0] = num & ((1 << DEFAULT_LOG_RADIX) - 1);
        if (num >= 1 << DEFAULT_LOG_RADIX) {
            res.number[1] = num >> DEFAULT_LOG_RADIX;
            res.realLen = 2;
        }
        if (num < 0) {
            res.number[0] = -res.number[0];
            res.isPositive = false;
        }
        return res;
    }

    public long longValue() {
        return number[0];
    }

    public static BigNumber gcd(BigNumber a, BigNumber b) {
        BigNumber tmp;
        while (!b.equalsTo(ZERO)) {
            tmp = a.mod(b);
            a = b;
            b = tmp;
        }
        return a;
    }

    private long montOmega(BigNumber n) {
        long t = 1;
        for (int i = 0; i < DEFAULT_LOG_RADIX; i++) {
            t = t * t & DIGIT_MASK;
            t = t * n.number[0] & DIGIT_MASK;
        }
        return RADIX - t;
    }

    private BigNumber montMul(BigNumber x, BigNumber y, BigNumber n, long w) {
        BigNumber res = new BigNumber(n.length);
        BigNumber u;
        long x0 = x.number[0], yi, r0;
        for (int i = 0; i < n.realLen; i++) {
            yi = i >= y.realLen ? 0 : y.number[i];
            r0 = res.number[0];
            u = BigNumber.valueOf((r0 + yi * x0 & DIGIT_MASK) * w & DIGIT_MASK);
            res = res.add(BigNumber.valueOf(yi).multiply(x).add(u.multiply(n))).shiftRightDigits(1);
        }
        if (res.compareTo(n) > 0) {
            res = res.minus(n);
        }
        return res;
    }

    public BigNumber montExp(BigNumber b, BigNumber n) {
//        BigNumber rho = new BigNumber();
//        rho.realLen = n.realLen + 1;
//        rho.number[n.realLen] = 1;
//        BigNumber rhoSquare = rho.shiftLeftDigits(rho.realLen - 1);
//        rhoSquare = rhoSquare.mod(n);
        BigNumber rhoSquare = new BigNumber((n.realLen << 1) + 1);
        rhoSquare.realLen = rhoSquare.length;
        rhoSquare.number[rhoSquare.realLen - 1] = 1;
        rhoSquare = rhoSquare.mod(n);

        long omega = montOmega(n);
        BigNumber t = montMul(ONE, rhoSquare, n, omega);
        BigNumber a = montMul(this, rhoSquare, n, omega);
        int bBinaryBits = b.getTotalBinaryDigits();
        for (int i = bBinaryBits; i >= 0; i--) {
            t = montMul(t, t, n, omega);
            if (b.getBit(i) == 1)
                t = montMul(t, a, n, omega);
        }
        return montMul(t, ONE, n, omega);
    }

    public int getLowestZeroBits() {
        int res = 0;
        for (int i = 0; i < realLen; i++) {
            if (number[i] == 0) {
                res += DEFAULT_LOG_RADIX;
                continue;
            }
            long mask = 1;
            while (true) {
                if ((number[i] & mask) == 0) {
                    res++;
                    mask <<= 1;
                } else {
                    break;
                }
            }
            break;
        }
        return res;
    }

    private void shiftLeftWithinDigit(int n) {
        if (n == 0)
            return;
        assert n < DEFAULT_LOG_RADIX;
        long carry = 0;
        for (int i = 0; i < realLen; i++) {
            long cur = number[i];
            number[i] = ((cur << n) & DIGIT_MASK) + carry;
            carry = cur >> DEFAULT_LOG_RADIX - n;
        }
        if (carry != 0) {
            realLen += 1;
            if (realLen > length) {
//                System.out.println("Shift left overflow!");
                long[] numbers = new long[realLen + 1];
                System.arraycopy(this.number, 0, numbers, 0, realLen - 1);
                this.length = realLen + 1;
                this.number = numbers;
            }
            number[realLen - 1] = carry;
        }
    }

    private BigNumber shiftLeftDigits(int n) {
        realLen += n;
        long[] numbers;
        if (realLen > length) {
            numbers = new long[realLen + 1];
            this.length = realLen + 1;
        } else {
            numbers = this.number;
        }
        for (int i = realLen - n - 1; i >= 0; i--) {
            numbers[i + n] = this.number[i];
            if (i < n) {
                numbers[i] = 0;
            }
        }
        this.number = numbers;
        return this;
    }

    public BigNumber shiftLeft(int n) {
        long start = System.nanoTime();
        if (n == 0)
            return this;
        int shiftDigit = n / DEFAULT_LOG_RADIX;
        shiftLeftDigits(shiftDigit);
        n %= DEFAULT_LOG_RADIX;
        shiftLeftWithinDigit(n);
        countShiftLeftTime += System.nanoTime() - start;
        countShiftLeftNum ++;
        return this;
    }

    private void shiftRightWithinDigit(int n) {
        assert n < DEFAULT_LOG_RADIX;
        if (n == 0)
            return;
        long mask = (1 << n) - 1;
        number[0] >>= n;
        for (int i = 1; i < realLen; i++) {
            long cur = number[i];
            number[i - 1] += (cur & mask) << (DEFAULT_LOG_RADIX - n);
            number[i] >>= n;
        }
        if (number[realLen - 1] == 0)
            realLen = Math.max(1, realLen - 1);
    }

    private void setToZero() {
        for (int i = 0; i < realLen; i++)
            number[i] = 0;
        realLen = 1;
    }

    private BigNumber shiftRightDigits(int n) {
        if (n == 0)
            return this;
        if (n >= realLen) {
            setToZero();
            return this;
        }
        for (int i = 0; i < realLen - n; i++) {
            number[i] = number[i + n];
        }
        for (int i = realLen - n; i < realLen; i++) {
            number[i] = 0;
        }
        realLen -= n;
        return this;
    }

    public BigNumber shiftRight(int n) {
        long start = System.nanoTime();
        if (n == 0)
            return this;
        int shiftDigit = n / DEFAULT_LOG_RADIX;
        shiftRightDigits(shiftDigit);
        n %= DEFAULT_LOG_RADIX;
        shiftRightWithinDigit(n);
        countShiftRightTime += System.nanoTime() - start;
        countShiftRightNum++;
        return this;
    }

    private int getTotalBinaryDigits() {
        int res = (realLen - 1) * DEFAULT_LOG_RADIX;
        long highestDigit = number[realLen - 1];
        return (int) (Math.log(highestDigit) / Math.log(2)) + 1 + res;
    }

    private int getBit(int n) {
        long num = number[n / DEFAULT_LOG_RADIX];
        n %= DEFAULT_LOG_RADIX;
        return (int)(num >> n) & 1;
    }

    public BigNumber powerAndMod(final BigNumber b, final BigNumber n) {
        BigNumber res = new BigNumber(n.length);
        res.number[0] = 1;
        BigNumber aa = new BigNumber(this);
        int binaryDigits = b.getTotalBinaryDigits();
        for (int i = 0; i < binaryDigits; i++) {
            if (b.getBit(i) == 1) {
                res = res.multiply(aa).mod(n);
            }
            aa = aa.multiply(aa).mod(n);
        }
        return res;
    }

    public int mod(int n) {
        long start = System.nanoTime();
        BigInteger num = BigInteger.valueOf(n);
        BigInteger cur = toBigInteger();
        int res = cur.mod(num).intValue();
        countModSmallTime += System.nanoTime() - start;
        countModSmallNum++;
        return res;
    }

    private void setBit(int n) {
        int digit = n / DEFAULT_LOG_RADIX;
        n %= DEFAULT_LOG_RADIX;
        if (realLen - 1 < digit)
            realLen = digit + 1;
        number[digit] |= 1 << n;
    }

    public BigNumber[] divide(final BigNumber n) {
        if (this.equalsTo(ZERO))
            return new BigNumber[]{new BigNumber(ZERO), new BigNumber(n.length)};
        int compareRes = this.compareTo(n);
        // dividend < divisor
        if (compareRes < 0)
            return new BigNumber[]{new BigNumber(ZERO), new BigNumber(this)};
        // dividend == divisor
        if (compareRes == 0)
            return new BigNumber[]{new BigNumber(ONE), new BigNumber(n.length)};
        long start = System.nanoTime();
        BigNumber remainder = new BigNumber(this);
        BigNumber quotient = new BigNumber(this.length);
        BigNumber divisor = new BigNumber(n.length);
        int nBinaryBits = n.getTotalBinaryDigits();
        while (remainder.compareTo(n) > 0) {
            copy(divisor, n);
            BigNumber curQuotient = new BigNumber();
            int shiftBits = remainder.getTotalBinaryDigits() - nBinaryBits;
            divisor.shiftLeft(shiftBits);
            if (remainder.compareTo(divisor) < 0) {
                divisor.shiftRight(1);
                curQuotient.setBit(shiftBits - 1);
            } else {
                curQuotient.setBit(shiftBits);
            }
            quotient = quotient.add(curQuotient);
            remainder = remainder.minus(divisor);
        }
        remainder.isPositive = this.isPositive == n.isPositive;
        countDivideTime += System.nanoTime() - start;
        countDivideNum ++;
        return new BigNumber[]{quotient, remainder};
    }

    private void copy(BigNumber a, BigNumber b) {
        a.realLen = b.realLen;
        a.isPositive = b.isPositive;
        for (int i = 0; i < b.realLen; i++) {
            a.number[i] = b.number[i];
        }
    }

    public BigNumber mod(final BigNumber n) {
        if (!this.isPositive || !n.isPositive)
            System.out.println("Mod warning, needs two positive numbers");
        int compareRes = this.compareTo(n);
        if (compareRes < 0)
            return new BigNumber(this);
        if (compareRes == 0)
            return new BigNumber(ZERO);
        long start = System.nanoTime();
        int nBinaryBits = n.getTotalBinaryDigits();
        BigNumber remainder = new BigNumber(this);
        BigNumber divisor = new BigNumber(this.length);
        while (remainder.compareTo(n) > 0) {
            copy(divisor, n);
            int shiftBits = remainder.getTotalBinaryDigits() - nBinaryBits;
            divisor.shiftLeft(shiftBits);
            if (remainder.compareTo(divisor) < 0) {
                divisor.shiftRight(1);
            }
            remainder = remainder.minus(divisor);
        }
        countModBigTime += System.nanoTime() - start;
        countModBigNum ++;
        return remainder;
    }

    public static BigNumber[] extendEuclidean(BigNumber a, BigNumber b) {
        if (b.equalsTo(ZERO))
            return new BigNumber[]{ONE, ZERO, a};
        BigNumber[] res = extendEuclidean(b, a.mod(b));
        BigNumber oldX = res[0], oldY = res[1];
        res[0] = oldY;
        BigNumber temp = a.divide(b)[0].multiply(oldY);
        if (oldX.isPositive != temp.isPositive) {
            temp.isPositive = oldX.isPositive;
            res[1] = oldX.add(temp);
        } else {
            boolean sign = oldX.isPositive;
            int compareRes = oldX.compareTo(temp);
            if (compareRes > 0) {
                res[1] = oldX.minus(temp);
                res[1].isPositive = sign;
            } else if (compareRes < 0) {
                res[1] = temp.minus(oldX);
                res[1].isPositive = !sign;
            } else {
                res[1] = new BigNumber(oldX.length);
            }
        }
        return res;
    }

//    public static BigInteger[] extendEuclideanByAPI(BigInteger a, BigInteger b) {
//        if (b.equals(BigInteger.ZERO)) {
//            return new BigInteger[]{BigInteger.ONE, BigInteger.ZERO, a};
//        }
//        BigInteger[] res = extendEuclideanByAPI(b, a.mod(b));
//        BigInteger oldX = res[0], oldY = res[1];
//        res[0] = oldY;
//        res[1] = oldX.subtract(a.divide(b).multiply(oldY));
//        return res;
//    }

//    public BigNumber modInverseByAPI(BigNumber n) {
//        BigInteger e = this.toBigInteger();
//        BigInteger nn = n.toBigInteger();
//        BigInteger[] res = extendEuclideanByAPI(this.toBigInteger(), n.toBigInteger());
//        if (!res[2].equals(BigInteger.ONE)) {
//            System.out.println("Inverse doesn't exists");
//            return null;
//        }
//        return new BigNumber(res[0].mod(nn));
//    }

    public BigNumber modInverse(final BigNumber n) {
        long start = System.nanoTime();
        BigNumber[] results = extendEuclidean(new BigNumber(this), new BigNumber(n));
        if (!results[2].equalsTo(ONE)) {
            System.out.println("Mod inverse doesn't exists");
            return null;
        }
        BigNumber res = results[0];
        if (!res.isPositive) {
            res.isPositive = true;
            while (res.compareTo(n) > 0) {
                res = res.minus(n);
            }
            return n.minus(res);
        }
        BigNumber result = res.mod(n);
        countInverseTime += System.nanoTime() - start;
        countInverseNum ++;
        return result;
    }

    public BigNumber minusOne() {
        if (!this.isPositive) {
            System.out.println("Minus one warning!");
        }
        BigNumber res = new BigNumber(this);
        for (int i = 0; i < realLen; i++) {
            if (res.number[i] > 0) {
                res.number[i] -= 1;
                if (i == realLen - 1 && res.number[i] == 0)
                    realLen = Math.max(1, realLen - 1);
                return res;
            } else {
                res.number[i] = RADIX - 1;
            }
        }
        System.out.println("Zero cannot minus one!");
        return new BigNumber(ZERO);
    }

    public BigNumber minus(final BigNumber n) {
        if (this.compareTo(n) < 0) {
            System.out.println("Minus error!");
            return null;
        }
        if (isPositive != n.isPositive)
            System.out.println("Minus warning!");
        long start = System.nanoTime();
        BigNumber res = new BigNumber(this);
        res.realLen = realLen;
        res.isPositive = isPositive;
        for (int i = 0; i < realLen; i++) {
            long a = res.number[i];
            long b = i >= n.length ? 0 : n.number[i];
            if (a < b) {
                a += RADIX;
                res.number[i + 1] -= 1;
            }
            res.number[i] = a - b;
        }
        for (int i = realLen - 1; i > 0; i--) {
            if (res.number[i] == 0) {
                res.realLen--;
            } else {
                break;
            }
        }
        countMinusTime += System.nanoTime() - start;
        countMinusNum++;
        return res;
    }

    public BigNumber add(final BigNumber addend) {
        long start = System.nanoTime();
        if (this.isPositive != addend.isPositive) {
            System.out.println("Add warning!");
        }
        BigNumber res = new BigNumber();
        int maxRealLen = Math.max(realLen, addend.realLen);
        res.realLen = maxRealLen;
        res.isPositive = this.isPositive;
        long c = 0;
        for (int i = 0; i < maxRealLen; i++) {
            long a = i >= length ? 0 : number[i];
            long b = i >= addend.length ? 0 : addend.number[i];
            res.number[i] = a + b + c;
            c = res.number[i] >> DEFAULT_LOG_RADIX;
            res.number[i] &= DIGIT_MASK;
        }
        if (c > 0) {
            if (res.realLen == res.length) {
                System.out.println("Add overflow!");
            } else {
                res.number[realLen] = c;
                res.realLen++;
            }
        }
        countAddTime += System.nanoTime() - start;
        countAddNum++;
        return res;
    }

    public BigNumber multiply(final BigNumber multiplier) {
        long start = System.nanoTime();
        BigNumber res;
        if (realLen + multiplier.realLen - 1 < length)
            res = new BigNumber();
        else
            res = new BigNumber(DEFAULT_LENGTH << 1);
        for (int i = 0; i < realLen; i++) {
            for (int j = 0; j < multiplier.realLen; j++) {
                res.number[i + j] += number[i] * multiplier.number[j];
            }
        }
        int resRealLen = 0;
        for (int i = 0; i < realLen + multiplier.realLen; i++) {
            if (res.number[i] > 0)
                resRealLen = i + 1;
            if (res.number[i] >= RADIX) {
                res.number[i + 1] += res.number[i] >> DEFAULT_LOG_RADIX;
                res.number[i] &= DIGIT_MASK;
            }
        }
        res.realLen = Math.max(1, resRealLen);
        res.isPositive = this.isPositive == multiplier.isPositive;
        countMulTime += System.nanoTime() - start; // count
        countMulNum++;
        return res;
    }

    private BigInteger toBigInteger() {
        return new BigInteger(this.toString(), 16);
    }

    private static final char[] ZEROS = new char[DEFAULT_LOG_RADIX];
    static {
        Arrays.fill(ZEROS, '0');
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Long.toHexString(number[realLen - 1]));
        for (int i = realLen - 2; i >= 0; i--) {
            String tmp = Long.toHexString(number[i]);
            sb.append(ZEROS, 0, DIGIT_HEX_LENGTH - tmp.length()).append(tmp);
        }
        return sb.toString();
    }

    public byte[] toBytes() {
        String hexString = toString();
        if ((hexString.length() & 1) == 1)
            hexString = "0" + hexString;

        byte[] res = new byte[hexString.length() >> 1];
        for (int i = 0; i < hexString.length(); i += 2) {
            res[i >> 1] = Integer.valueOf(hexString.substring(i, i + 2), 16).byteValue();
        }
        return res;
    }

    public byte[] toBytes(int bits) {
        String hexString = toString(bits);
        if ((hexString.length() & 1) == 1)
            hexString = "0" + hexString;

        byte[] res = new byte[hexString.length() >> 1];
        for (int i = 0; i < hexString.length(); i += 2) {
            res[i >> 1] = Integer.valueOf(hexString.substring(i, i + 2), 16).byteValue();
        }
        return res;
    }

    public String toString(int bits) {
        StringBuilder sb = new StringBuilder();
        sb.append(toString());
        int hexBits = bits >> 2;
        while (sb.length() < hexBits) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    public boolean equalsTo(BigNumber other) {
        return this.compareTo(other) == 0;
    }

    @Override
    public int compareTo(final BigNumber o) {
        if (realLen != o.realLen) {
            return realLen > o.realLen ? 1 : -1;
        }
        for (int i = realLen - 1; i >= 0; i--) {
            if (number[i] > o.number[i])
                return 1;
            if (number[i] < o.number[i])
                return -1;
        }
        return 0;
    }
}
