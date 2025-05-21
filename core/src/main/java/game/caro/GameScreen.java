package game.caro;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import game.caro.classes.Mark;
import game.caro.helper.GameLogic;

public class GameScreen implements Screen {

    final Caro game;

    final int BOARD_SIZE = 15;

    int boardState[][] = new int[BOARD_SIZE][BOARD_SIZE];
    boolean isGameOver = false;

    Texture backgroundTexture;
    Texture boardTexture;
    Vector2 touchPos;
    Sprite boardSprite;
    Vector2 boardSize;
    float cellSizeX;
    float cellSizeY;

    // X and O
    Animation<TextureRegion> X_animation;
    Animation<TextureRegion> O_animation;
    Array<Mark> marks;
    float XO_timer = 0f;

    boolean turn = true; // true = X, false = O

    public GameScreen(final Caro game) {
        this.game = game;

        // Load textures
        backgroundTexture = new Texture("background.png");
        boardTexture = new Texture("board.png");

        // Board sprite initialize
        boardSprite = new Sprite(boardTexture);
        boardSprite.setSize(BOARD_SIZE, BOARD_SIZE);
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
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            game.viewport.unproject(touchPos);
        }

        if (tick(turn)) {
            turn = !turn;
        }
    }

    private void update() {

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

    private boolean tick(Boolean is_X) {
        if (isGameOver) {
            return false;
        }

        float boardLeft = boardSprite.getX();
        float boardBottom = boardSprite.getY();

        float x = touchPos.x;
        float y = touchPos.y;

        if (x < boardLeft || x > boardLeft + boardSize.x || y < boardBottom || y > boardBottom + boardSize.y) {
            return false;
        }

        int col = (int) ((x - boardLeft) / cellSizeX);
        int row = (int) ((y - boardBottom) / cellSizeY);

        if (boardState[row][col] != 0) {
            return false;
        }

        boardState[row][col] = is_X ? 1 : 2;

        float xPos = boardLeft + (col + 0.5f) * cellSizeX;
        float yPos = boardBottom + (row + 0.5f) * cellSizeY;

        marks.add(Mark.create(is_X, xPos, yPos, X_animation, O_animation));

        for (Mark m : marks) {
            m.resetAnimation();
        }

        if (GameLogic.checkWin(boardState, row, col, is_X ? 1 : 2, row)) {
            isGameOver = true;
            System.out.println((is_X ? "X" : "O") + " wins!");
        } else if (GameLogic.isBoardFull(boardState, BOARD_SIZE)) {
            isGameOver = true;
            System.out.println("Draw!");
        }
        return true;
    }

    @Override
    public void render(float delta) {
        input();
        update();
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
