package game.caro;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import game.caro.helper.GameConfig;
import game.caro.helper.GlobalInputHandler;
import game.caro.screens.HomeScreen;

public class Caro extends Game {

    public SpriteBatch batch;
    public BitmapFont font;
    public FitViewport viewport;
    public TextureAtlas textureAtlas;
    public Texture backgroundTexture;
    private final GlobalInputHandler globalInputHandler = new GlobalInputHandler(GameConfig.WINDOW_WIDTH,
            GameConfig.WINDOW_HEIGHT);

    private InputMultiplexer inputMultiplexer = new InputMultiplexer();

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        viewport = new FitViewport(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        textureAtlas = new TextureAtlas(Gdx.files.internal("spritesheet.txt"));
        backgroundTexture = new Texture("background.png");

        font.setUseIntegerPositions(false);
        font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());

        inputMultiplexer.addProcessor(globalInputHandler);
        Gdx.input.setInputProcessor(inputMultiplexer);

        this.setScreen(new HomeScreen(this));
    }

    public void addScreenInputProcessor(InputAdapter screenInputProcessor) {
        inputMultiplexer.clear();
        inputMultiplexer.addProcessor(globalInputHandler);
        inputMultiplexer.addProcessor(screenInputProcessor);
    }

    public void beginFrame() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        drawBackground();
    }

    public void endFrame() {
        batch.end();
    }

    public void drawBackground() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        batch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        textureAtlas.dispose();
        backgroundTexture.dispose();
    }
}