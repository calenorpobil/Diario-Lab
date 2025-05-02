package com.merlita.diariolab.Modelos;

public class Pareja<L,R> {

    private final L left;
    private final R right;

    public Pareja(L left, R right) {
        assert left != null;
        assert right != null;

        this.left = left;
        this.right = right;
    }

    public L getLeft() { return left; }
    public R getRight() { return right; }

    @Override
    public int hashCode() { return left.hashCode() ^ right.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pareja)) return false;
        Pareja pairo = (Pareja) o;
        return this.left.equals(pairo.getLeft()) &&
                this.right.equals(pairo.getRight());
    }

}
