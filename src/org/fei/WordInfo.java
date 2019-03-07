package org.fei;

public class WordInfo {

    Integer wordNumber;
    String word;
    Integer frequency;

    public WordInfo(String word, Integer frequency) {
        this.word = word;
        this.frequency = frequency;
    }

    public void setWordNumber(Integer wordNumber) {
        this.wordNumber = wordNumber;
    }

    @Override
    public String toString() {
        return "WordInfo{" +
                "word='" + word + '\'' +
                ", frequency=" + frequency +
                '}';
    }
}
