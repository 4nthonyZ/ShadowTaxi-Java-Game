import bagel.Image;
import bagel.Input;
import bagel.Keys;

import java.util.Properties;

/**
 * The class representing the taxis in the game play
 */
public class Taxi {

    private final Image IMAGE;
    private final int SPEED_X;
    private final float RADIUS;

    private final Trip[] TRIPS;
    private int tripCount;

    private int x;
    private int y;
    private boolean isMovingY;
    private boolean isMovingX;

    private Coin coinPower;
    private Trip trip;

    public Taxi(int x, int y, int maxTripCount, Properties props) {
        this.x = x;
        this.y = y;
        TRIPS = new Trip[maxTripCount];

        this.SPEED_X = Integer.parseInt(props.getProperty("gameObjects.taxi.speedX"));
        this.IMAGE = new Image(props.getProperty("gameObjects.taxi.image"));
        this.RADIUS = Float.parseFloat(props.getProperty("gameObjects.taxi.radius"));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isMovingY() {
        return isMovingY;
    }

    public boolean isMovingX() {
        return isMovingX;
    }

    public float getRadius() {
        return RADIUS;
    }

    /**
     * If it's a new trip, it will added to the list of trips.
     * @param trip trip object
     */
    public void setTrip(Trip trip) {
        this.trip = trip;
        if(trip != null) {
            this.TRIPS[tripCount] = trip;
            tripCount++;
        }
    }

    public Trip getTrip() {
        return this.trip;
    }

    /**
     * Get the last trip from the list of trips.
     * @return Trip object
     */
    public Trip getLastTrip() {
        if(tripCount == 0) {
            return null;
        }
        return TRIPS[tripCount - 1];
    }

    /**
     * Update the GameObject object's movement states based on the input.
     * Render the game object into the screen.
     * @param input The current mouse/keyboard input.
     */
    public void update(Input input) {
        // if the taxi has coin power, apply the effect of the coin on the priority of the passenger
        // (See the logic in TravelPlan class)
        if (trip != null && coinPower != null) {
            TravelPlan tp = trip.getPassenger().getTravelPlan();
            int newPriority = tp.getPriority();
            if(!tp.getCoinPowerApplied()) {
                newPriority = coinPower.applyEffect(tp.getPriority());
            }
            if(newPriority < tp.getPriority()) {
                tp.setCoinPowerApplied();
            }
            tp.setPriority(newPriority);
        }

        if(input != null) {
            adjustToInputMovement(input);
        }

        if(trip != null && trip.hasReachedEnd()) {
            getTrip().end();
        }

        draw();

        // the flag of the current trip renders to the screen
        if(tripCount > 0) {
            Trip lastTrip = TRIPS[tripCount - 1];
            if(!lastTrip.getPassenger().hasReachedFlag()) {
                lastTrip.getTripEndFlag().update(input);
            }
        }

    }

    /**
     * Draw the GameObject object into the screen.
     */
    public void draw() {
        IMAGE.draw(x, y);
    }

    /**
     * Adjust the movement of the taxi based on the keyboard input.
     * If the taxi has a driver, and taxi has health>0 the taxi can only move left and right (fixed in y direction).
     * If the taxi does not have a driver, the taxi can move in all directions.
     * @param input The current mouse/keyboard input.
     */
    public void adjustToInputMovement(Input input) {
        if (input.wasPressed(Keys.UP)) {
            isMovingY = true;
        }  else if(input.wasReleased(Keys.UP)) {
            isMovingY = false;
        } else if(input.isDown(Keys.LEFT)) {
            x -= SPEED_X;
            isMovingX = true;
        }  else if(input.isDown(Keys.RIGHT)) {
            x += SPEED_X;
            isMovingX =  true;
        } else if(input.wasReleased(Keys.LEFT) || input.wasReleased(Keys.RIGHT)) {
            isMovingX = false;
        }
    }

    public void collectPower(Coin coin) {
        coinPower = coin;
    }

    /**
     * Calculate total earnings. (See how fee is calculated for each trip in Trip class.)
     * @return int, total earnings
     */
    public float calculateTotalEarnings() {
        float totalEarnings = 0;
        for(Trip trip : TRIPS) {
            if (trip != null) {
                totalEarnings += trip.getFee();
            }
        }
        return totalEarnings;
    }
}
