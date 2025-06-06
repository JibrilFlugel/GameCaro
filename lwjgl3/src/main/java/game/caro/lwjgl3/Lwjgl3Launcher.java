package game.caro.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;

import game.caro.Caro;
import game.caro.helper.GameConfig;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired())
            return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Caro(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle(GameConfig.SETTINGS.TITLE);
        //// Vsync limits the frames per second to what your hardware can display, and
        //// helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line
        //// after is a safeguard.
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to
        //// try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match
        //// the monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        //// If you remove the above line and set Vsync to false, you can get unlimited
        //// FPS, which can be
        //// useful for testing performance, but can also be very stressful to some
        //// hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can
        //// cause screen tearing.

        configuration.setWindowedMode(GameConfig.WINDOW.WIDTH, GameConfig.WINDOW.HEIGHT);
        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        //// They can also be loaded from the root of assets/ .
        configuration.setWindowIcon("logo.png");

        configuration.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public boolean closeRequested() {
                // This will (1) call your Screen.dispose() → GameScreenMultiplayer.dispose(),
                // which in turn calls networkHandler.stop(), closing the socket,
                // (2) then actually close the window.
                com.badlogic.gdx.Gdx.app.exit();
                return true; // allow the window to close
            }
        });
        return configuration;
    }
}