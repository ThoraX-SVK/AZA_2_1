package org.fei;

public class MatrixEntry {

    Integer wordNumber;
    Integer cost;

    public MatrixEntry(Integer wordNumber, Integer cost) {
        this.wordNumber = wordNumber;
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "MatrixEntry{" +
                "wordNumber=" + wordNumber +
                ", cost=" + cost +
                '}';
    }
}
