package game.caro.helper;

import com.badlogic.gdx.utils.Array;

public class GameLogic {

    public static boolean isBoardFull(int[][] boardState, int size) {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (boardState[r][c] == 0)
                    return false;
            }
        }
        return true;
    }

    public static boolean checkWin(int[][] boardState, int row, int col, int mark, int boardSize) {
        return checkDirection(boardState, row, col, mark, 1, 0, boardSize) ||
                checkDirection(boardState, row, col, mark, 0, 1, boardSize) ||
                checkDirection(boardState, row, col, mark, 1, 1, boardSize) ||
                checkDirection(boardState, row, col, mark, 1, -1, boardSize);
    }

    private static boolean checkDirection(int[][] boardState, int row, int col, int mark, int dx, int dy,
            int boardSize) {
        int count = 1;
        int r = row + dy, c = col + dx;
        while (isValid(r, c, boardSize) && boardState[r][c] == mark) {
            count++;
            r += dy;
            c += dx;
        }
        r = row - dy;
        c = col - dx;
        while (isValid(r, c, boardSize) && boardState[r][c] == mark) {
            count++;
            r -= dy;
            c -= dx;
        }
        return count >= 5;
    }

    private static boolean isValid(int r, int c, int size) {
        return r >= 0 && r < size && c >= 0 && c < size;
    }

    public static Array<int[][]> getAllLines(int size, int length) {
        Array<int[][]> lines = new Array<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c <= size - length; c++) {
                int[][] line = new int[length][2];
                for (int i = 0; i < length; i++) {
                    line[i][0] = r;
                    line[i][1] = c + i;
                }
                lines.add(line);
            }
        }
        for (int c = 0; c < size; c++) {
            for (int r = 0; r <= size - length; r++) {
                int[][] line = new int[length][2];
                for (int i = 0; i < length; i++) {
                    line[i][0] = r + i;
                    line[i][1] = c;
                }
                lines.add(line);
            }
        }
        for (int r = 0; r <= size - length; r++) {
            for (int c = 0; c <= size - length; c++) {
                int[][] line = new int[length][2];
                for (int i = 0; i < length; i++) {
                    line[i][0] = r + i;
                    line[i][1] = c + i;
                }
                lines.add(line);
            }
        }
        for (int r = 0; r <= size - length; r++) {
            for (int c = length - 1; c < size; c++) {
                int[][] line = new int[length][2];
                for (int i = 0; i < length; i++) {
                    line[i][0] = r + i;
                    line[i][1] = c - i;
                }
                lines.add(line);
            }
        }
        return lines;
    }
}