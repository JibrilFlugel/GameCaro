package game.caro.screens.ai;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import game.caro.Caro;
import game.caro.helper.GameConfig;

public class ChoiceScreen implements Screen {
    private Stage stage;
    private final Caro game;

    public ChoiceScreen(Caro game) {
        this.game = game;
        stage = new Stage(game.viewport, game.batch);

        Array<AtlasRegion> xFrames = game.textureAtlas.findRegions("X");
        Array<AtlasRegion> oFrames = game.textureAtlas.findRegions("O");

        Animation<TextureRegion> xAnimation = new Animation<>(GameConfig.SETTINGS.FRAME_DURATION, xFrames,
                Animation.PlayMode.LOOP);
        Animation<TextureRegion> oAnimation = new Animation<>(GameConfig.SETTINGS.FRAME_DURATION, oFrames,
                Animation.PlayMode.LOOP);

        float buttonSize = GameConfig.MENU.BUTTON_HEIGHT;

        AnimatedActor animatedX = new AnimatedActor(xAnimation);
        AnimatedActor animatedO = new AnimatedActor(oAnimation);
        animatedX.setSize(buttonSize, buttonSize);
        animatedO.setSize(buttonSize, buttonSize);

        animatedX.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen1P(game, 1));
            }
        });

        animatedO.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen1P(game, 2));
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(animatedX).size(buttonSize, buttonSize).pad(20);
        table.add(animatedO).size(buttonSize, buttonSize).pad(20);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        game.beginFrame();
        game.endFrame();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        game.addScreenInputProcessor(stage);
    }

    @Override
    public void hide() {
        stage.clear();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    private static class AnimatedActor extends Actor {
        private final Animation<TextureRegion> animation;
        private float stateTime = 0f;

        public AnimatedActor(Animation<TextureRegion> animation) {
            this.animation = animation;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            TextureRegion frame = animation.getKeyFrame(stateTime);
            batch.draw(frame, getX(), getY(), getWidth(), getHeight());
        }
    }
}
