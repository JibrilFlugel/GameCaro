package game.caro.screens.pvp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

import game.caro.Caro;
import game.caro.classes.GameScreen;
import game.caro.classes.MessageType;
import game.caro.helper.GameConfig;
import game.caro.helper.NetworkHandler;
import game.caro.helper.GameConfig.PVP.GameState;

public class GameScreenMultiplayer extends GameScreen {

    // Multiplayer config
    TextureRegion yourCode;
    private final boolean isServer;
    private int localMark;
    private int remoteMark;
    private boolean isLocalPlayerTurn;
    private NetworkHandler networkHandler;
    private Thread networkThread;
    private GameState gameState;
    private String serverIp;
    private String code;
    private volatile boolean broadcasting = true;
    private float turnTimer = 0f;

    private Array<AtlasRegion> digitRegions;

    public GameScreenMultiplayer(final Caro game, boolean isServer, String serverIp) {
        super(game);
        this.isServer = isServer;
        this.serverIp = serverIp;
        this.gameState = GameState.WAITING;

        digitRegions = game.textureAtlas.findRegions("numbers");
        yourCode = game.textureAtlas.findRegion("yourCode");

        if (isServer) {
            localMark = 1;
            remoteMark = 2;
            isLocalPlayerTurn = true; // Server makes first move
            code = String.valueOf(1000 + new Random().nextInt(9000));
            setupServer();
            startBroadcasting();
        } else {
            localMark = 2;
            remoteMark = 1;
            isLocalPlayerTurn = false;
            connectToServer();
        }
    }

