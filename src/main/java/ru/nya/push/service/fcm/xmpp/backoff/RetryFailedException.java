package ru.nya.push.service.fcm.xmpp.backoff;

public class RetryFailedException extends Exception {

    public RetryFailedException(int numberOfRetries, long timeToWait) {
        super("Retry Failed: Total of attempts: " + numberOfRetries + ". Total waited time: "
                + timeToWait + "ms.");
    }
}
