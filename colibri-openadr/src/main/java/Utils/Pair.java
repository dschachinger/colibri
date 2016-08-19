package Utils;

/**
 * This class is used to combine two objects in one object.
 * @param <A> Type of the first object
 * @param <B> Type of the second object
 */
public class Pair<A, B> {
    // First object
    private A fst;
    // Second object
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
