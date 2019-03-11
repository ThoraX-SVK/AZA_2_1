package org.fei;

public class MinCalcInfo {
    Integer wordNumber;
    Double sum;

    public MinCalcInfo(Integer wordNumber, Double sum) {
        this.wordNumber = wordNumber;
        this.sum = sum;
    }

    @Override
    public String toString() {
        return "MinCalcInfo{" +
                "wordNumber=" + wordNumber +
                ", sum=" + sum +
                '}';
    }
}
