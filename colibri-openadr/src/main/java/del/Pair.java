package del;

public class Pair<A, B> {
    private A fst;
    private B snd;

    public Pair(A fst, B snd) {
        this.fst = fst;
        this.snd = snd;
    }

    public A getFst() {
        return fst;
    }

    public B getSnd() {
        return snd;
    }

    public void setFst(A a) {
        this.fst = a;
    }

    public void setSnd(B b) {
        this.snd = b;
    }

    public String toString(){
        return "fst: " + fst + " snd: " + snd;
    }
}
