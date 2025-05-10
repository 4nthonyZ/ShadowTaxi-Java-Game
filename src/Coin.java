import bagel.Image;
import bagel.Input;
import bagel.Keys;

import java.util.Properties;

/**
 * Class representing coins in the game. Coins can be collected by either the player or the taxi.
 * It will set one level higher priority for the passengers that are waiting to get-in or already in the taxi.
 */
public class Coin{
    private final int MAX_FRAMES;
    private final Image IMAGE;
    private final int SPEED_Y;
    private final float RADIUS;

    private int x;
    private int y;
    private int moveY;
    private boolean isCollided;
    private int framesActive = 0;

    public Coin(int x, int y, Properties props) {
        this.x = x;
        this.y = y;
        this.moveY = 0;

        this.SPEED_Y = Integer.parseInt(props.getProperty("gameObjects.taxi.speedY"));
        this.RADIUS = Float.parseFloat(props.getProperty("gameObjects.coin.radius"));
        this.IMAGE = new Image(props.getProperty("gameObjects.coin.image"));
        this.MAX_FRAMES = Integer.parseInt(props.getProperty("gameObjects.coin.maxFrames"));
    }

    /**
     * Apply the effect of the coin on the priority of the passenger.
     * @param priority The current priority of the passenger.
     * @return The new priority of the passenger.
     */
    public Integer applyEffect(Integer priority) {
        if (framesActive <= MAX_FRAMES && priority > 1) {
            priority -= 1;
        }

        return priority;
    }

    /**
     * Move the object in y direction according to the keyboard input, and render the coin image, before collision
     * happens with PowerCollectable objects. Once the collision happens, the coin active time will be increased.
     * @param input The current mouse/keyboard input.
     */
    public void update(Input input) {
        if(isCollided) {
            framesActive++;
        } else {
            if(input != null) {
                adjustToInputMovement(input);
            }

            move();
            draw();
        }
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

    /**
     * Check if the coin has collided with any PowerCollectable objects, and power will be collected by PowerCollectable
     * object that is collided with.
     */
    public void collide(Taxi taxi) {
        if(hasCollidedWith(taxi)) {
            taxi.collectPower(this);
            setIsCollided();
        }
    }

    /**
     * Check if the object is collided with another object based on the radius of the two objects.
     * @param taxi The taxi object to be checked.
     * @return True if the two objects are collided, false otherwise.
     */
    public boolean hasCollidedWith(Taxi taxi) {
        // if the distance between the two objects is less than the sum of their radius, they are collided
        float collisionDistance = RADIUS + taxi.getRadius();
        float currDistance = (float) Math.sqrt(Math.pow(x - taxi.getX(), 2) + Math.pow(y - taxi.getY(), 2));
        return currDistance <= collisionDistance;
    }

    /**
     * Mark the status of the object as collided when it's collided with another object.
     * This will initiate the collision timeout.
     */
    public void setIsCollided() {
        this.isCollided = true;
    }

    public boolean getIsActive() {
        return isCollided && framesActive <= MAX_FRAMES && framesActive > 0;
    }

    public int getFramesActive() {
        return framesActive;
    }

    public int getMaxFrames(){
        return MAX_FRAMES;
    }
}
