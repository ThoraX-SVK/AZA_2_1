package org.fei;

public class MatrixEntry {

    public double q;
    Integer wordNumber;
    Integer cost;

    public MatrixEntry(Integer wordNumber, Integer cost) {
        this.wordNumber = wordNumber;
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "MatrixEntry{" +
                "q=" + q +
                ", wordNumber=" + wordNumber +
                ", cost=" + cost +
                '}';
    }
}
