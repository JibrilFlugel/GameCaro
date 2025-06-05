package game.caro.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.badlogic.gdx.Gdx;

import game.caro.screens.pvp.GameScreenMultiplayer;

public class NetworkHandler implements Runnable {
    private BufferedReader in;
    private PrintWriter out;
    private boolean running = true;
    private Socket socket;
    private final GameScreenMultiplayer gameScreen;

    public NetworkHandler(Socket socket, GameScreenMultiplayer gameScreen) throws IOException {
        this.socket = socket;
        this.gameScreen = gameScreen;
        socket.setSoTimeout(1000);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                String message = in.readLine();
                if (message == null) {
                    running = false;
                    Gdx.app.postRunnable(() -> gameScreen.handleDisconnection());
                    break;
                }
                Gdx.app.postRunnable(() -> gameScreen.processMessage(message));
            } catch (SocketTimeoutException e) {
                if (!running) {
                    break;
                }
            } catch (IOException e) {
                running = false;
                Gdx.app.postRunnable(() -> gameScreen.handleDisconnection());
            }
        }
        System.out.println("NetworkHandler thread exiting");
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void stop() {
        running = false;
        try {
            socket.shutdownInput();
        } catch (IOException ignored) {
        }
        try {
            in.close();
        } catch (IOException ignored) {
        }
        out.close();
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
    // TODO: Socket only shutdown when manually close terminal
}
