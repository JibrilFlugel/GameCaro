package game.caro.screens.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import game.caro.Caro;
import game.caro.ai.Minimax;
import game.caro.classes.Mark;
import game.caro.helper.GameConfig;
import game.caro.helper.GameLogic;

public class GameScreen1P implements Screen {
    final Caro game;
    boolean isPlayerTurn = true;
    boolean isGameOver = false;
    final int BOARD_LENGTH = GameConfig.BOARD_LENGTH;
    final int BOARD_SIZE = GameConfig.BOARD_SIZE;
    int[][] boardState = new int[BOARD_SIZE][BOARD_SIZE];
    boolean isFullscreen = false;

    final float worldWidth;
    final float worldHeight;

    final Texture backgroundTexture;
    Texture boardTexture;
    Vector2 touchPos;
    Sprite boardSprite;
    Vector2 boardSize;
    float cellSizeX;
    float cellSizeY;

    Animation<TextureRegion> X_animation;
    Animation<TextureRegion> O_animation;
    Array<Mark> marks;
    float XO_timer = 0f;

    private int playerMark;
    private int aiMark;
    private Minimax ai;

    private Animation<TextureRegion> winAnimation;
    private Animation<TextureRegion> drawAnimation;
    private Animation<TextureRegion> lossAnimation;
    private float resultTimer = 0f;
    private int gameResult = 0; // 1: player wins, -1: player loss, 0: draw

    public GameScreen1P(final Caro game, int playerMark) {
        this.game = game;
        this.playerMark = playerMark;
        this.aiMark = (playerMark == 1) ? 2 : 1;
        this.ai = new Minimax(aiMark);
        this.backgroundTexture = game.backgroundTexture;
        worldHeight = game.viewport.getWorldHeight();
        worldWidth = game.viewport.getWorldWidth();

        boardTexture = new Texture("board.png");

        boardSprite = new Sprite(boardTexture);
        boardSprite.setSize(BOARD_LENGTH, BOARD_LENGTH);
        boardSize = new Vector2(boardSprite.getWidth(), boardSprite.getHeight());

        boardSprite.setPosition((worldWidth - boardSize.x) / 2, (worldHeight - boardSize.y) / 2);

        cellSizeX = boardSize.x / BOARD_SIZE;
        cellSizeY = boardSize.y / BOARD_SIZE;

        touchPos = new Vector2();

        X_animation = new Animation<>(0.5f, game.textureAtlas.findRegions("X"), Animation.PlayMode.LOOP);
        O_animation = new Animation<>(0.5f, game.textureAtlas.findRegions("O"), Animation.PlayMode.LOOP);
        marks = new Array<>();

        winAnimation = new Animation<>(0.5f, game.textureAtlas.findRegions("win"), Animation.PlayMode.LOOP);
        drawAnimation = new Animation<>(0.5f, game.textureAtlas.findRegions("draw"), Animation.PlayMode.LOOP);
        lossAnimation = new Animation<>(0.5f, game.textureAtlas.findRegions("loss"), Animation.PlayMode.LOOP);
    }

    private void input() {
        if (Gdx.input.justTouched() && !isGameOver && isPlayerTurn) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            game.viewport.unproject(touchPos);
            float boardLeft = boardSprite.getX();
            float boardBottom = boardSprite.getY();
            int col = (int) ((touchPos.x - boardLeft) / cellSizeX);
            int row = (int) ((touchPos.y - boardBottom) / cellSizeY);

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

    private void logic() {
        XO_timer += Gdx.graphics.getDeltaTime();
        for (Mark m : marks) {
            m.update(Gdx.graphics.getDeltaTime());
        }
        if (isGameOver) {
            resultTimer += Gdx.graphics.getDeltaTime();
        }
    }

    private void draw() {
        game.beginFrame();

        boardSprite.draw(game.batch);

        for (Mark m : marks) {
            m.draw(game, X_animation, O_animation, cellSizeX, cellSizeY);
        }

        if (isGameOver) {
            Animation<TextureRegion> resultAnimation;
            if (gameResult == 1) {
                resultAnimation = winAnimation;
            } else if (gameResult == -1) {
                resultAnimation = lossAnimation;
            } else {
                resultAnimation = drawAnimation;
            }
            TextureRegion frame = resultAnimation.getKeyFrame(resultTimer, true);
            float width = GameConfig.RESULT_WIDTH;
            float height = GameConfig.RESULT_HEIGHT;
            float x = worldWidth / 2 - width / 2;
            float y = worldHeight / 2 + height / 2;
            game.batch.draw(frame, x, y, width, height);
        }

        game.endFrame();
    }

    private boolean placeMark(int mark, int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE || boardState[row][col] != 0) {
            return false;
        }
        boardState[row][col] = mark;
        float xPos = boardSprite.getX() + (col + 0.5f) * cellSizeX;
        float yPos = boardSprite.getY() + (row + 0.5f) * cellSizeY;
        marks.add(Mark.create(mark == 1, xPos, yPos, X_animation, O_animation));
        for (Mark m : marks) {
            m.resetAnimation();
        }
        if (GameLogic.checkWin(boardState, row, col, mark, BOARD_SIZE)) {
            isGameOver = true;
            if (mark == playerMark) {
                gameResult = 1; // Player wins
            } else {
                gameResult = -1; // Player loss
            }
            System.out.println((mark == 1 ? "X" : "O") + " wins!");
        } else if (GameLogic.isBoardFull(boardState, BOARD_SIZE)) {
            isGameOver = true;
            gameResult = 0; // Draw
            System.out.println("Draw!");
        }
        return true;
    }

    @Override
    public void render(float delta) {
        input();
        logic();
        draw();
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        boardTexture.dispose();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}