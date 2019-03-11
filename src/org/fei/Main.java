package org.fei;

import org.fei.tree.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {

    private static List<WordInfo> words;
    private static List<KeyWordInfo> keyWords;

    private static Node tree;
    private static int cumulativeFrequency = 0;

    private static Matrix weightMatrix;

    public static void main(String[] args) throws IOException {

        constructWeightMatrix();
        constructTree();
        pocet_porovnani("must");

        int i = 42;
    }

    private static void constructWeightMatrix() throws IOException {
        words = new ArrayList<>();

        //Prechádzame riadok po riadku
        try (Stream<String> stream = Files.lines(Paths.get("dictionary.txt"))) {
            stream.forEach(line -> {
                String[] parts = line.split(" ");

                Integer frequency = Integer.parseInt(parts[0]);
                String name = parts[1];

                words.add(new WordInfo(name, frequency));
            });
        }

        //Lexikograficky rozadíme (tj. a -> b -> c...)
        words.sort(Comparator.comparing(wordInfo -> wordInfo.word));

        //Očíslujeme každé slovo od 1
        int wordNum = 1;
        for(WordInfo wordInfo : words) {
            wordInfo.setWordNumber(wordNum);
            wordNum++;
        }

        keyWords = new ArrayList<>();

        Integer sumOfFrequencies = words.stream()
                .mapToInt(it -> it.frequency)
                .sum();

        words.forEach(it -> {
            if(it.frequency > 50000) {
                // is key word
                double p = (double) it.frequency / sumOfFrequencies;
                double q = (double) cumulativeFrequency / sumOfFrequencies;
                keyWords.add(new KeyWordInfo(p,q,it.wordNumber, it));

                cumulativeFrequency = 0;
            } else {
                cumulativeFrequency += it.frequency;
            }
        });

        wordNum = 1;
        for(KeyWordInfo wordInfo : keyWords) {
            wordInfo.wordNumber = wordNum;
            wordNum++;
        }

        weightMatrix = new Matrix(keyWords.size());

        fillWeightMatrixDiagonal(0);

        IntStream.rangeClosed(0,weightMatrix.matrix.size()).forEach(index -> {

            int startAtColumn = index + 2;

            for(int i = startAtColumn; i <= weightMatrix.matrix.size(); i++) {

                Double previousQ = weightMatrix.matrix.get(index).get(i - 2).q;

                Integer indexo = i;

                KeyWordInfo keyWordAtDiagonal = keyWords.stream()
                        .filter(it -> it.wordNumber == indexo)
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);

                KeyWordInfo before = keyWords.stream()
                        .filter(it -> it.wordNumber == indexo - 1)
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);

                weightMatrix.matrix.get(index).get(i - 1).q = previousQ + keyWordAtDiagonal.q + before.p;
            }
        });
    }

    private static void fillWeightMatrixDiagonal(int startAtColumn) {

        List<Pair> pairsToBeLookedAt = constructPairsWeAreLookingFor(startAtColumn, weightMatrix.matrix.size());

        if(startAtColumn == 0) {
            pairsToBeLookedAt.forEach(pair -> {

                KeyWordInfo keyWordAtDiagonal = keyWords.stream()
                        .filter(it -> it.wordNumber == pair.second + 1)
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);

                weightMatrix.matrix.get(pair.first).get(pair.second).q = keyWordAtDiagonal.q;
                weightMatrix.matrix.get(pair.first).get(pair.second).wordNumber = keyWordAtDiagonal.wordNumber;
            });
        }
    }

    private static Integer pocet_porovnani(String slovo) {
        Integer porovani = tree.find(slovo);

        System.out.println("Na najdenie slova '" + slovo + "' bolo potrebných " + porovani + " porovnani!");

        return porovani;
    }

    private static void constructTree() {

        //Vytvoríme maticu v ktorej budeme počítať, cenu za najnižšie zostrojenie stromu a podstromu
        Matrix matrix = new Matrix(keyWords.size());

        //Začneme postupne vypĺňať diagonály
        IntStream.range(0,matrix.matrix.size())
                .forEach(index -> fillMatrixDiagonal(index, matrix));

        //Root je element, ktorý bol v poslednom stĺpci matice
        MatrixEntry root = matrix.matrix.get(0).get(keyWords.size() -1);

        String word = keyWords.stream()
                .filter(it -> it.wordNumber == root.wordNumber)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .wordInfo.word;

        tree = new Node(word);

        Queue<Pair> queue = new ArrayDeque<>();

        //Ľavý a pravý subStrom problém
        queue.add(new Pair(0, root.wordNumber - 1));
        queue.add(new Pair(root.wordNumber, keyWords.size() - 1));

        do {
            Pair processing = queue.poll();

            MatrixEntry actual = matrix.matrix.get(processing.first).get(processing.second);

            word = keyWords.stream()
                    .filter(it -> it.wordNumber == actual.wordNumber)
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new)
                    .wordInfo.word;

            tree.add(word);

            //Nový lavý a pravý substrom
            Pair left = new Pair(processing.first, actual.wordNumber - 1);
            Pair right = new Pair(actual.wordNumber, processing.second);

            if(!left.first.equals(left.second)) {
                queue.add(left);
            }

            if(!right.first.equals(right.second)) {
                queue.add(right);
            }

        } while (!queue.isEmpty());
    }

    /**   0   1   2   3   4   5   6
     *   ___ ___ ___ ___ ___ ___ ___
     *  |   | x |   |   |   |   |   |
     *  |___|___|___|___|___|___|___|
     *  |   |   | x |   |   |   |   |
     *  |___|___|___|___|___|___|___|
     *  |   |   |   | x |   |   |   |
     *  |___|___|___|___|___|___|___|
     *  |   |   |   |   | x |   |   |
     *  |___|___|___|___|___|___|___|
     *  |   |   |   |   |   | x |   |
     *  |___|___|___|___|___|___|___|
     *  |   |   |   |   |   |   | x |
     *  |___|___|___|___|___|___|___|
     *  |   |   |   |   |   |   |   |
     *  |___|___|___|___|___|___|___|
     *
     *  Example for 1 as staring column
     *
     * @param startAtColumn
     */
    public static void fillMatrixDiagonal(int startAtColumn, Matrix matrix) {

        //Získame x,y dvojice v matici ktoré prislúchajú diagonále ktorú práve spracúvavame
        List<Pair> pairsToBeLookedAt = constructPairsWeAreLookingFor(startAtColumn, matrix.matrix.size());

        if(startAtColumn == 0) {
            //Špeciálny prípad, napĺňame diagonálu frekvenciou daného slova
            pairsToBeLookedAt.forEach(pair -> {
                //Stĺpec hovorí o tom, ktoré slovo používame
                KeyWordInfo actualWord = keyWords.get(pair.second);

                Double QValue = weightMatrix.matrix.get(pair.first).get(pair.second).q;

                matrix.matrix.get(pair.first).get(pair.second).q = QValue;
                matrix.matrix.get(pair.first).get(pair.second).wordNumber = actualWord.wordNumber;
            });
        } else {
            pairsToBeLookedAt.forEach(pair -> {

                //Pre [a,b] nájde všetky slová s poradovým číslom "a+1" až "b" (vrátane)
                List<KeyWordInfo> wordsThatAreRelatedToThisPair = findWordsThatAreRelatedToPair(pair);

                Double cellWeight = weightMatrix.matrix.get(pair.first).get(pair.second).q;

                List<MinCalcInfo> toFindMinimum = new ArrayList<>();

                wordsThatAreRelatedToThisPair.forEach(it -> {
                    Pair firtToLookAt = new Pair(pair.first, it.wordNumber - 1);
                    Pair secondToLookAt = new Pair(it.wordNumber, pair.second);

                    Double firstValue = matrix.matrix.get(firtToLookAt.first).get(firtToLookAt.second).q;
                    Double secondValue = matrix.matrix.get(secondToLookAt.first).get(secondToLookAt.second).q;

                    MinCalcInfo info = new MinCalcInfo(it.wordNumber, firstValue + secondValue);
                    toFindMinimum.add(info);
                });

                //Vyberieme to ktoré má najmänšiu sumu
                toFindMinimum.sort(Comparator.comparingDouble(o -> o.sum));
                MinCalcInfo min = toFindMinimum.get(0);

//                System.out.println("Start: " + pair.first + " End: " + pair.second + " Min: " + toFindMinimum.get(0).sum);
                Double finalSum = min.sum + cellWeight;
                Integer finalWordNumber = min.wordNumber;

                if(pair.first == 0 && pair.second == 9) {
                    int i = 42;
                }


                //Na práve iterované políčko zapíšeme sumu a ktoré slovo je potrebné dať ako root element
                matrix.matrix.get(pair.first).get(pair.second).q = finalSum;
                matrix.matrix.get(pair.first).get(pair.second).wordNumber = finalWordNumber;
            });
        }
    }

    private static List<KeyWordInfo> findWordsThatAreRelatedToPair(Pair pair) {

        Integer startingWordNumberInclusive = pair.first + 1;
        Integer endingWordNumberInclusive = pair.second;

        List<KeyWordInfo> affected = new ArrayList<>();

        IntStream.rangeClosed(startingWordNumberInclusive, endingWordNumberInclusive)
                .forEach(value -> {
                    affected.add(keyWords.get(value - 1));
                });

        return affected;
    }

    public static void fillMainDiagonalWithZeroes(Matrix matrix) {
        IntStream.range(0,matrix.matrix.size())
                .forEach(index -> {
                    matrix.matrix.get(index).get(index).q = 0;
                });
    }

    private static List<Pair> constructPairsWeAreLookingFor(int startAtColumn, int size) {

        Integer firstNumber = 0;
        List<Pair> pairs = new ArrayList<>();

        for (int i = startAtColumn; i < size; i++) {
            pairs.add(new Pair(firstNumber, i));
            firstNumber++;
        }

        return pairs;
    }




}
