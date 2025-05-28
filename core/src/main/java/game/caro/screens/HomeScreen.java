package game.caro.screens;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import game.caro.Caro;
import game.caro.screens.ai.ChoiceScreen;
import game.caro.screens.pvp.GameScreenMultiplayer;

public class HomeScreen implements Screen {
    final Caro game;
    private Stage stage;

    public HomeScreen(Caro game) {
        this.game = game;
        stage = new Stage(game.viewport, game.batch);

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
                game.setScreen(new GameScreenMultiplayer(game, true, null));
            }
        });

        imageJoin.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTextInputDialog(stage);
            }

            // TODO: Crappy workaround for not having skin, fix later
            private void showTextInputDialog(Stage stage) {
                final Window dialog = new Window("Enter Code", new Window.WindowStyle(
                        new BitmapFont(), Color.WHITE,
                        new TextureRegionDrawable(new TextureRegion(new Texture(1, 1, Pixmap.Format.RGBA8888)))));
                dialog.setSize(300, 150);
                dialog.setPosition(
                        50,
                        (stage.getHeight() - dialog.getHeight()) - 50);

                final TextField textField = new TextField("", new TextField.TextFieldStyle(
                        new BitmapFont(), Color.BLACK, null, null, null));
                textField.setMessageText("e.g., 1234");
                textField.setMaxLength(4);
                textField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());

                TextButton btnOk = new TextButton("OK", new TextButton.TextButtonStyle(
                        new TextureRegionDrawable(new TextureRegion(new Texture(1, 1, Pixmap.Format.RGBA8888))),
                        null, null, new BitmapFont()));
                TextButton btnCancel = new TextButton("Cancel", new TextButton.TextButtonStyle(
                        new TextureRegionDrawable(new TextureRegion(new Texture(1, 1, Pixmap.Format.RGBA8888))),
                        null, null, new BitmapFont()));

                btnOk.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        String code = textField.getText().trim();
                        if (code.matches("\\d{4}")) {
                            findServerByCode(code);
                        } else {
                            Gdx.app.log("Menu", "Invalid code. Please enter a 4-digit number.");
                        }
                        dialog.remove();
                    }
                });

                btnCancel.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Gdx.app.log("Menu", "Code input canceled");
                        dialog.remove();
                    }
                });

                dialog.add(new Label("Enter 4-digit code:", new Label.LabelStyle(new BitmapFont(), Color.WHITE)))
                        .colspan(2).pad(10);
                dialog.row();
                dialog.add(textField).colspan(2).width(200).pad(10);
                dialog.row();
                dialog.add(btnOk).pad(10);
                dialog.add(btnCancel).pad(10);

                stage.addActor(dialog);
                stage.setKeyboardFocus(textField);
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
        game.beginFrame();
        game.endFrame();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
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

    private void findServerByCode(String userCode) {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(12346);
                socket.setSoTimeout(1000);
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                long startTime = System.currentTimeMillis();

                while (System.currentTimeMillis() - startTime < 10000) { // 10s timeout
                    try {
                        socket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength());
                        if (message.startsWith("CARO_CODE:")) {
                            String[] parts = message.split(":");
                            if (parts.length == 3 && parts[1].equals(userCode)) {
                                String serverIp = parts[2];
                                Gdx.app.postRunnable(
                                        () -> game.setScreen(new GameScreenMultiplayer(game, false, serverIp)));
                                socket.close();
                                return;
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        // Continue listening
                    }
                }
                Gdx.app.postRunnable(() -> Gdx.app.log("Menu", "Server not found with code: " + userCode));
                socket.close();
            } catch (IOException e) {
                Gdx.app.log("Client", "Error finding server: " + e.getMessage());
            }
        }).start();
    }
}