    private void setupServer() {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(12345);
                Socket clientSocket = serverSocket.accept();
                networkHandler = new NetworkHandler(clientSocket, this);
                networkThread = new Thread(networkHandler);
                networkThread.start();
                broadcasting = false;
                Gdx.app.postRunnable(() -> gameState = GameState.PLAYING);
                serverSocket.close();
            } catch (IOException e) {
                Gdx.app.log("Server", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                Socket socket = new Socket(serverIp, 12345);
                networkHandler = new NetworkHandler(socket, this);
                networkThread = new Thread(networkHandler);
                networkThread.start();
                Gdx.app.postRunnable(() -> gameState = GameState.PLAYING);
            } catch (IOException e) {
                Gdx.app.log("Client", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void startBroadcasting() {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);
                String localIp = getLocalIpAddress();
                String message = "CARO_CODE:" + code + ":" + localIp;
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(),
                        InetAddress.getByName("255.255.255.255"), 12346);

                while (broadcasting) {
                    socket.send(packet);
                    Thread.sleep(1000);
                }
                socket.close();
            } catch (IOException | InterruptedException e) {
                Gdx.app.log("Server", "Broadcast error: " + e.getMessage());
            }
        }).start();
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Gdx.app.log("Server", "Error getting local IP: " + ex.getMessage());
        }
        return "127.0.0.1";
    }

    @Override
    protected void handleReplay() {
        boardState = new int[BOARD_SIZE][BOARD_SIZE];
        marks.clear();
        isGameOver = false;
        gameState = GameState.PLAYING;
        isLocalPlayerTurn = isServer; // Server starts
        gameResult = 0;
        resultTimer = 0f;
        turnTimer = 0f;
        imageReplay.setVisible(false);
        // Optional: Add network message to sync replay with opponent
        if (networkHandler != null) {
            networkHandler.sendMessage("REPLAY");
        }
    }

    @Override
    protected void input() {
        if (Gdx.input.justTouched() && !isGameOver && isLocalPlayerTurn && gameState.equals(GameState.PLAYING)) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            game.viewport.unproject(touchPos);
            float boardLeft = boardSprite.getX();
            float boardBottom = boardSprite.getY();
            int col = (int) ((touchPos.x - boardLeft) / GameConfig.BOARD.CELL_SIZE);
            int row = (int) ((touchPos.y - boardBottom) / GameConfig.BOARD.CELL_SIZE);

            if (placeMark(localMark, row, col)) {
                networkHandler.sendMessage(MessageType.MOVE.serialize(row, col));
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
            turnTimer = 0f;
        } else if (message.equals("TURN_SWITCH")) {
            isLocalPlayerTurn = true;
            turnTimer = 0f;
        }
    }

    public void handleDisconnection() {
        if (!gameState.equals(GameState.GAME_OVER)) {
            gameState = GameState.GAME_OVER;
            isGameOver = true;
            gameResult = 1; // Opponent disconnect = insta win
        }
    }

    @Override
    protected void setGameResult(int mark) {
        gameState = GameState.GAME_OVER;
        if (mark == localMark) {
            gameResult = 1; // Local wins
        } else {
            gameResult = -1; // Remote wins
        }
    }

    @Override
    protected void draw() {

        drawBoardAndMarks();

        if (gameState.equals(GameState.WAITING) && isServer) {
            try {
                game.batch.draw(yourCode,
                        notiPos.x, notiPos.y,
                        GameConfig.PVP.YOUR_CODE_WIDTH, GameConfig.PVP.YOUR_CODE_HEIGHT);
                renderCode(code, notiPos.x,
                        notiPos.y - GameConfig.PVP.YOUR_CODE_HEIGHT
                                - GameConfig.WINDOW.SPACING);
            } catch (Exception e) {
                game.font.draw(game.batch, e.toString(), 50, worldHeight - 50);
            }

        } else if (gameState.equals(GameState.PLAYING)) {
            Array<AtlasRegion> turnFrames = isLocalPlayerTurn ? game.textureAtlas.findRegions("yourTurn")
                    : game.textureAtlas.findRegions("waiting");
            turnAnimation = new Animation<>(GameConfig.SETTINGS.FRAME_DURATION, turnFrames);
            TextureRegion frame = turnAnimation.getKeyFrame(XO_timer, true);
            game.batch.draw(frame, notiPos.x, notiPos.y,
                    GameConfig.PVP.TURN_WIDTH, GameConfig.PVP.TURN_HEIGHT);
            // countdown
            if (isLocalPlayerTurn) {
                int remaining = Math.max(0, GameConfig.PVP.TIME_SPAN - ((int) turnTimer)); // remaining >= 0
                String timeString = String.valueOf(remaining);
                if (remaining < 10)
                    timeString = '0' + timeString;
                renderCode(timeString, notiPos.x + GameConfig.PVP.TURN_WIDTH / 2 - GameConfig.PVP.DIGIT_SIZE,
                        notiPos.y - GameConfig.PVP.TURN_HEIGHT - 2 * GameConfig.WINDOW.SPACING);
            }
        }

        if (gameState.equals(GameState.GAME_OVER)) {
            drawResult();
        }

    }

    @Override
    public void render(float delta) {
        if (gameState.equals(GameState.PLAYING) && isLocalPlayerTurn) {
            turnTimer += delta;
            if (turnTimer > GameConfig.PVP.TIME_SPAN) {
                isLocalPlayerTurn = false;
                networkHandler.sendMessage(MessageType.TURN_SWITCH.serialize());
                turnTimer = 0f;
            }
        }
        super.render(delta);
    }

    @Override
    public void dispose() {
        super.dispose();
        broadcasting = false;

        if (networkHandler != null) {
            networkHandler.stop();
        }
        if (networkThread != null) {
            networkThread.interrupt();
        }
    }

    public void renderCode(String code, float x, float y) {
        int pos = 0;
        for (int i = 0; i < code.length(); i++) {
            char digit = code.charAt(i);
            if (Character.isDigit(digit)) {
                TextureRegion num = digitRegions.get(digit - '0');
                if (num != null) {
                    game.batch.draw(num, x + pos * GameConfig.PVP.DIGIT_SIZE, y,
                            GameConfig.PVP.DIGIT_SIZE, GameConfig.PVP.DIGIT_SIZE);
                    pos++;
                }
            }
        }
    }
}