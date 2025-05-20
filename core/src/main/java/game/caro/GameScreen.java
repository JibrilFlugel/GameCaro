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

public class GameScreen implements Screen {

    final Caro game;

    final int BOARD_SIZE = 15;

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

    public class Mark {
        boolean isX;
        Vector2 position;
        float timer = 0f;

        Mark(boolean isX, float x, float y) {
            this.isX = isX;
            this.position = new Vector2(x, y);
        }

        void update(float delta) {
            timer += delta;
        }

        void draw(Caro game, float width, float height) {
            Animation<TextureRegion> animation = isX ? X_animation : O_animation;
            TextureRegion frame = animation.getKeyFrame(timer, true);
            game.batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
        }
    }

    public GameScreen(final Caro game) {
        this.game = game;

        // Load textures
        backgroundTexture = new Texture("background.png");
        boardTexture = new Texture("board.png");

        boardSprite = new Sprite(boardTexture);
        boardSprite.setSize(BOARD_SIZE, BOARD_SIZE);
        boardSize = new Vector2(boardSprite.getWidth(), boardSprite.getHeight());

        cellSizeX = boardSize.x / BOARD_SIZE;
        cellSizeY = boardSize.y / BOARD_SIZE;

        touchPos = new Vector2();

        X_animation = new Animation<>(0.5f, game.textureAtlas.findRegions("X"), Animation.PlayMode.LOOP);
        O_animation = new Animation<>(0.5f, game.textureAtlas.findRegions("O"), Animation.PlayMode.LOOP);
        marks = new Array<>();
    }

    private void input() {
        // TODO: Handle input
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            game.viewport.unproject(touchPos);
            tick(turn);
            turn = !turn;
        }
    }

    private void logic() {
        // TODO: Handle game logic
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        // Set the position of the board sprite to the center of the screen
        boardSprite.setPosition((worldWidth - boardSize.x) / 2, (worldHeight - boardSize.y) / 2);

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
            m.draw(game, cellSizeX, cellSizeY);
        }

        game.batch.end();
    }

    private void tick(Boolean is_X) {
        float boardLeft = boardSprite.getX();
        float boardBottom = boardSprite.getY();

        float x = touchPos.x;
        float y = touchPos.y;

        if (x < boardLeft || x > boardLeft + boardSize.x || y < boardBottom || y > boardBottom + boardSize.y) {
            return;
        }

        int col = (int) ((x - boardLeft) / cellSizeX);
        int row = (int) ((y - boardBottom) / cellSizeY);

        float xPos = boardLeft + (col + 0.5f) * cellSizeX;
        float yPos = boardBottom + (row + 0.5f) * cellSizeY;

        marks.add(new Mark(is_X, xPos, yPos));
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
