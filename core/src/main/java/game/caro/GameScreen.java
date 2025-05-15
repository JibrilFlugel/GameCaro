package game.caro;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {

    final Caro game;

    Texture backgroundTexture;
    Texture boardTexture;
    Texture X_texture;
    Texture O_texture;
    Vector2 touchPos;
    Sprite boardSprite;
    Array<Sprite> X_sprites;
    Array<Sprite> O_sprites;
    float timer;
    boolean turn = true; // true = X, false = O

    public GameScreen(final Caro game) {
        this.game = game;

        // Load textures
        backgroundTexture = new Texture("background.png");
        X_texture = new Texture("X.png");
        O_texture = new Texture("O.png");
        boardTexture = new Texture("board.png");

        boardSprite = new Sprite(boardTexture);
        boardSprite.setSize(15, 15);

        touchPos = new Vector2();

        X_sprites = new Array<>();
        O_sprites = new Array<>();
    }

    private void input() {
        // TODO: Handle input
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            game.viewport.unproject(touchPos);
            tick(turn);
            turn = !turn;
        }
    }

    private void logic() {
        //TODO: Handle game logic
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();
        float boardWidth = boardSprite.getWidth();
        float boardHeight = boardSprite.getHeight();

        // Set the position of the board sprite to the center of the screen
        boardSprite.setPosition((worldWidth - boardWidth) / 2, (worldHeight - boardHeight) / 2);
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

        for (Sprite X_sprite : X_sprites) {
            X_sprite.draw(game.batch);
        }

        for (Sprite O_sprite : O_sprites) {
            O_sprite.draw(game.batch);
        }

        game.batch.end();
    }

    private void tick(Boolean is_X) {
        float markWidth = 1;
        float markHeight = 1;
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();
        float boardWidth = boardSprite.getWidth();
        float boardHeight = boardSprite.getHeight();

        // Calculate the position of the mark based on the touch position
        // float xPos = (touchPos.x - (worldWidth - boardWidth) / 2) / (boardWidth / 15);
        // float yPos = (touchPos.y - (worldHeight - boardHeight) / 2) / (boardHeight / 15);
        float xPos = touchPos.x;
        float yPos = touchPos.y;

        // Create a new sprite for the mark
        Sprite markSprite = new Sprite(is_X ? X_texture : O_texture);
        markSprite.setSize(markWidth, markHeight);
        markSprite.setPosition(xPos, yPos);

        if (is_X) {
            X_sprites.add(markSprite);
        } else {
            O_sprites.add(markSprite);
        }
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
        O_texture.dispose();
        X_texture.dispose();
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
