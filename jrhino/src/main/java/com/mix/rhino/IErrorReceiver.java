package com.mix.rhino;

/**
 * Interface which is used to notify a component about a network
 * error
 *
 * @author: Ferenc Tollas
 */
public interface IErrorReceiver {
    /**
     * When network error occurs on the client side, then
     * the error is sent to the client.
     *
     * @param errorMessage String containing the error message
     */
    void networkErrorOccurred(final String errorMessage);
}
