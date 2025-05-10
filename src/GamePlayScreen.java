import bagel.Font;
import bagel.Input;

import java.util.*;

/**
 * Represents the gameplay screen in the game.
 */
public class GamePlayScreen{
    private final Properties GAME_PROPS;
    private final Properties MSG_PROPS;

    // keep track of earning and coin timout
    private float totalEarnings;
    private float coinFramesActive;

    private int currFrame = 0;

    // game objects
    private Taxi taxi;
    private Passenger[] passengers;
    private Coin[] coins;
    private Background background1;
    private Background background2;

    private final float TARGET;
    private final int MAX_FRAMES;

    // vars for save score into the file
    private final String PLAYER_NAME;
    private boolean savedData;

    // display text vars
    private final Font INFO_FONT;
    private final int EARNINGS_Y;
    private final int EARNINGS_X;
    private final int COIN_X;
    private final int COIN_Y;
    private final int TARGET_X;
    private final int TARGET_Y;
    private final int MAX_FRAMES_X;
    private final int MAX_FRAMES_Y;

    private final int TRIP_INFO_X;
    private final int TRIP_INFO_Y;
    private final int TRIP_INFO_OFFSET_1;
    private final int TRIP_INFO_OFFSET_2;
    private final int TRIP_INFO_OFFSET_3;

    public GamePlayScreen(Properties gameProps, Properties msgProps, String playerName) {
        this.GAME_PROPS = gameProps;
        this.MSG_PROPS = msgProps;

        // read game objects from file and weather file and populate the game objects and weather conditions
        ArrayList<String[]> lines = IOUtils.readCommaSeperatedFile(gameProps.getProperty("gamePlay.objectsFile"));
        populateGameObjects(lines);

        this.TARGET = Float.parseFloat(gameProps.getProperty("gamePlay.target"));
        this.MAX_FRAMES = Integer.parseInt(gameProps.getProperty("gamePlay.maxFrames"));

        // display text vars
        INFO_FONT = new Font(gameProps.getProperty("font"), Integer.parseInt(
                gameProps.getProperty("gameplay.info.fontSize")));
        EARNINGS_Y = Integer.parseInt(gameProps.getProperty("gameplay.earnings.y"));
        EARNINGS_X = Integer.parseInt(gameProps.getProperty("gameplay.earnings.x"));
        COIN_X = Integer.parseInt(gameProps.getProperty("gameplay.coin.x"));
        COIN_Y = Integer.parseInt(gameProps.getProperty("gameplay.coin.y"));
        TARGET_X = Integer.parseInt(gameProps.getProperty("gameplay.target.x"));
        TARGET_Y = Integer.parseInt(gameProps.getProperty("gameplay.target.y"));
        MAX_FRAMES_X = Integer.parseInt(gameProps.getProperty("gameplay.maxFrames.x"));
        MAX_FRAMES_Y = Integer.parseInt(gameProps.getProperty("gameplay.maxFrames.y"));

        // current trip info vars
        TRIP_INFO_X = Integer.parseInt(gameProps.getProperty("gameplay.tripInfo.x"));
        TRIP_INFO_Y = Integer.parseInt(gameProps.getProperty("gameplay.tripInfo.y"));
        TRIP_INFO_OFFSET_1 = 30;
        TRIP_INFO_OFFSET_2 = 60;
        TRIP_INFO_OFFSET_3 = 90;

        this.PLAYER_NAME = playerName;
    }

    /**
     * Populate the game objects from the lines read from the game objects file.
     * @param lines list of lines read from the game objects file. lines are processed into String arrays using comma as
     *             delimiter.
     */
    private void populateGameObjects(ArrayList<String[]> lines) {

        // two background images stacked in y-axis are used to create a scrolling effect
        background1 = new Background(
                Integer.parseInt(GAME_PROPS.getProperty("window.width")) / 2,
                Integer.parseInt(GAME_PROPS.getProperty("window.height")) / 2,
                GAME_PROPS);
        background2 = new Background(
                Integer.parseInt(GAME_PROPS.getProperty("window.width")) / 2,
                -1 * Integer.parseInt(GAME_PROPS.getProperty("window.height")) / 2,
                GAME_PROPS);

        // Since you haven't learned Lists in Java, we have to use two for loops to iterate over the lines.
        int passengerCount = 0;
        int coinCount = 0;
        for(String[] lineElement: lines) {
            if(lineElement[0].equals(GameObjectType.PASSENGER.name())) {
                passengerCount++;
            } else if(lineElement[0].equals(GameObjectType.COIN.name())) {
                coinCount++;
            }
        }
        passengers = new Passenger[passengerCount];
        coins = new Coin[coinCount];

        // process each line in the file
        int passenger_idx = 0;
        int coin_idx = 0;
        for(String[] lineElement: lines) {
            int x = Integer.parseInt(lineElement[1]);
            int y = Integer.parseInt(lineElement[2]);

            if(lineElement[0].equals(GameObjectType.TAXI.name())) {
                taxi = new Taxi(x, y, passengerCount, this.GAME_PROPS);
            } else if(lineElement[0].equals(GameObjectType.PASSENGER.name())) {
                int priority = Integer.parseInt(lineElement[3]);
                int travelEndX = Integer.parseInt(lineElement[4]);
                int travelEndY = Integer.parseInt(lineElement[5]);

                Passenger passenger = new Passenger(x, y, priority, travelEndX, travelEndY, GAME_PROPS);
                passengers[passenger_idx] = passenger;
                passenger_idx++;

            } else if(lineElement[0].equals(GameObjectType.COIN.name())) {
                Coin coinPower = new Coin(x, y, this.GAME_PROPS);
                coins[coin_idx] = coinPower;
                coin_idx++;
            }
        }
    }

