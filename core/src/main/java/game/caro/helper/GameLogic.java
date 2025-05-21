package game.caro.helper;

public class GameLogic {
    // TODO: This game logic is ass, need to fix later
    public static boolean isBoardFull(int[][] boardState, int size) {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (boardState[r][c] == 0)
                    return false;
            }
        }
        return true;
    }

    public static boolean checkWin(int[][] boardState, int row, int col, int X_or_O, int boardSize) {
        return checkDirection(boardState, row, col, X_or_O, 1, 0, boardSize) ||
                checkDirection(boardState, row, col, X_or_O, 0, 1, boardSize) ||
                checkDirection(boardState, row, col, X_or_O, 1, 1, boardSize) ||
                checkDirection(boardState, row, col, X_or_O, 1, -1, boardSize);
    }

    private static boolean checkDirection(int[][] boardState, int row, int col, int mark, int dx, int dy,
            int boardSize) {
        int count = 1;
        boolean blockedStart = false;
        boolean blockedEnd = false;

        int r = row + dy, c = col + dx;
        while (isValid(r, c, boardSize) && boardState[r][c] == mark) {
            count++;
            r += dy;
            c += dx;
        }
        if (isValid(r, c, boardSize) && boardState[r][c] != 0 && boardState[r][c] != mark)
            blockedEnd = true;

        r = row - dy;
        c = col - dx;
        while (isValid(r, c, boardSize) && boardState[r][c] == mark) {
            count++;
            r -= dy;
            c -= dx;
        }
        if (isValid(r, c, boardSize) && boardState[r][c] != 0 && boardState[r][c] != mark)
            blockedStart = true;

        return count >= 5 && !(blockedStart && blockedEnd);
    }

    private static boolean isValid(int r, int c, int size) {
        return r >= 0 && r < size && c >= 0 && c < size;
    }
}
