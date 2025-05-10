import bagel.Image;
import bagel.Input;
import bagel.Keys;

import java.util.Properties;

/**
 * A class representing the background of the game play.
 */
public class Background {

    private final int WINDOW_HEIGHT;
    private final Image IMAGE;
    private final int SPEED_Y;

    private int x;
    private int y;
    private int moveY;

    public Background(int x, int y, Properties props) {
        this.x = x;
        this.y = y;
        this.moveY = 0;

        this.SPEED_Y = Integer.parseInt(props.getProperty("gameObjects.taxi.speedY"));
        this.IMAGE = new Image(props.getProperty("backgroundImage"));
        this.WINDOW_HEIGHT = Integer.parseInt(props.getProperty("window.height"));
    }

    /**
     * Move the background in y direction according to the keyboard input. And render the background image.
     * @param input The current mouse/keyboard input.
     */
    public void update(Input input, Background background) {
        if(input != null) {
            adjustToInputMovement(input);
        }

        move();
        draw();

        if (y >= WINDOW_HEIGHT * 1.5) {
            y = background.getY() - WINDOW_HEIGHT;
        }
    }

    public int getY() {
        return y;
    }

    /**
     * Move the GameObject object in the y-direction based on the speedY attribute.
     */
    public void move() {
        this.y += SPEED_Y * moveY;
    }

    /**
     * Draw the GameObject object into the screen.
     */
    public void draw() {
        IMAGE.draw(x, y);
    }

    /**
     * Adjust the movement direction in y-axis of the GameObject based on the keyboard input.
     * @param input The current mouse/keyboard input.
     */
    public void adjustToInputMovement(Input input) {
        if (input.wasPressed(Keys.UP)) {
            moveY = 1;
        }  else if(input.wasReleased(Keys.UP)) {
            moveY = 0;
        }
    }
}
