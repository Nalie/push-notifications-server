package ru.nya.push.service.fcm.xmpp.backoff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Util class for back off strategy
 */

public class BackOffStrategy {

    private static final Logger logger = LoggerFactory.getLogger(BackOffStrategy.class);

    private static final int DEFAULT_RETRIES = 3;
    private static final long DEFAULT_WAIT_TIME_IN_MILLI = 1000;

    private final int numberOfRetries;
    private int numberOfTriesLeft;
    private final long defaultTimeToWait;
    private long timeToWait;
    private final Random random = new Random();

    public BackOffStrategy() {
        this(DEFAULT_RETRIES, DEFAULT_WAIT_TIME_IN_MILLI);
    }

    public BackOffStrategy(int numberOfRetries, long defaultTimeToWait) {
        this.numberOfRetries = numberOfRetries;
        this.numberOfTriesLeft = numberOfRetries;
        this.defaultTimeToWait = defaultTimeToWait;
        this.timeToWait = defaultTimeToWait;
    }

    /**
     * @return true if there are tries left
     */
    public boolean shouldRetry() {
        return numberOfTriesLeft > 0;
    }

    public void errorOccured() throws RetryFailedException {
        numberOfTriesLeft--;
        if (!shouldRetry()) {
            throw new RetryFailedException(numberOfRetries, timeToWait);
        }
        waitUntilNextTry();
        timeToWait *= 2;
        // we add a random time (recommendation from google)
        timeToWait += random.nextInt(500);
    }

    private void waitUntilNextTry() {
        try {
            Thread.sleep(timeToWait);
        } catch (InterruptedException e) {
            logger.info("Error waiting until next try for the backoff strategy. Error: {}", e.getMessage());
        }
    }

    /**
     * Use this method when the call was successful otherwise it will continue in an infinite loop
     */
    public void doNotRetry() {
        numberOfTriesLeft = 0;
    }

    /**
     * Reset back off state. Call this method after successful attempts if you want to reuse the class.
     */
    public void reset() {
        this.numberOfTriesLeft = numberOfRetries;
        this.timeToWait = defaultTimeToWait;
    }

}
