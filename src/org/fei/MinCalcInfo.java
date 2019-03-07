package org.fei;

public class MinCalcInfo {
    Integer wordNumber;
    Integer sum;

    public MinCalcInfo(Integer wordNumber, Integer sum) {
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
