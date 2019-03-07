package org.fei;

import java.util.ArrayList;
import java.util.List;

public class Matrix {

    List<List<MatrixEntry>> matrix;

    Matrix(int size) {

        matrix = new ArrayList<>();

        for (int i = 0; i < size; i++) {

            List<MatrixEntry> row = new ArrayList<>();

            for (int j = 0; j < size; j++) {
                row.add(new MatrixEntry(null, null));
            }
            matrix.add(row);
        }
    }

    @Override
    public String toString() {
        return "Matrix{" +
                "matrix=" + matrix +
                '}';
    }
}
