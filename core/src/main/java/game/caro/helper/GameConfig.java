package game.caro.helper;

public class GameConfig {

    public static class WINDOW {
        public static final int WIDTH = 1600;
        public static final int HEIGHT = 900;
        public static final int MARGIN_Y = 100;
        public static final int MARGIN_X = 50;
        public static final int SPACING = 20;
    }

    public static class BOARD {
        public static final int SIZE = 15;
        public static final int LENGTH = WINDOW.HEIGHT - WINDOW.MARGIN_Y;
        public static final float CELL_SIZE = (float) LENGTH / SIZE;
    }

    public static class GAMEOVER {
        public static final int RESULT_WIDTH = 470;
        public static final int RESULT_HEIGHT = 120;
        public static final int BACK_WIDTH = 230;
        public static final int BACK_HEIGHT = 120;
        public static final int REPLAY_WIDTH = 280;
        public static final int REPLAY_HEIGHT = 120;
    }

    public static class MENU {
        public static final int BUTTON_WIDTH = 300;
        public static final int BUTTON_HEIGHT = 150;
    }

    public static class PVP {
        public static final int YOUR_CODE_WIDTH = 304;
        public static final int YOUR_CODE_HEIGHT = 72;
        public static final int DIGIT_SIZE = 72;
        public static final int TIME_SPAN = 30;
        public static final int TURN_WIDTH = 282;
        public static final int TURN_HEIGHT = 72;

        public static enum GameState {
            WAITING,
            PLAYING,
            GAME_OVER
        }
    }

    public static class SETTINGS {
        public static final String TITLE = "Caro Game";
        public static final int FPS = 60;
        public static final float FRAME_DURATION = 0.5f;
    }

    private GameConfig() {
    }
}