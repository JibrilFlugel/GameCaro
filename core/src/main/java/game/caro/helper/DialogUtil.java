package game.caro.helper;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;

public class DialogUtil {
    public static Texture createRoundedRect(float r, float g, float b, float a, int radius) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fillRectangle(radius, 0, 64 - 2 * radius, 64);
        pixmap.fillRectangle(0, radius, 64, 64 - 2 * radius);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(64 - radius, radius, radius);
        pixmap.fillCircle(radius, 64 - radius, radius);
        pixmap.fillCircle(64 - radius, 64 - radius, radius);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public static NinePatch createRoundedRectNinePatch(float r, float g, float b, float a, int radius) {
        Texture texture = createRoundedRect(r, g, b, a, radius);
        return new NinePatch(texture, radius, radius, radius, radius);
    }

    public static Texture createCursorTexture() {
        Pixmap pixmap = new Pixmap(2, 20, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.0f, 0.5f, 1.0f, 1.0f); // Blue cursor
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public static Texture createSelectionTexture() {
        Pixmap pixmap = new Pixmap(1, 20, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.0f, 0.5f, 1.0f, 0.3f); // Light blue selection
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

}
