package game.caro.screens.pvp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import game.caro.Caro;
import game.caro.classes.Mark;
import game.caro.helper.GameConfig;
import game.caro.helper.GameLogic;
import game.caro.helper.NetworkHandler;

public class GameScreenMultiplayer implements Screen {
    final Caro game;
    boolean isGameOver = false;
    final int BOARD_LENGTH = GameConfig.BOARD_LENGTH;
    final int BOARD_SIZE = GameConfig.BOARD_SIZE;
    int[][] boardState = new int[BOARD_SIZE][BOARD_SIZE];

    final float worldWidth;
    final float worldHeight;

    final Texture backgroundTexture;
    Texture boardTexture;
    Vector2 touchPos;
    Sprite boardSprite;
    Vector2 boardSize;
    float cellSizeX;
    float cellSizeY;

    Animation<TextureRegion> X_animation;
    Animation<TextureRegion> O_animation;
    Array<Mark> marks;
    float XO_timer = 0f;

    Animation<TextureRegion> winAnimation;
    Animation<TextureRegion> drawAnimation;
    Animation<TextureRegion> lossAnimation;
    float resultTimer = 0f;
    int gameResult = 0;

    // Multiplayer config
    private final boolean isServer;
    private int localMark;
    private int remoteMark;
    private boolean isLocalPlayerTurn;
    private NetworkHandler networkHandler;
    private Thread networkThread;
    private String gameState = "WAITING"; // WAITING, PLAYING, GAME_OVER
    private String serverIp;

    public GameScreenMultiplayer(final Caro game, boolean isServer, String serverIp) {
        this.game = game;
        this.isServer = isServer;
        this.serverIp = serverIp;
        this.backgroundTexture = game.backgroundTexture;
        worldHeight = game.viewport.getWorldHeight();
        worldWidth = game.viewport.getWorldWidth();

        boardTexture = new Texture("board.png");
        boardSprite = new Sprite(boardTexture);
        boardSprite.setSize(BOARD_LENGTH, BOARD_LENGTH);
        boardSize = new Vector2(boardSprite.getWidth(), boardSprite.getHeight());
        boardSprite.setPosition((worldWidth - boardSize.x) / 2, (worldHeight - boardSize.y) / 2);

        cellSizeX = boardSize.x / BOARD_SIZE;
        cellSizeY = boardSize.y / BOARD_SIZE;
        touchPos = new Vector2();

        X_animation = new Animation<>(0.5f, game.textureAtlas.findRegions("X"), Animation.PlayMode.LOOP);
        O_animation = new Animation<>(0.5f, game.textureAtlas.findRegions("O"), Animation.PlayMode.LOOP);
        marks = new Array<>();

        winAnimation = new Animation<>(0.5f, game.textureAtlas.findRegions("win"), Animation.PlayMode.LOOP);
        drawAnimation = new Animation<>(0.5f, game.textureAtlas.findRegions("draw"), Animation.PlayMode.LOOP);
        lossAnimation = new Animation<>(0.5f, game.textureAtlas.findRegions("loss"), Animation.PlayMode.LOOP);

        if (isServer) {
            localMark = 1;
            remoteMark = 2;
            isLocalPlayerTurn = true; // Server makes first move
            setupServer();
        } else {
            localMark = 2;
            remoteMark = 1;
            isLocalPlayerTurn = false;
            setupClient();
        }
        // TODO: Randomize mark for server and client
    }

