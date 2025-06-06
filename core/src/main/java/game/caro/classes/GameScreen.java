package game.caro.classes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

import game.caro.Caro;
import game.caro.helper.GameConfig;
import game.caro.helper.GameLogic;
import game.caro.screens.HomeScreen;

public abstract class GameScreen implements Screen {
    protected final Caro game;
    protected boolean isGameOver = false;
    protected final int BOARD_LENGTH = GameConfig.BOARD.LENGTH;
    protected final int BOARD_SIZE = GameConfig.BOARD.SIZE;
    protected int[][] boardState = new int[BOARD_SIZE][BOARD_SIZE];

    protected final float worldWidth;
    protected final float worldHeight;
    protected Vector2 notiPos;

    protected final Texture backgroundTexture;
    protected Texture boardTexture;
    protected Vector2 touchPos;
    protected Sprite boardSprite;

    protected Animation<TextureRegion> X_animation;
    protected Animation<TextureRegion> O_animation;
    protected Array<Mark> marks;
    protected float XO_timer = 0f;

    protected Animation<TextureRegion> winAnimation;
    protected Animation<TextureRegion> drawAnimation;
    protected Animation<TextureRegion> lossAnimation;
    protected Animation<TextureRegion> turnAnimation;
    protected float resultTimer = 0f;
    protected int gameResult = 0;

    protected Stage stage;
    protected Image imageBack;
    protected Image imageReplay;

    public void drawBoardAndMarks() {
        boardSprite.draw(game.batch);

        for (Mark m : marks) {
            m.draw(game, X_animation, O_animation,
                    GameConfig.BOARD.CELL_SIZE, GameConfig.BOARD.CELL_SIZE);
        }
    };

    protected abstract void setGameResult(int mark);

    protected abstract void handleReplay();

    protected abstract void draw();

    protected abstract void input();

    public GameScreen(final Caro game) {
        this.game = game;
        this.backgroundTexture = game.backgroundTexture;
        worldHeight = game.viewport.getWorldHeight();
        worldWidth = game.viewport.getWorldWidth();
        stage = new Stage(game.viewport, game.batch);

        notiPos = new Vector2(GameConfig.WINDOW.MARGIN_X, worldHeight - GameConfig.WINDOW.MARGIN_Y);
        boardTexture = new Texture("board.png");

        boardSprite = new Sprite(boardTexture);
        boardSprite.setSize(BOARD_LENGTH, BOARD_LENGTH);

        boardSprite.setPosition((worldWidth - GameConfig.BOARD.LENGTH) / 2,
                (worldHeight - GameConfig.BOARD.LENGTH) / 2);

        touchPos = new Vector2();

        X_animation = new Animation<>(GameConfig.SETTINGS.FRAME_DURATION, game.textureAtlas.findRegions("X"),
                Animation.PlayMode.LOOP);
        O_animation = new Animation<>(GameConfig.SETTINGS.FRAME_DURATION, game.textureAtlas.findRegions("O"),
                Animation.PlayMode.LOOP);
        marks = new Array<>();

        winAnimation = new Animation<>(GameConfig.SETTINGS.FRAME_DURATION, game.textureAtlas.findRegions("win"),
                Animation.PlayMode.LOOP);
        drawAnimation = new Animation<>(GameConfig.SETTINGS.FRAME_DURATION, game.textureAtlas.findRegions("draw"),
                Animation.PlayMode.LOOP);
        lossAnimation = new Animation<>(GameConfig.SETTINGS.FRAME_DURATION, game.textureAtlas.findRegions("loss"),
                Animation.PlayMode.LOOP);

        TextureRegion backRegion = new TextureRegion(game.textureAtlas.findRegion("back"));
        TextureRegion replayRegion = new TextureRegion(game.textureAtlas.findRegion("replay"));

        imageBack = new Image(backRegion);
        imageReplay = new Image(replayRegion);

        imageBack.setSize(GameConfig.GAMEOVER.BACK_WIDTH, GameConfig.GAMEOVER.BACK_HEIGHT);
        imageReplay.setSize(GameConfig.GAMEOVER.REPLAY_WIDTH, GameConfig.GAMEOVER.REPLAY_HEIGHT);

        float x = worldWidth / 2 - GameConfig.GAMEOVER.REPLAY_WIDTH / 2;
        float y = worldHeight / 2 - GameConfig.GAMEOVER.RESULT_HEIGHT / 2 - GameConfig.WINDOW.SPACING;

        imageBack.setPosition(GameConfig.WINDOW.MARGIN_X, GameConfig.WINDOW.MARGIN_Y);
        imageReplay.setPosition(x, y);

        imageBack.setScaling(Scaling.fill);
        imageReplay.setScaling(Scaling.fill);

        imageBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new HomeScreen(game));
            }
        });

        imageReplay.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleReplay();
            }
        });

        imageReplay.setVisible(false);

        stage.addActor(imageBack);
        stage.addActor(imageReplay);
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
    public void show() {
        game.addScreenInputProcessor(stage);
    }

    protected void logic() {
        XO_timer += Gdx.graphics.getDeltaTime();
        for (Mark m : marks) {
            m.update(Gdx.graphics.getDeltaTime());
        }
        if (isGameOver) {
            resultTimer += Gdx.graphics.getDeltaTime();
        }
    }

    @Override
    public void render(float delta) {
        input();
        logic();
        game.beginFrame();
        draw();
        imageReplay.setVisible(isGameOver);
        game.endFrame();
        stage.act(delta);
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        boardTexture.dispose();
        stage.dispose();
    }

    protected boolean placeMark(int mark, int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE || boardState[row][col] != 0) {
            return false;
        }
        boardState[row][col] = mark;
        float xPos = boardSprite.getX() + (col + 0.5f) * GameConfig.BOARD.CELL_SIZE;
        float yPos = boardSprite.getY() + (row + 0.5f) * GameConfig.BOARD.CELL_SIZE;
        marks.add(Mark.create(mark == 1, xPos, yPos, X_animation, O_animation));
        for (Mark m : marks) {
            m.resetAnimation();
        }
        if (GameLogic.checkWin(boardState, row, col, mark, BOARD_SIZE)) {
            isGameOver = true;
            setGameResult(mark);
            System.out.println((mark == 1 ? "X" : "O") + " wins!");
        } else if (GameLogic.isBoardFull(boardState, BOARD_SIZE)) {
            isGameOver = true;
            gameResult = 0; // Draw
            System.out.println("Draw!");
        }
        return true;
    }

    public void drawResult() {
        Animation<TextureRegion> resultAnimation;
        if (gameResult == 1) {
            resultAnimation = winAnimation;
        } else if (gameResult == -1) {
            resultAnimation = lossAnimation;
        } else {
            resultAnimation = drawAnimation;
        }
        TextureRegion frame = resultAnimation.getKeyFrame(resultTimer, true);
        float width = GameConfig.GAMEOVER.RESULT_WIDTH;
        float height = GameConfig.GAMEOVER.RESULT_HEIGHT;
        float x = worldWidth / 2 - width / 2;
        float y = worldHeight / 2 + height / 2;
        game.batch.draw(frame, x, y, width, height);
    }
}
