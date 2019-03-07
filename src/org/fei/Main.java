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
    private static Node tree;

    public static void main(String[] args) throws IOException {

        constructTree();
        pocet_porovnani("and");

        int i = 42;
    }

    private static Integer pocet_porovnani(String slovo) {
        Integer porovani = tree.find(slovo);

        System.out.println("Na najdenie slova '" + slovo + "' bolo potrebných " + porovani + " porovnani!");

        return porovani;
    }

    private static void constructTree() throws IOException {
        words = new ArrayList<>();

        //Prechádzame riadok po riadku
        try (Stream<String> stream = Files.lines(Paths.get("dictionary.txt"))) {
            stream.forEach(line -> {
                String[] parts = line.split(" ");

                Integer frequency = Integer.parseInt(parts[0]);
                String name = parts[1];

                if(frequency > 50000) {
                    //Pridáme do zoznamu slov s ktorými budeme pracovať
                    words.add(new WordInfo(name, frequency));
                }
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

        //Vytvoríme maticu v ktorej budeme počítať, cenu za najnižšie zostrojenie stromu a podstromu
        Matrix matrix = new Matrix(words.size() + 1);

        //Začneme postupne vypĺňať diagonály
        IntStream.range(0,matrix.matrix.size())
                .forEach(index -> fillMatrixDiagonal(index, matrix));


        //Root je element, ktorý bol v poslednom stĺpci matice
        MatrixEntry root = matrix.matrix.get(0).get(words.size());

        tree = new Node(words.get(root.wordNumber - 1).word);

        Queue<Pair> queue = new ArrayDeque<>();

        //Ľavý a pravý subStrom problém
        queue.add(new Pair(0, root.wordNumber - 1));
        queue.add(new Pair(root.wordNumber, words.size()));

        do {
            Pair processing = queue.poll();

            MatrixEntry actual = matrix.matrix.get(processing.first).get(processing.second);
            tree.add(words.get(actual.wordNumber - 1).word);

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

        if(startAtColumn == 0) {
            //Špeciálny prípad hlavnej diagonály, cost je vtedy všade nula
            fillMainDiagonalWithZeroes(matrix);
            return;
        }

        //Získame x,y dvojice v matici ktoré prislúchajú diagonále ktorú práve spracúvavame
        List<Pair> pairsToBeLookedAt = constructPairsWeAreLookingFor(startAtColumn, matrix.matrix.size());

        if(startAtColumn == 1) {
            //Špeciálny prípad, napĺňame diagonálu frekvenciou daného slova
            pairsToBeLookedAt.forEach(pair -> {

                //Stĺpec hovorí o tom, ktoré slovo používame
                Integer wordNumber = pair.second;
                Integer indexInWordsList = wordNumber - 1;
                WordInfo actualWord = words.get(indexInWordsList);

                matrix.matrix.get(pair.first).get(pair.second).cost = actualWord.frequency;
                matrix.matrix.get(pair.first).get(pair.second).wordNumber = actualWord.wordNumber;
            });
        } else {

            pairsToBeLookedAt.forEach(pair -> {

                //Pre [a,b] nájde všetky slová s poradovým číslom "a+1" až "b" (vrátane)
                List<WordInfo> wordsThatAreRelatedToThisPair = findWordsThatAreRelatedToPair(pair);

                Integer sumOfFrequency = wordsThatAreRelatedToThisPair.stream().mapToInt(value -> value.frequency).sum();

                List<MinCalcInfo> toFindMinimum = new ArrayList<>();

                //Spočítame cost pre každé slovo
                wordsThatAreRelatedToThisPair.forEach(it -> {
                    Pair firtToLookAt = new Pair(pair.first, it.wordNumber - 1);
                    Pair secondToLookAt = new Pair(it.wordNumber, pair.second);

                    Integer firstValue = matrix.matrix.get(firtToLookAt.first).get(firtToLookAt.second).cost;
                    Integer secondValue = matrix.matrix.get(secondToLookAt.first).get(secondToLookAt.second).cost;

                    MinCalcInfo info = new MinCalcInfo(it.wordNumber, firstValue + secondValue);
                    toFindMinimum.add(info);
                });

                //Vyberieme to ktoré má najmänšiu sumu
                toFindMinimum.sort(Comparator.comparingInt(o -> o.sum));
                MinCalcInfo min = toFindMinimum.get(0);

                Integer finalSum = min.sum + sumOfFrequency;
                Integer finalWordNumber = min.wordNumber;

                //Na práve iterované políčko zapíšeme sumu a ktoré slovo je potrebné dať ako root element
                matrix.matrix.get(pair.first).get(pair.second).cost = finalSum;
                matrix.matrix.get(pair.first).get(pair.second).wordNumber = finalWordNumber;
            });
        }
    }

    private static List<WordInfo> findWordsThatAreRelatedToPair(Pair pair) {

        Integer startingWordNumberInclusive = pair.first + 1;
        Integer endingWordNumberInclusive = pair.second;

        List<WordInfo> affected = new ArrayList<>();

        IntStream.rangeClosed(startingWordNumberInclusive, endingWordNumberInclusive)
                .forEach(value -> {
                    affected.add(words.get(value - 1));
                });

        return affected;
    }

    public static void fillMainDiagonalWithZeroes(Matrix matrix) {
        IntStream.range(0,matrix.matrix.size())
                .forEach(index -> {
                    matrix.matrix.get(index).get(index).cost = 0;
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