    private void setupServer() {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(12345);
                Socket clientSocket = serverSocket.accept();
                networkHandler = new NetworkHandler(clientSocket, this);
                networkThread = new Thread(networkHandler);
                networkThread.start();
                Gdx.app.postRunnable(() -> gameState = "PLAYING");
                serverSocket.close();
            } catch (IOException e) {
                Gdx.app.log("Server", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void setupClient() {
        new Thread(() -> {
            try {
                Socket socket = new Socket(serverIp, 12345);
                networkHandler = new NetworkHandler(socket, this);
                networkThread = new Thread(networkHandler);
                networkThread.start();
                Gdx.app.postRunnable(() -> gameState = "PLAYING");
            } catch (IOException e) {
                Gdx.app.log("Client", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void input() {
        if (Gdx.input.justTouched() && !isGameOver && isLocalPlayerTurn && gameState.equals("PLAYING")) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            game.viewport.unproject(touchPos);
            float boardLeft = boardSprite.getX();
            float boardBottom = boardSprite.getY();
            int col = (int) ((touchPos.x - boardLeft) / cellSizeX);
            int row = (int) ((touchPos.y - boardBottom) / cellSizeY);

            if (placeMark(localMark, row, col)) {
                networkHandler.sendMessage("MOVE " + row + " " + col);
                isLocalPlayerTurn = false;
            }
        }
    }

    public void processMessage(String message) {
        if (message.startsWith("MOVE")) {
            String[] parts = message.split(" ");
            int row = Integer.parseInt(parts[1]);
            int col = Integer.parseInt(parts[2]);
            placeMark(remoteMark, row, col);
            isLocalPlayerTurn = true;
        }
    }

    public void handleDisconnection() {
        if (!gameState.equals("GAME_OVER")) {
            gameState = "GAME_OVER";
            isGameOver = true;
            gameResult = -1; // Disconnect = loss
        }
    }

    private boolean placeMark(int mark, int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE || boardState[row][col] != 0) {
            return false;
        }
        boardState[row][col] = mark;
        float xPos = boardSprite.getX() + (col + 0.5f) * cellSizeX;
        float yPos = boardSprite.getY() + (row + 0.5f) * cellSizeY;
        marks.add(Mark.create(mark == 1, xPos, yPos, X_animation, O_animation));
        for (Mark m : marks) {
            m.resetAnimation();
        }
        if (GameLogic.checkWin(boardState, row, col, mark, BOARD_SIZE)) {
            isGameOver = true;
            gameState = "GAME_OVER";
            if (mark == localMark) {
                gameResult = 1; // Local wins
            } else {
                gameResult = -1; // Remote wins
            }
        } else if (GameLogic.isBoardFull(boardState, BOARD_SIZE)) {
            isGameOver = true;
            gameState = "GAME_OVER";
            gameResult = 0;
        }
        return true;
    }

    private void logic() {
        XO_timer += Gdx.graphics.getDeltaTime();
        for (Mark m : marks) {
            m.update(Gdx.graphics.getDeltaTime());
        }
        if (isGameOver) {
            resultTimer += Gdx.graphics.getDeltaTime();
        }
    }

    private void draw() {
        game.beginFrame();
        boardSprite.draw(game.batch);

        for (Mark m : marks) {
            m.draw(game, X_animation, O_animation, cellSizeX, cellSizeY);
        }

        if (gameState.equals("WAITING") && isServer) {
            try {
                String ip = InetAddress.getLocalHost().getHostAddress();
                game.font.draw(game.batch, "Waiting for opponent. Your IP: " + ip, 50, worldHeight - 50);
            } catch (Exception e) {
                game.font.draw(game.batch, "Waiting for opponent...", 50, worldHeight - 50);
            }
        } else if (gameState.equals("PLAYING")) {
            String turnText = isLocalPlayerTurn ? "Your turn" : "Opponent's turn";
            game.font.draw(game.batch, turnText, 50, worldHeight - 50);
        }

        if (gameState.equals("GAME_OVER")) {
            Animation<TextureRegion> resultAnimation;
            if (gameResult == 1) {
                resultAnimation = winAnimation;
            } else if (gameResult == -1) {
                resultAnimation = lossAnimation;
            } else {
                resultAnimation = drawAnimation;
            }
            TextureRegion frame = resultAnimation.getKeyFrame(resultTimer, true);
            float width = GameConfig.RESULT_WIDTH;
            float height = GameConfig.RESULT_HEIGHT;
            float x = worldWidth / 2 - width / 2;
            float y = worldHeight / 2 + height / 2;
            game.batch.draw(frame, x, y, width, height);
        }

        game.endFrame();
    }

    @Override
    public void render(float delta) {
        input();
        logic();
        draw();
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        boardTexture.dispose();
        if (networkHandler != null) {
            networkHandler.stop();
        }
        if (networkThread != null) {
            networkThread.interrupt();
        }
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
}
