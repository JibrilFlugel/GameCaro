package game.caro.screens.ai;

import com.badlogic.gdx.Gdx;

import game.caro.Caro;
import game.caro.ai.Minimax;
import game.caro.classes.GameScreen;
import game.caro.helper.GameConfig;

public class GameScreen1P extends GameScreen {

    boolean isPlayerTurn = true;
    private int playerMark;
    private int aiMark;
    private Minimax ai;

    public GameScreen1P(final Caro game, int playerMark) {
        super(game);
        this.playerMark = playerMark;
        this.aiMark = (playerMark == 1) ? 2 : 1;
        this.ai = new Minimax(aiMark);
    }

    @Override
    protected void input() {
        if (Gdx.input.justTouched() && !isGameOver && isPlayerTurn) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            game.viewport.unproject(touchPos);
            float boardLeft = boardSprite.getX();
            float boardBottom = boardSprite.getY();
            int col = (int) ((touchPos.x - boardLeft) / GameConfig.BOARD.CELL_SIZE);
            int row = (int) ((touchPos.y - boardBottom) / GameConfig.BOARD.CELL_SIZE);

            if (placeMark(playerMark, row, col)) {
                isPlayerTurn = false;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final int[] aiMove = ai.findBestMove(boardState);
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                placeMark(aiMark, aiMove[0], aiMove[1]);
                                if (!isGameOver) {
                                    isPlayerTurn = true;
                                }
                            }
                        });
                    }
                }).start();
            }
        }
    }

    @Override
    protected void draw() {
        game.beginFrame();

        drawBoardAndMarks();

        if (isGameOver) {
            drawResult();
        }

        game.endFrame();
    }

    @Override
    protected void setGameResult(int mark) {
        if (mark == playerMark) {
            gameResult = 1;
        } else {
            gameResult = -1;
        }
    }
}