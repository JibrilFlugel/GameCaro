package game.caro.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                final String message = in.readLine();
                if (message == null) {
                    running = false;
                    Gdx.app.postRunnable(() -> gameScreen.handleDisconnection());
                    break;
                }
                Gdx.app.postRunnable(() -> gameScreen.processMessage(message));
            } catch (IOException e) {
                running = false;
                Gdx.app.postRunnable(() -> gameScreen.handleDisconnection());
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void stop() {
        running = false;
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            // TODO: idk what to put here
        }
    }
}
