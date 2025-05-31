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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.Timer;

import game.caro.Caro;
import game.caro.helper.DialogUtil;
import game.caro.helper.GameConfig;
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

        float buttonSize = GameConfig.MENU_BUTTON_SIZE;

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

            private void showTextInputDialog(Stage stage) {
                Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmap.setColor(0.15f, 0.15f, 0.2f, 0.95f);
                pixmap.fill();
                Texture texture = new Texture(pixmap);
                pixmap.dispose();
                TextureRegionDrawable background = new TextureRegionDrawable(new TextureRegion(texture));

                BitmapFont font = new BitmapFont(Gdx.files.internal("OpenSans-Regular.fnt"));
                font.getData().setScale(0.8f);
                font.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                Window.WindowStyle windowStyle = new Window.WindowStyle(font, Color.WHITE, background);
                windowStyle.background = new NinePatchDrawable(
                        DialogUtil.createRoundedRectNinePatch(0.2f, 0.2f, 0.25f, 0.95f, 10));

                final Window dialog = new Window("", windowStyle);
                dialog.setSize(400, 300);
                dialog.setPosition(
                        (stage.getWidth() - dialog.getWidth()) / 2,
                        (stage.getHeight() - dialog.getHeight()) / 2);
                dialog.setMovable(false);

                TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle(
                        font,
                        Color.WHITE,
                        new TextureRegionDrawable(DialogUtil.createCursorTexture()),
                        new TextureRegionDrawable(DialogUtil.createSelectionTexture()),
                        new TextureRegionDrawable(DialogUtil.createRoundedRect(0.3f, 0.3f, 0.35f, 1.0f, 5)));
                textFieldStyle.messageFont = font;
                textFieldStyle.messageFontColor = new Color(0.7f, 0.7f, 0.7f, 1.0f);

                final TextField textField = new TextField("", textFieldStyle);
                textField.setMessageText("e.g., 1234");
                textField.setMaxLength(4);
                textField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
                textField.setAlignment(Align.center);
                textField.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle(
                        new TextureRegionDrawable(DialogUtil.createRoundedRect(0.0f, 0.5f, 0.8f, 1.0f, 5)),
                        new TextureRegionDrawable(DialogUtil.createRoundedRect(0.0f, 0.6f, 0.9f, 1.0f, 5)),
                        new TextureRegionDrawable(DialogUtil.createRoundedRect(0.0f, 0.4f, 0.7f, 1.0f, 5)),
                        font);
                buttonStyle.fontColor = Color.WHITE;
                buttonStyle.overFontColor = new Color(0.9f, 0.9f, 1.0f, 1.0f);

                TextButton btnOk = new TextButton("OK", buttonStyle);
                TextButton btnCancel = new TextButton("Cancel", buttonStyle);

                btnOk.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        String code = textField.getText().trim();
                        if (code.matches("\\d{4}")) {
                            findServerByCode(code);
                        } else {
                            Gdx.app.log("Menu", "Invalid code. Please enter a 4-digit number.");
                            textField.getStyle().fontColor = Color.RED;
                            Timer.schedule(new Timer.Task() {
                                @Override
                                public void run() {
                                    textField.getStyle().fontColor = Color.WHITE;
                                }
                            }, 0.5f);
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

                dialog.getTitleTable().padTop(10);
                dialog.add(new Label("Enter 4-digit code:", new Label.LabelStyle(font, Color.WHITE)))
                        .colspan(2).pad(15).align(Align.center);
                dialog.row();
                dialog.add(textField).colspan(2).width(250).pad(15);
                dialog.row();
                dialog.add(btnOk).width(100).height(40).pad(10);
                dialog.add(btnCancel).width(100).height(40).pad(10);

                dialog.setColor(1, 1, 1, 0.98f);
                dialog.addAction(Actions.sequence(
                        Actions.scaleTo(0.9f, 0.9f),
                        Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.swingOut)));

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