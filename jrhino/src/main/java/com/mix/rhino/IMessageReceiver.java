package com.mix.rhino;

import java.nio.channels.Channel;

/**
 * Interface for implementations which receives
 * notifications/messages from the network layer
 *
 * @author  Ferenc Tollas
 */
public interface IMessageReceiver {

    /**
     * When a message arrives, this method
     * will be executed
     *
     * @param Channel The channel on which the message was received
     * @param message The received message
     */
    void messageArrived(final Channel channel, final String message);
}
