package org.fei.tree;

public class Node {

    public String data;
    public Node left;
    public Node right;

    public Node(String data) {
        this.data = data;
    }

    public Node() {

    }

    private void addLeft(String insert) {

        if(this.left == null) {
            Node left = new Node();
            left.data = insert;
            this.left = left;
        } else {
            this.left.add(insert);
        }
    }

    private void addRight(String insert) {
        if(this.right == null) {
            Node right = new Node();
            right.data = insert;
            this.right = right;
        } else {
            this.right.add(insert);
        }
    }

    public void add(String string) {

        if(string == null) {
            return;
        }

        if(string.compareTo(data) <= 0) {
            addLeft(string);
        } else {
            addRight(string);
        }
    }

    public Integer find(String toFind) {
        return findRecursive(0,toFind);
    }

    private Integer findRecursive(Integer numberOfComparations, String toFind) {

        if(data == null) {
            return  numberOfComparations;
        }

        if(data.equals(toFind)) {
            return numberOfComparations + 1;
        } else {

            if(toFind.compareTo(data) <= 0) {

                if(this.left == null) {
                    return numberOfComparations;
                }

                return this.left.findRecursive(numberOfComparations + 1, toFind);
            } else {

                if(this.right == null) {
                    return numberOfComparations;
                }

                return this.right.findRecursive(numberOfComparations + 1, toFind);
            }
        }




    }



}
