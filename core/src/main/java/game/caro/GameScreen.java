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

    Texture backgroundTexture;
    Texture boardTexture;
    Vector2 touchPos;
    Sprite boardSprite;

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
        boardSprite.setSize(15, 15);

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
        //TODO: Handle game logic
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();
        float boardWidth = boardSprite.getWidth();
        float boardHeight = boardSprite.getHeight();

        // Set the position of the board sprite to the center of the screen
        boardSprite.setPosition((worldWidth - boardWidth) / 2, (worldHeight - boardHeight) / 2);

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
    m.draw(game, 1, 1);
}

        game.batch.end();
    }

    private void tick(Boolean is_X) {
    float markWidth = 1;
    float markHeight = 1;
    float xPos = touchPos.x;
    float yPos = touchPos.y;

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
