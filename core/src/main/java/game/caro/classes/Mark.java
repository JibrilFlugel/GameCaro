package game.caro.classes;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import game.caro.Caro;

public class Mark {
    boolean isX;
    Vector2 position;
    float timer = 0f;
    Animation<TextureRegion> X_animation;
    Animation<TextureRegion> O_animation;

    public Mark(boolean isX, float x, float y, Animation<TextureRegion> xAnim, Animation<TextureRegion> oAnim) {
        this.isX = isX;
        this.position = new Vector2(x, y);
        this.X_animation = xAnim;
        this.O_animation = oAnim;
    }

    public Mark(boolean isX, float x, float y) {
        this.isX = isX;
        this.position = new Vector2(x, y);
    }

    public void update(float delta) {
        timer += delta;
    }

    public void draw(
            Caro game,
            Animation<TextureRegion> X_animation,
            Animation<TextureRegion> O_animation,
            float width,
            float height) {
        Animation<TextureRegion> animation = isX ? X_animation : O_animation;
        TextureRegion frame = animation.getKeyFrame(timer, true);
        game.batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
    }

    public static Mark create(boolean isX, float x, float y) {
        return new Mark(isX, x, y);
    }
}
