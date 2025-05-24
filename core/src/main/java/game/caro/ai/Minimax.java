package game.caro.ai;

import com.badlogic.gdx.utils.Array;
import game.caro.helper.GameLogic;

public class Minimax {
    private int aiMark;
    private int opponentMark;
    private static final int DEPTH = 2;
    private static final int BOARD_SIZE = 15;

    public Minimax(int aiMark) {
        this.aiMark = aiMark;
        this.opponentMark = (aiMark == 1) ? 2 : 1;
    }

    public int[] findBestMove(int[][] board) {
        int bestScore = Integer.MIN_VALUE;
        int bestRow = -1;
        int bestCol = -1;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (board[r][c] == 0) {
                    board[r][c] = aiMark;
                    int score = minimax(board, DEPTH - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE, r, c);
                    board[r][c] = 0;
                    if (score > bestScore) {
                        bestScore = score;
                        bestRow = r;
                        bestCol = c;
                    }
                }
            }
        }
        return new int[] { bestRow, bestCol };
    }

    private int minimax(int[][] board, int depth, boolean isMaximizing, int alpha, int beta, int lastRow, int lastCol) {
        int mark = board[lastRow][lastCol];
        if (GameLogic.checkWin(board, lastRow, lastCol, mark, BOARD_SIZE)) {
            return (mark == aiMark) ? 1000000 : -1000000;
        } else if (GameLogic.isBoardFull(board, BOARD_SIZE)) {
            return 0;
        } else if (depth == 0) {
            return evaluate(board);
        }

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            outer: for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    if (board[r][c] == 0) {
                        board[r][c] = aiMark;
                        int score = minimax(board, depth - 1, false, alpha, beta, r, c);
                        board[r][c] = 0;
                        bestScore = Math.max(bestScore, score);
                        alpha = Math.max(alpha, score);
                        if (beta <= alpha) {
                            break outer;
                        }
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            outer: for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    if (board[r][c] == 0) {
                        board[r][c] = opponentMark;
                        int score = minimax(board, depth - 1, true, alpha, beta, r, c);
                        board[r][c] = 0;
                        bestScore = Math.min(bestScore, score);
                        beta = Math.min(beta, score);
                        if (beta <= alpha) {
                            break outer;
                        }
                    }
                }
            }
            return bestScore;
        }
    }

    private int evaluate(int[][] board) {
        int score = 0;
        Array<int[][]> lines = GameLogic.getAllLines(BOARD_SIZE, 5);
        for (int[][] line : lines) {
            int aiCount = 0;
            int opponentCount = 0;
            for (int[] cell : line) {
                int r = cell[0];
                int c = cell[1];
                if (board[r][c] == aiMark) {
                    aiCount++;
                } else if (board[r][c] == opponentMark) {
                    opponentCount++;
                }
            }
            if (opponentCount == 0 && aiCount > 0) {
                score += (int) Math.pow(10, aiCount - 1);
            } else if (aiCount == 0 && opponentCount > 0) {
                score -= (int) Math.pow(10, opponentCount - 1);
            }
        }
        return score;
    }
}