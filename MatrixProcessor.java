package processor;
import java.util.Scanner;
import java.util.Stack;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. Add matrices");
            System.out.println("2. Multiply matrix to a constant");
            System.out.println("3. Multiply matrices");
            System.out.println("4. Transpose matrix");
            System.out.println("5. Calculate determinant");
            System.out.println("0. Exit");
            System.out.println("Your choice: ");
            int choice = scanner.nextInt();
            if (choice == 0) {
                break;
            } else {
                switch (choice) {
                    case 1:
                        double[][] matrix1 = buildMatrix(scanner);
                        double[][] matrix2 = buildMatrix(scanner);
                        if (matrix1.length == matrix2.length && matrix1[0].length == matrix2[0].length) {
                            double[][] matrixSum = new double[matrix1.length][matrix1[0].length];
                            for (int i = 0; i < matrix1.length; i++) {
                                for (int j = 0; j < matrix1[0].length; j++) {
                                    matrixSum[i][j] = matrix1[i][j] + matrix2[i][j];
                                }
                            }
                            printMatrix(matrixSum);
                        } else {
                            System.out.print("ERROR");
                        }
                        break;
                    case 2:
                        double[][] matrix = buildMatrix(scanner);
                        double c = scanner.nextDouble();
                        for (int i = 0; i < matrix.length; i++) {
                            for (int j = 0; j < matrix[0].length; j++) {
                                matrix[i][j] = matrix[i][j] * c;
                            }
                        }
                        printMatrix(matrix);
                        break;
                    case 3:
                        double[][] matrixA = buildMatrix(scanner);
                        double[][] matrixB = buildMatrix(scanner);
                        double[][] matrixBT = new double[matrixB[0].length][matrixB.length];
                        for (int i = 0; i < matrixB.length; i++) {
                            for (int j = 0; j < matrixB[0].length; j++) {
                                matrixBT[j][i] = matrixB[i][j];
                            }
                        }
                        if (matrixA[0].length == matrixB.length) {
                            double[][] matrixProduct = new double[matrixA.length][matrixB[0].length];
                            for (int i = 0; i < matrixA.length; i++) {
                                double[] row = matrixA[i];
                                for (int j = 0; j < matrixB[0].length; j++) {
                                    double[] column = matrixBT[j];
                                    double product = 0;
                                    for (int k = 0; k < row.length; k++) {
                                        product += row[k] * column[k];
                                    }
                                    matrixProduct[i][j] = product;
                                }
                            }
                            printMatrix(matrixProduct);
                        } else {
                            System.out.print("ERROR");
                        }
                        break;
                    case 4:
                        System.out.println("1. Main diagonal");
                        System.out.println("2. Side diagonal");
                        System.out.println("3. Vertical line");
                        System.out.println("4. Horizontal line");
                        System.out.println("Your choice: ");
                        int transposeType = scanner.nextInt();
                        switch (transposeType) {
                            case 1:
                                double[][] matrixP = buildMatrix(scanner);
                                double[][] matrixPT = transposeMatrix(matrixP);
                                printMatrix(matrixPT);
                                break;
                            case 2:
                                double[][] matrixQ = buildMatrix(scanner);
                                double[][] matrixQhT = new double[matrixQ.length][matrixQ[0].length];
                                for (int i = 0; i < matrixQ.length; i++) {
                                    matrixQhT[i] = matrixQ[matrixQ.length - 1 - i];
                                }
                                double[][] matrixQvT = transposeMatrix(matrixQhT);
                                double[][] matrixQsdT = new double[matrixQvT.length][matrixQvT[0].length];
                                for (int i = 0; i < matrixQvT.length; i++) {
                                    matrixQsdT[i] = matrixQvT[matrixQvT.length - 1 - i];
                                }
                                printMatrix(matrixQsdT);
                                break;
                            case 3:
                                double[][] matrixR = buildMatrix(scanner);
                                double[][] matrixRT = transposeMatrix(matrixR);
                                double[][] matrixRThT = new double[matrixRT.length][matrixRT[0].length];
                                for (int i = 0; i < matrixRT.length; i++) {
                                    matrixRThT[i] = matrixRT[matrixRT.length - 1 - i];
                                }
                                double[][] matrixRvT = transposeMatrix(matrixRThT);
                                printMatrix(matrixRvT);
                                break;
                            case 4:
                                double[][] matrixS = buildMatrix(scanner);
                                double[][] matrixShT = new double[matrixS.length][matrixS[0].length];
                                for (int i = 0; i < matrixS.length; i++) {
                                    matrixShT[i] = matrixS[matrixS.length - 1 - i];
                                }
                                printMatrix(matrixShT);
                                break;
                        }
                        break;
                    case 5:
                        double[][] matrixU = buildMatrix(scanner);
                        System.out.println(calculateDeterminant(matrixU));
                }
            }
        }
    }
    public static double[][] buildMatrix(Scanner scanner) {
        System.out.println("Enter matrix size: ");
        int m = scanner.nextInt();
        int n = scanner.nextInt();
        System.out.println("Enter matrix: ");
        double[][] matrix = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = scanner.nextDouble();
            }
        }
        return matrix;
    }

    public static void printMatrix(double[][] matrix) {
        System.out.println("Result: ");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static double[][] transposeMatrix(double[][] matrix) {
        double[][] matrixT = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrixT[j][i] = matrix[i][j];
            }
        }
        return matrixT;
    }

    public static double calculateDeterminant(double [][] matrix) {
        if (matrix[0].length == 1) {
            return matrix[0][0];
        } else if (matrix[0].length == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        } else {
            double determinant = 0;
            for (int j = 0; j < matrix[0].length; j++) {
                double sign = Math.pow(-1, j + 2);

                double[][] minor = new double[matrix.length - 1][matrix[0].length - 1];
                Stack<Double> minorElements = new Stack<Double>();
                for (int p = 0; p < matrix.length; p++) {
                    for (int q = 0; q < matrix[0].length; q++) {
                        if (p != 0 ^ q != j) {
                            minorElements.push(matrix[p][q]);
                        }
                    }
                }
                for (int x = minor.length - 1; x >= 0; x--) {
                    for (int y = minor[0].length - 1; y >= 0; y--) {
                        minor[x][y] = minorElements.pop();
                    }
                }

                determinant += matrix[0][j] * calculateDeterminant(minor) * sign;
            }
            return determinant;
        }
    }
}