    /**
     * Update the states of the game objects based on the keyboard input.
     * Handle the spawning of other cars in random intervals
     * Change the background image and change priorities based on the weather condition
     * Handle collision between game objects
     * Spawn new taxi if the active taxi is destroyed
     * @param input
     * @return true if the game is finished, false otherwise
     */
    public boolean update(Input input) {
        currFrame++;

        background1.update(input, background2);
        background2.update(input, background1);

        for(Passenger passenger: passengers) {
            passenger.updateWithTaxi(input, taxi);
        }

        taxi.update(input);
        totalEarnings = taxi.calculateTotalEarnings();

        if(coins.length > 0) {
            int minFramesActive = coins[0].getMaxFrames();
            for(Coin coinPower: coins) {
                coinPower.update(input);
                coinPower.collide(taxi);

                // check if there's active coin and finding the coin with maximum ttl
                int framesActive = coinPower.getFramesActive();
                if(coinPower.getIsActive() && minFramesActive > framesActive) {
                    minFramesActive = framesActive;
                }
            }
            coinFramesActive = minFramesActive;
        }

        displayInfo();

        return isGameOver() || isLevelCompleted();

    }

    /**
     * Display the game information on the screen.
     */
    public void displayInfo() {
        INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.earnings") + getTotalEarnings(), EARNINGS_X, EARNINGS_Y);
        INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.target") + String.format("%.02f", TARGET), TARGET_X,
                TARGET_Y);
        INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.remFrames") + (MAX_FRAMES - currFrame), MAX_FRAMES_X,
                MAX_FRAMES_Y);

        if(coins.length > 0 && coins[0].getMaxFrames() != coinFramesActive) {
            INFO_FONT.drawString(String.valueOf(Math.round(coinFramesActive)), COIN_X, COIN_Y);
        }

        Trip lastTrip = taxi.getLastTrip();
        if(lastTrip != null) {
            if(lastTrip.isComplete()) {
                INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.completedTrip.title"), TRIP_INFO_X, TRIP_INFO_Y);
            } else {
                INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.onGoingTrip.title"), TRIP_INFO_X, TRIP_INFO_Y);
            }
            INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.trip.expectedEarning")
                    + lastTrip.getPassenger().getTravelPlan().getExpectedFee(), TRIP_INFO_X, TRIP_INFO_Y
                    + TRIP_INFO_OFFSET_1);
            INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.trip.priority")
                    + lastTrip.getPassenger().getTravelPlan().getPriority(), TRIP_INFO_X, TRIP_INFO_Y
                    + TRIP_INFO_OFFSET_2);
            if(lastTrip.isComplete()) {
                INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.trip.penalty") + String.format("%.02f",
                        lastTrip.getPenalty()), TRIP_INFO_X, TRIP_INFO_Y + TRIP_INFO_OFFSET_3);
            }
        }
    }

    public String getTotalEarnings() {
        return String.format("%.02f", totalEarnings);
    }

    /**
     * Check if the game is over. If the game is over and not saved the score, save the score.
     * @return true if the game is over, false otherwise.
     */
    public boolean isGameOver() {
        // Game is over if the current frame is greater than the max frames
        boolean isGameOver = currFrame >= MAX_FRAMES;

        if(currFrame >= MAX_FRAMES && !savedData) {
            savedData = true;
            IOUtils.writeLineToFile(GAME_PROPS.getProperty("gameEnd.scoresFile"), PLAYER_NAME + "," + totalEarnings);
        }
        return isGameOver;
    }

    /**
     * Check if the level is completed. If the level is completed and not saved the score, save the score.
     * @return true if the level is completed, false otherwise.
     */
    public boolean isLevelCompleted() {
        // Level is completed if the total earnings is greater than or equal to the target earnings
        boolean isLevelCompleted = totalEarnings >= TARGET;
        if(isLevelCompleted && !savedData) {
            savedData = true;
            IOUtils.writeLineToFile(GAME_PROPS.getProperty("gameEnd.scoresFile"), PLAYER_NAME + "," + totalEarnings);
        }
        return isLevelCompleted;
    }
}
