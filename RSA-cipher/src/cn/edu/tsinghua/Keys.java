package cn.edu.tsinghua;

public class Keys {

    private BigNumber p;
    private BigNumber q;
    private BigNumber n;
    private BigNumber e;
    private BigNumber d;

    public Keys() {
    }

    public Keys(BigNumber p, BigNumber q, BigNumber n, BigNumber e, BigNumber d) {
        this.p = p;
        this.q = q;
        this.n = n;
        this.e = e;
        this.d = d;
    }

    public BigNumber getP() {
        return p;
    }

    public void setP(BigNumber p) {
        this.p = p;
    }

    public BigNumber getQ() {
        return q;
    }

    public void setQ(BigNumber q) {
        this.q = q;
    }

    public BigNumber getN() {
        return n;
    }

    public void setN(BigNumber n) {
        this.n = n;
    }

    public BigNumber getE() {
        return e;
    }

    public void setE(BigNumber e) {
        this.e = e;
    }

    public BigNumber getD() {
        return d;
    }

    public void setD(BigNumber d) {
        this.d = d;
    }
}
