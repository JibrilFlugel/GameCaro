package game.caro.screens.pvp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

import game.caro.Caro;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WaitScreen implements Screen {
    private Stage stage;
    final Caro game;
    private boolean isHosting;
    private Socket socket;

    public WaitScreen(Caro game, boolean isHosting) {
        this.game = game;
        this.isHosting = isHosting;
        stage = new Stage(game.viewport, game.batch);
        Gdx.input.setInputProcessor(stage);
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = game.font;
        labelStyle.fontColor = Color.RED;

        Label label = new Label(isHosting ? "Waiting for opponent..." : "Connecting to server...", labelStyle);
        Table table = new Table();
        table.setFillParent(true);
        table.add(label);
        stage.addActor(table);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isHosting) {
                        ServerSocket serverSocket = new ServerSocket(12345);
                        socket = serverSocket.accept();
                    } else {
                        socket = new Socket("localhost", 12345);
                    }
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            // game.setScreen(new GameScreenPvP(game, socket, isHosting));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO: handle errors by returning to ModeChoiceScreen
                }
            }
        }).start();
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
