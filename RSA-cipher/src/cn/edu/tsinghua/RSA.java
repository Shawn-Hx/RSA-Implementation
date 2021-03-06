package cn.edu.tsinghua;


public class RSA {

    private final Keys RSA_KEYS;
    private final int BITS;
    private final int ENCRYPT_BYTES;

    public RSA(int bits, long e) {
        this.RSA_KEYS = KeyGenerator.generate(bits >> 1, e);
        this.BITS = bits;
        this.ENCRYPT_BYTES = (bits >> 3) - 1;
    }

    public String encrypt(String plainText) {
        if (plainText.length() == 0)
            return "";

        StringBuilder sb = new StringBuilder();
        BigNumber e = RSA_KEYS.getE();
        BigNumber n = RSA_KEYS.getN();
        byte[] bytes = plainText.getBytes();
        int offset = bytes.length % ENCRYPT_BYTES;
        if (offset != 0) {
            BigNumber m = BigNumber.valueOf(bytes, 0, offset - 1);
            sb.append(m.montExp(e, n).toString(BITS));
        }
        while (offset < bytes.length) {
            BigNumber m = BigNumber.valueOf(bytes, offset, offset + ENCRYPT_BYTES - 1);
            sb.append(m.montExp(e, n).toString(BITS));
            offset += ENCRYPT_BYTES;
        }
        assert offset == bytes.length;
        return sb.toString();
    }

    public String decrypt(String cipherText) {
        if (cipherText.length() == 0)
            return "";

        int hexBits = BITS >> 2;
        BigNumber d = RSA_KEYS.getD();
        BigNumber n = RSA_KEYS.getN();

        BigNumber m = new BigNumber(cipherText.substring(0, hexBits));
        BigNumber origin = m.montExp(d, n);
        byte[] originBytes = origin.toBytes();
        byte[] bytes = new byte[originBytes.length + (cipherText.length() / hexBits - 1) * ENCRYPT_BYTES];
        System.arraycopy(originBytes, 0, bytes, 0, originBytes.length);

        int count = originBytes.length;

        for (int i = hexBits; i < cipherText.length(); i += hexBits) {
            m = new BigNumber(cipherText.substring(i, i + hexBits));
            origin = m.montExp(d, n);
            originBytes = origin.toBytes(ENCRYPT_BYTES << 3);
            for (byte originByte : originBytes) {
                bytes[count++] = originByte;
            }
        }
        assert count == bytes.length;
        return new String(bytes);
    }

    public String getP() {
        return RSA_KEYS.getP().toString();
    }

    public String getQ() {
        return RSA_KEYS.getQ().toString();
    }

    public String getN() {
        return RSA_KEYS.getN().toString();
    }

    public String getD() {
        return RSA_KEYS.getD().toString();
    }

}
