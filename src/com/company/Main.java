package com.company;

/*
 * @author: Кожуховский Максим
 * mail: m.kozhukhovskii@g.nsu.ru
 * */

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Stack;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(Path.of("tests.txt"), StandardCharsets.UTF_8);
        PrintWriter out = new PrintWriter("out.txt", StandardCharsets.UTF_8);
        int n;
        n = in.nextInt();

        for (int play = 1; play <= n; ++play) {
            game(in, out, play);
        }
        out.flush();
    }

    /*
    * Метод игры, выводит номер игры, все шаги и количество очков
    * @param in Поток ввода
    * @param out Поток вывода
    * @param play номер игры */
    public static void game(Scanner in, PrintWriter out, int play) {
        String symbol;
        short[][] arr = new short[10][15];
        Cluster largest;
        int points;
        int score = 0;
        int balls_remaining = 0;

        for (int i = 0; i < 10; ++i) {
            symbol = in.next();
            for (int j = 0; j < 15; ++j) {
                switch (symbol.charAt(j)) {
                    case 'R' -> arr[i][j] = 1;
                    case 'B' -> arr[i][j] = 2;
                    case 'G' -> arr[i][j] = 3;
                }
            }
        }

        out.write("Game " + play + ":\n");

        largest = detectLarge(arr);

        while (largest.am_balls > 1 && arr[largest.x][largest.y] != 0) {
            points = (largest.am_balls - 2) * (largest.am_balls - 2);

            out.write("Move at (" + (10 - largest.x) + "," + (largest.y + 1) +
                    ") : removed " + largest.am_balls + " balls of color ");

            if (arr[largest.x][largest.y] == 1) out.write("R");
            else if (arr[largest.x][largest.y] == 2) out.write("B");
            else if (arr[largest.x][largest.y] == 3) out.write("G");
            out.write(", got " + points + ".\n");

            removing(arr, largest.x, largest.y);
            shiftDown(arr);
            shiftLeft(arr);

            score += points;

            largest = detectLarge(arr);
        }

        if (arr[largest.x][largest.y] == 0)
            score += 1000;

        for (int i = 0; i < 10; ++i)
            for (int j = 0; j < 15; ++j)
                if (arr[i][j] != 0) ++balls_remaining;
        out.write("Final score: " + score + ", with " + balls_remaining + " balls remaining.\n");
    }

    /*
    * Метод который помечает "проверен" кластер на доске
    * @param arr Текущая доска
    * @param x Координата яцейки кластера, который нужно пометить
    * @param y Координата яцейки кластера, который нужно пометить
    * @param curr_check доска проверки, в которую записыватся результат*/
    public static void labeling(short[][] arr, Integer x, Integer y, boolean[][] curr_check) {
        Stack<Integer> pX = new Stack<>();
        Stack<Integer> pY = new Stack<>();

        pX.push(x);
        pY.push(y);

        while (!pX.isEmpty()) {
            x = pX.peek();
            y = pY.peek();
            pX.pop();
            pY.pop();
            curr_check[x][y] = true;

            if (x + 1 < 10 && !curr_check[x + 1][y])
                if (arr[x + 1][y] == arr[x][y]) {
                    pX.push(x + 1);
                    pY.push(y);
                }

            if (y + 1 < 15 && !curr_check[x][y + 1])
                if (arr[x][y + 1] == arr[x][y]) {
                    pX.push(x);
                    pY.push(y + 1);
                }

            if (x - 1 >= 0 && !curr_check[x - 1][y])
                if (arr[x - 1][y] == arr[x][y]) {
                    pX.push(x - 1);
                    pY.push(y);
                }

            if (y - 1 >= 0 && !curr_check[x][y - 1])
                if (arr[x][y - 1] == arr[x][y]) {
                    pX.push(x);
                    pY.push(y - 1);
                }
        }
    }

    /*
    * Метод, который подсчитывает количество шаров в кластере
    * @param arr доска
    * @param curr кластер, в котором подсчитываются шары
    * */
    public static void calculate(short[][] arr, Cluster curr) {
        boolean[][] curr_check = new boolean[10][15];

        for (int i = 0; i < 10; ++i)
            for (int j = 0; j < 15; ++j)
                curr_check[i][j] = false;

        labeling(arr, curr.x, curr.y, curr_check);

        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 15; ++j) {
                if (curr_check[i][j]) curr.am_balls += 1;
            }
        }
    }

    /*
    * Метод, который определяет наибольший кластер на доске
    * @param arr доска, в данный момент времени
    * @return Cluster наибольший кластер
    * */
    public static Cluster detectLarge(short[][] arr) {
        Cluster max = new Cluster(9, 0, 0);
        Cluster curr;

        boolean[][] checked = new boolean[10][15];                                                   //провереные шары

        for (int j = 0; j < 15; ++j)
            for (int i = 9; i >= 0; --i)
                if (!checked[i][j] && arr[i][j] != 0) {
                    curr = new Cluster(i, j, 0);

                    calculate(arr, curr);

                    labeling(arr, curr.x, curr.y, checked);

                    if (curr.am_balls > max.am_balls) max = curr;
                }

        return max;
    }

    /*
    * Метод, который сдвигает все элементы доски влево по правилам игры
    * @param arr доска в нынешний момент
    * */
    public static void shiftLeft(short arr[][]) {
        boolean[] column_removed = new boolean[15];

        int k;

        for (int j = 0; j < 15; ++j) {
            column_removed[j] = true;
            for (int i = 9; i >= 0; --i) {
                if (arr[i][j] != 0) {
                    column_removed[j] = false;
                    break;
                }
            }
        }

        for (int j = 0; j < 14; ++j)
            if (column_removed[j]) {
                k = j + 1;
                while (column_removed[k] && k < 14) ++k;
                column_removed[k] = true;
                for (int i = 0; i < 10; ++i) {
                    arr[i][j] = arr[i][k];
                    arr[i][k] = 0;
                }
            }
    }

    /*
     * Метод, который сдвигает все элементы доски вниз по правилам игры
     * @param arr доска в нынешний момент
     * */
    public static void shiftDown(short arr[][]) {
        int k;
        for (int j = 0; j < 15; ++j) {
            for (int i = 9; i > 0; --i) {
                k = i - 1;
                if (arr[i][j] == 0) {
                    while (arr[k][j] == 0 && k > 0) --k;
                    arr[i][j] = arr[k][j];
                    arr[k][j] = 0;
                }
            }
        }
    }

    /*
     * Метод, который удаляет кластер
     * @param arr доска в нынешний момент
     * @param x координаты
     * @param y координаты
     * */
    public static void removing(short arr[][], int x, int y) {
        boolean curr_check[][] = new boolean[10][15];
        for (int i = 0; i < 10; ++i)
            for (int j = 0; j < 15; ++j)
                curr_check[i][j] = false;

        labeling(arr, x, y, curr_check);

        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 15; ++j) {
                if (curr_check[i][j]) arr[i][j] = 0;
            }
        }
    }
}
