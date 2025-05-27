package game.caro.helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class GlobalInputHandler extends InputAdapter {

    private final int windowWidth;
    private final int windowHeight;
    private boolean isFullscreen = false;

    public GlobalInputHandler(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ENTER &&
                (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))) {

            if (isFullscreen) {
                Gdx.graphics.setWindowedMode(windowWidth, windowHeight);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }

            isFullscreen = !isFullscreen;
            return true;
        }
        return false;
    }
}
