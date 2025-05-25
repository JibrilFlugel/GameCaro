package game.caro.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

import game.caro.Caro;
import game.caro.screens.ai.ChoiceScreen;
import game.caro.screens.pvp.WaitScreen;

public class ModeChoiceScreen implements Screen {
    private Stage stage;

    public ModeChoiceScreen(Caro game) {
        stage = new Stage(game.viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        TextureRegion hostRegion = game.textureAtlas.findRegion("host");
        TextureRegion joinRegion = game.textureAtlas.findRegion("join");
        TextureRegion aiRegion = game.textureAtlas.findRegion("ai");

        float buttonSize = 150f;

        Image imageHost = new Image(new TextureRegionDrawable(hostRegion));
        Image imageJoin = new Image(new TextureRegionDrawable(joinRegion));
        Image imageAI = new Image(new TextureRegionDrawable(aiRegion));

        imageHost.setSize(buttonSize, buttonSize);
        imageAI.setSize(buttonSize, buttonSize);
        imageJoin.setSize(buttonSize, buttonSize);

        imageHost.setScaling(Scaling.fill);
        imageAI.setScaling(Scaling.fill);
        imageJoin.setScaling(Scaling.fill);

        imageHost.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new WaitScreen(game, true));
            }
        });

        imageHost.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new WaitScreen(game, false));
            }
        });

        imageAI.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ChoiceScreen(game));
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.add(imageHost).size(buttonSize, buttonSize).pad(20);
        table.row();
        table.add(imageJoin).size(buttonSize, buttonSize).pad(20);
        table.row();
        table.add(imageAI).size(buttonSize, buttonSize).pad(20);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
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

    @Override
    public void dispose() {
        stage.dispose();
    }
}