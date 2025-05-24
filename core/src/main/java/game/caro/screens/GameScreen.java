package game.caro.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import game.caro.Caro;
import game.caro.ai.Minimax;
import game.caro.classes.Mark;
import game.caro.helper.GameLogic;

public class GameScreen implements Screen {
    final Caro game;
    boolean isPlayerTurn = true;
    boolean isGameOver = false;
    final int WINDOW_WIDTH = 1280;
    final int WINDOW_HEIGHT = 720;
    final int BOARD_LENGTH = WINDOW_HEIGHT - 100;
    final int BOARD_SIZE = 15;
    int[][] boardState = new int[BOARD_SIZE][BOARD_SIZE];
    boolean isFullscreen = false;

    Texture backgroundTexture;
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

    public GameScreen(final Caro game, int playerMark) {
        this.game = game;
        this.playerMark = playerMark;
        this.aiMark = (playerMark == 1) ? 2 : 1;
        this.ai = new Minimax(aiMark);

        backgroundTexture = new Texture("background.png");
        boardTexture = new Texture("board.png");

        boardSprite = new Sprite(boardTexture);
        boardSprite.setSize(BOARD_LENGTH, BOARD_LENGTH);
        boardSize = new Vector2(boardSprite.getWidth(), boardSprite.getHeight());
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();
        boardSprite.setPosition((worldWidth - boardSize.x) / 2, (worldHeight - boardSize.y) / 2);

        cellSizeX = boardSize.x / BOARD_SIZE;
        cellSizeY = boardSize.y / BOARD_SIZE;

        touchPos = new Vector2();

        X_animation = new Animation<>(0.5f, game.textureAtlas.findRegions("X"), Animation.PlayMode.LOOP);
        O_animation = new Animation<>(0.5f, game.textureAtlas.findRegions("O"), Animation.PlayMode.LOOP);
        marks = new Array<>();
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
                // Player's mark is placed and rendered immediately
                isPlayerTurn = false; // Switch to AI's turn

                // Start AI move computation in a separate thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final int[] aiMove = ai.findBestMove(boardState);
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                placeMark(aiMark, aiMove[0], aiMove[1]);
                                if (!isGameOver) {
                                    isPlayerTurn = true; // Back to player's turn if game continues
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
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        game.batch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        boardSprite.draw(game.batch);

        for (Mark m : marks) {
            m.draw(game, X_animation, O_animation, cellSizeX, cellSizeY);
        }

        game.batch.end();
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
            System.out.println((mark == 1 ? "X" : "O") + " wins!");
        } else if (GameLogic.isBoardFull(boardState, BOARD_SIZE)) {
            isGameOver = true;
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
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER &&
                        (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))) {
                    if (isFullscreen) {
                        Gdx.graphics.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
                        isFullscreen = false;
                    } else {
                        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                        isFullscreen = true;
                    }
                    return true;
                }
                return false;
            }
        });
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